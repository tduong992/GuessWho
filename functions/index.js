const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

const firestore = admin.firestore();

const checkPreconditions = (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError("unauthenticated", "Please login");
    }

    if (!data) {
        throw new functions.https.HttpsError("failed-precondition", "Please login");
    }
}


/**
 * P1 sends challenge with P2 email
 * Search for P2 uid
 * Create round for P1 and P2
 * Send notification to P2
 * @type {HttpsFunction & Runnable<any>}
 */
exports.searchFriend = functions.https.onCall(async (data, context) => {
    checkPreconditions(data, context);

    const userChallenger = await admin.auth().getUser(context.auth.uid);
    const userChallenged = await admin.auth().getUserByEmail(data.email);

    console.log("User challenged:", userChallenged);

    // Store match for p1 and p2
    const match = {
        player1: context.auth.uid,
        player2: userChallenged.uid,
        state: "PENDING",
    };

    console.log("Match created", match);

    const matchDoc = await firestore.collection('matches').add(match);
    const matchId = matchDoc.id;

    const savedMatch = await matchDoc.get().then(doc => doc.data());

    const round = {
        matchId: matchId,
        player1: savedMatch.player1,
        player2: savedMatch.player2,
        countFaceUpPlayer1: 16,
        countFaceUpPlayer2: 16,
        state: "SELECT_CHARACTER"
    }

    const roundRef = await firestore.collection('rounds').add(round);

    matchDoc.update({
        rounds: [{
            roundId: roundRef.id,
            winner: ""
        }]
    })

    console.log("Match saved", matchId, savedMatch);

    // Notify p2 of challenge
    const topic = userChallenged.email.replace('@', '');

    const message = {
        data: {
            matchId: matchId,
            uid: userChallenger.uid,
            email: userChallenger.email,
            displayName: userChallenger.displayName,
            imgUrl: userChallenger.photoURL
        },
        topic: topic
    };

    console.log(`Sending to topic ${topic} message`, message);

    return admin.messaging().send(message)
        .then((response) => {
            // Response is a message ID string.
            console.log('Successfully sent message:', response);
            // Answer p1 with created match
            return {
                matchId: matchId,
                ...savedMatch
            };
        })
        .catch((error) => {
            console.log('Error sending message:', error);
            throw new functions.https.HttpsError('unknown', error.message, error);
        });

});

/*
* @type {HttpsFunction & Runnable<any>}
*/
exports.answerChallenge = functions.https.onCall(async (data, context) => {
    checkPreconditions(data, context);

    const answer = data.answer;
    const matchId = data.matchId;
    if (matchId && answer) {
        const matchRef = firestore.collection('matches').doc(matchId);

        console.log("Player 2 accepted challenge", matchId)

        const match = await matchRef.get().then(doc => doc.data())

        console.log("Creating first round for match", match);

        // Update match state to ACCEPTED and add reference to the current running round
        // This will be useful to resume the match if the round is suddenly interrupted by any event
        matchRef.update({
            state: "ACCEPTED"
        });

        console.log("Challenge was accepted");
    } else if (matchId && answer === false) {
        const matchRef = firestore.collection('matches').doc(matchId);

        console.log("Player 2 rejected challenge", matchId)
        matchRef.update({
            state: "REJECTED"
        });
    } else {
        throw new functions.https.HttpsError("failed-precondition", "Match id or answer is empty!");
    }
});

exports.selectCharacter = functions.https.onCall(async (data, context) => {
    checkPreconditions(data, context);

    const playerId = context.auth.uid;
    const roundId = data.roundId;
    const selectedCharacter = data.selectedCharacter;

    const roundRef = firestore.collection('rounds').doc(roundId);
    let round = await roundRef.get().then(doc => doc.data());

    if (!round.state === "SELECT_CHARACTER") {
        throw new functions.https.HttpsError("failed-precondition", "Round is not in character selection state!");
    }

    const characterPlayer = playerId === round.player1 ? 'characterPlayer1' : 'characterPlayer2';
    await roundRef.update({
        [characterPlayer]: selectedCharacter
    })

    round = await roundRef.get().then(doc => doc.data());
    const bothHaveSelectedCharacter = !!round.characterPlayer1 && !!round.characterPlayer2
    roundRef.update({
        turnToPlayer: bothHaveSelectedCharacter ? round.player2 : '',
        state: bothHaveSelectedCharacter ? 'GAME_PASS' : 'SELECT_CHARACTER'
    });
});

exports.askQuestion = functions.https.onCall(async (data, context) => {
    checkPreconditions(data, context);

    const playerId = context.auth.uid;
    const roundId = data.roundId;
    const askedQuestion = data.askedQuestion;

    const roundRef = firestore.collection('rounds').doc(roundId);
    let round = await roundRef.get().then(doc => doc.data());

    const currentPlayer = playerId === round.player1 ? round.player1 : round.player2
    if (round.turnToPlayer !== currentPlayer) {
        throw new functions.https.HttpsError("failed-precondition", `Request from ${ currentPlayer }: is not your turn yet!`);
    }

    const questionPlayer = playerId === round.player1 ? 'questionsPlayer1' : 'questionsPlayer2';
    const questions = round[questionPlayer] || [];
    questions.push(askedQuestion);

    // Check if a player has already asked a question this turn
    const questionsPlayer1 = round.questionsPlayer1 || [];
    const questionsPlayer2 = round.questionsPlayer2 || [];
    if (playerId === round.player1 && (questions.length - questionsPlayer1.length) > 1) {
        throw new functions.https.HttpsError("failed-precondition", `You already asked a question this turn!`);
    }

    if (playerId === round.player2 && (questions.length - questionsPlayer2.length) > 1) {
        throw new functions.https.HttpsError("failed-precondition", `You already asked a question this turn!`);
    }

    const update = {
        [questionPlayer]: questions,
        state: 'GAME_QUESTION_ASK'
    }

    console.log("Added question to field:", questionPlayer);
    console.log("Question asked", askedQuestion);
    console.log("Question update", update);

    roundRef.update(update);
});

exports.answerQuestion = functions.https.onCall(async (data, context) => {
    checkPreconditions(data, context);

    const playerId = context.auth.uid;
    const roundId = data.roundId;
    const answeredQuestion = data.answeredQuestion;

    const roundRef = firestore.collection('rounds').doc(roundId);
    let round = await roundRef.get().then(doc => doc.data());

    const questionPlayer = playerId === round.player1 ? 'questionsPlayer2' : 'questionsPlayer1';
    const questions = round[questionPlayer];
    const askedQuestion = questions[questions.length - 1];
    askedQuestion.answer = answeredQuestion;
    askedQuestion.disabled = true;

    const update = {
        [questionPlayer]: questions,
        state: 'GAME_QUESTION_ANSWER'
    }

    roundRef.update(update);
});

exports.passTurn = functions.https.onCall(async (data, context) => {
    checkPreconditions(data, context);

    const playerId = context.auth.uid;
    const roundId = data.roundId;
    const countFaceUp = data.countFaceUp;

    const roundRef = firestore.collection('rounds').doc(roundId);
    let round = await roundRef.get().then(doc => doc.data());

    const playerTurn = playerId === round.player1 ? round.player2 : round.player1;
    const countFaceUpPlayer = playerId === round.player1 ? 'countFaceUpPlayer1' : 'countFaceUpPlayer2';
    roundRef.update({
        turnToPlayer: playerTurn,
        [countFaceUpPlayer]: countFaceUp, 
        state: 'GAME_PASS'
    });
});

exports.askGuess = functions.https.onCall(async (data, context) => {
    checkPreconditions(data, context);
    
    const playerId = context.auth.uid;
    const roundId = data.roundId;
    const guess = data.guess;

    const roundRef = firestore.collection('rounds').doc(roundId);
    let round = await roundRef.get().then(doc => doc.data());

    const guessPlayer = playerId === round.player1 ? 'guessPlayer1' : 'guessPlayer2';
    const guesses = round[guessPlayer] || [];
    guesses.push(guess)

    roundRef.update({
        state: 'GAME_GUESS_ASK',
        [guessPlayer]: guesses
    })
});

exports.answerGuess = functions.https.onCall(async (data, context) => {
    checkPreconditions(data, context);

    console.log("Answer guess received");
    
    const playerId = context.auth.uid;
    const roundId = data.roundId;
    const guess = data.guess;

    console.log("Answer guess payload:", data);

    const roundRef = firestore.collection('rounds').doc(roundId);
    let round = await roundRef.get().then(doc => doc.data());

    const guessPlayer = playerId === round.player1 ? 'guessPlayer2' : 'guessPlayer1';
    const guesses = round[guessPlayer] || [];
    guesses[guesses.length - 1].answer = guess.answer;

    if (guess.answer) {
        console.log("Correct guess", roundId, round, guess);
        const matchRef = firestore.collection('matches').doc(round.matchId);
        let match = await matchRef.get().then(doc => doc.data());
        const matchRound = match.rounds[match.rounds.length - 1];

        let player1Wins = 0;
        let player2Wins = 0;
        match.rounds.forEach(round => {
            if (round.winner === match.player1) {
                player1Wins++;
            } else if (round.winner === match.player2) {
                player2Wins++;
            }
        })

        matchRound.winner = playerId === round.player1 ? round.player2 : round.player1;

        if (player1Wins < 3 && player2Wins < 3) {

            const newRound = {
                matchId: round.matchId,
                player1: round.player1,
                player2: round.player2,
                countFaceUpPlayer1: 16,
                countFaceUpPlayer2: 16,
                state: "SELECT_CHARACTER"
            }
        
            const newRoundRef = await firestore.collection('rounds').add(newRound);
        
            match.rounds.push({
                roundId: newRoundRef.id,
                winner: ""
            });
        
            console.log("Created new round", newRound);
        }

        await matchRef.update({
            rounds: match.rounds
        });

        roundRef.update({
            state: 'GAME_END',
            [guessPlayer]: guesses
        })
    } else {
        console.log("Wrong guess", roundId, round, guess);
        roundRef.update({
            state: 'GAME_GUESS_ANSWER',
            [guessPlayer]: guesses
        })
    }
});

exports.quitMatch = functions.https.onCall(async (data, context) => {
    checkPreconditions(data, context);
    const matchId = data.matchId;

    const matchRef = firestore.collection('matches').doc(matchId);
    matchRef.update({
        state: "ABORT"
    });
});
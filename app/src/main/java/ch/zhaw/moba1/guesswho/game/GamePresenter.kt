package ch.zhaw.moba1.guesswho.game

import android.util.Log
import ch.zhaw.moba1.guesswho.Attribute
import ch.zhaw.moba1.guesswho.Character
import ch.zhaw.moba1.guesswho.model.Guess
import ch.zhaw.moba1.guesswho.model.Match
import ch.zhaw.moba1.guesswho.model.Round
import ch.zhaw.moba1.guesswho.model.WinCounts
import ch.zhaw.moba1.guesswho.originalAttributes
import ch.zhaw.moba1.guesswho.originalCharacters

class GamePresenter(private val gameActivity: GameActivity, private val gameModel: GameModel) {

    private val TAG = GamePresenter::class.java.name

    private val MAX_WIN_COUNT = 3

    private lateinit var myPlayerId: String

    private lateinit var match: Match

    private lateinit var round: Round

    private lateinit var selectedCharacter: Character

    private lateinit var attributes: MutableList<Attribute>

    private lateinit var characters: MutableList<Character>

    private var isGuessState: Boolean = false

    fun initGameView(matchId: String, iAmPlayer: String) {
        this.myPlayerId = iAmPlayer

        resetGameView(matchId, true)

        subscribeToMatch(matchId)
    }

    fun resetGameView(matchId: String, isInit: Boolean = false) {
        this.attributes = originalAttributes.map { it.copy() }.toMutableList()
        this.characters = originalCharacters.map { it.copy() }.toMutableList()

        fetchMatch(matchId) {
            gameActivity.showSelectCharacterHint()

            subscribeToRound(match.currentRound().roundId)
        }

        if (isInit) {
            gameActivity.createQuestionListView(attributes)
            gameActivity.createQuestionListViewDisabled(attributes)
            gameActivity.createCharacterListView(characters)
        } else {
            gameActivity.setRecyclerViewAdapterDatasets(characters, attributes)
        }
    }

    fun fetchMatch(matchId: String, callback: () -> Unit) {
        gameModel.fetchMatch(matchId) {
            this.match = it
            fetchRound(match.currentRound().roundId, callback)
        }
    }

    fun fetchRound(roundId: String, callback: () -> Unit) {
        // Listen in Real-Time change to Round object, matching the given roundId
        gameModel.fetchRound(roundId) {
            this.round = it
            callback()
        }
    }

    fun onCharacterClick(character: Character) {
        if (isGuessState) {
            val guess = Guess(character.name, null)
            gameModel.askGuess(round.roundId, guess)
            isGuessState = false
        } else if (round.isGameRunning()) {
            character.isFaceDown = !character.isFaceDown
            gameActivity.notifyCharacterGridViewAdapter()
        } else if (round.isSelectCharacter()) {
            gameModel.selectCharacter(round.roundId, character) {
                gameActivity.setSelectedCharacterOnGameScreen(character)
            }
        }
    }

    fun onQuestionClick(attribute: Attribute) {
        gameModel.askQuestion(round.roundId, attribute)
    }

    fun showPopupWindowAnswerPending() {
        val myQuestion = round.getMyQuestion(myPlayerId)
        gameActivity.showPopupWindowAnswer(myQuestion, null)
    }

    fun handleAnswerReceived() {
        val myQuestion = round.getMyQuestion(myPlayerId)
        gameActivity.showPopupWindowAnswer(myQuestion, myQuestion.answer)
        val attributeIndex = attributes.indexOfFirst { it.name == myQuestion.name }
        attributes[attributeIndex] = myQuestion
        gameActivity.showPlayerAction()
        gameActivity.notifyAttributeGridViewAdapter()
        gameActivity.notifyDisabledAttributeGridViewAdapter()
    }

    fun answerQuestion(answer: Boolean) {
        gameModel.answerQuestion(round.roundId, answer)
    }

    private fun subscribeToMatch(matchId: String) {
        gameModel.subscribeToMatch(matchId) { currentMatch ->
            Log.i(TAG, "Match update: $currentMatch")
            var previousMatch: Match? = null
            if (::match.isInitialized) {
                previousMatch = this.match
            }
            this.match = currentMatch

            if (currentMatch.isAbort()) {
                Log.i(TAG, "Opponent quit the match ${currentMatch.matchId}")
                gameActivity.opponentQuit()
                return@subscribeToMatch
            }

            val winCount = currentMatch.getWinCount(myPlayerId)
            gameActivity.setTrophies(winCount)

            Log.i(TAG, "Set trophy count")

            if (winCount.isGameOver(MAX_WIN_COUNT)) {
                Log.i(TAG, "Game over: $winCount")
                gameActivity.dismissPopupWindowGuessAnswer()
                val didIWin = currentMatch.didIWinTheMatch(myPlayerId)
                val otherPlayerCharacter = round.getOtherCharacter(myPlayerId)
                gameActivity.showPopupWindowGameOver(otherPlayerCharacter!!, didIWin)
                unsubscribeAll()

            } else if (isNewRound(previousMatch, currentMatch, winCount)) {
                Log.i(TAG, "Round ${round.roundId} over, starting a new round")
                gameActivity.dismissPopupWindowGuessAnswer()
                gameActivity.setSelectedCharacterOnGameScreen(null)

                val winnerPlayerId = match.rounds.find { it.roundId == round.roundId }!!.winner
                val didIWin = winnerPlayerId == myPlayerId
                val otherPlayerCharacter = round.getOtherCharacter(myPlayerId)
                gameActivity.showPopupWindowRoundOver(otherPlayerCharacter!!, didIWin)
                gameModel.unsubscribeToRound()
                resetGameView(matchId)
                gameActivity.setFaceUpCounters(16, 16)
                gameActivity.showNextRoundToast()
            }
        }
    }

    private fun isNewRound(
        previousMatch: Match?,
        currentMatch: Match,
        winCount: WinCounts
    ) =
        previousMatch != null && previousMatch.rounds.size < currentMatch.rounds.size && (winCount.myWins <= MAX_WIN_COUNT && winCount.opponentWins <= MAX_WIN_COUNT)

    private fun subscribeToRound(roundId: String) {
        gameModel.subscribeToRound(roundId) { currentRound ->
            Log.i(TAG, "Round update. Previous round ${this.round}, current round $currentRound")
            this.round = currentRound
            val myCharacter = currentRound.getMyCharacter(myPlayerId)

            // TODO: for dev
            if (myCharacter != null) {
                selectedCharacter = myCharacter
            }


            if (myCharacter == null && currentRound.isSelectCharacter()) {
                gameActivity.showSelectCharacterHint()
            } else if (currentRound.isSelectCharacter()) {
                gameActivity.showWaitingOnOpponentCharacterSelectionHint()
            } else if (currentRound.isGameEnd()) {
                Log.i(TAG, "GAME OVER!")
            } else if (currentRound.isMyTurn(myPlayerId)) {
                Log.i(TAG, "My turn logic")
                myTurnLogic()
            } else {
                Log.i(TAG, "Opponent's turn!")
                // TODO: wait on opponent action
                opponentTurnLogic()
            }

        }
    }

    fun myTurnLogic() {
        if (round.isGamePass()) {
            Log.i(TAG, "Game Pass")
            gameActivity.showPlayerAction()
            gameActivity.setFaceUpCounters(
                round.getMyFaceUpCounter(myPlayerId),
                round.getOpponentFaceUpCounter(myPlayerId)
            )
        } else if (round.isQuestionAsk()) {
            Log.i(TAG, "Question Ask. Nothing to do")
            showPopupWindowAnswerPending()
        } else if (round.isQuestionAnswer()) {
            Log.i(TAG, "Question Answer")
            handleAnswerReceived()
        } else if (round.isGuessAsk()) {
            Log.i(TAG, "Guess Ask")
            gameActivity.showPopupWindowGuessAnswer(round.getMyGuess(myPlayerId))
        } else if (round.isGuessAnswer()) {
            Log.i(TAG, "Guess Answer")
            gameActivity.showPopupWindowGuessAnswer(round.getMyGuess(myPlayerId))
        } else {
            Log.w(TAG, "Error")
            throw IllegalArgumentException("This is not supposed to be happening! Current round $round")
        }
    }

    fun opponentTurnLogic() {
        if (round.isGamePass()) {
            Log.i(TAG, "Game Pass")
            // TODO: wait for opponent action
            gameActivity.showWaitingOnOpponentAction()
            gameActivity.setFaceUpCounters(
                round.getMyFaceUpCounter(myPlayerId),
                round.getOpponentFaceUpCounter(myPlayerId)
            )
        } else if (round.isQuestionAsk()) {
            Log.i(TAG, "Question Ask")
            gameActivity.showPopupWindowQuestion(
                selectedCharacter,
                round.getQuestion(myPlayerId)
            )
        } else if (round.isQuestionAnswer()) {
            Log.i(TAG, "Question Answer")
            // TODO: wait on opponent action
        } else if (round.isGuessAsk()) {
            Log.i(TAG, "Guess Ask")
            gameActivity.showPopupWindowGuessQuestion(selectedCharacter, round.getGuess(myPlayerId))
        } else if (round.isGuessAnswer()) {
            Log.i(TAG, "Guess Answer")
            // TODO: wait for opponent action
        } else {
            Log.w(TAG, "Error")
            throw IllegalArgumentException("This is not supposed to be happening! Current round $round")
        }
    }

    fun passTurn() {
        gameModel.passTurn(round.roundId, countFaceUp())
    }

    fun enterGuessState() {
        isGuessState = true
    }

    fun answerGuess(guess: Guess) {
        gameModel.answerGuess(round.roundId, guess)
    }

    fun unsubscribeAll() {
        gameModel.unsubscribeToRound()
        gameModel.unsubscribeToMatch()
    }

    fun quitMatch() {
        unsubscribeAll()
        gameModel.quitMatch(match.matchId)
    }

    fun countFaceUp() = characters.count { !it.isFaceDown }
}
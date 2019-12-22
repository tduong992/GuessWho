package ch.zhaw.moba1.guesswho.game

import android.util.Log
import ch.zhaw.moba1.guesswho.Attribute
import ch.zhaw.moba1.guesswho.Character
import ch.zhaw.moba1.guesswho.model.Guess
import ch.zhaw.moba1.guesswho.model.Match
import ch.zhaw.moba1.guesswho.model.Round
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions

class GameModel {

    private val TAG = GameModel::class.java.name

    private val functions = FirebaseFunctions.getInstance()

    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var roundChangeListener: ListenerRegistration

    private lateinit var matchChangeListener: ListenerRegistration

    fun fetchMatch(matchId: String, callback: (Match) -> Unit) {
        // Listen in Real-Time change to Match object, matching the given matchId
        val matchRef = firestore.collection("matches").document(matchId).get()

        matchRef
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    val match = Match.of(matchId, document.data)
                    Log.d(TAG, "Parsed match: $match")

                    callback(match)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    fun fetchRound(roundId: String, callback: (Round) -> Unit) {
        // Listen in Real-Time change to Round object, matching the given roundId
        val fetchRoundTask = firestore.collection("rounds").document(roundId).get()

        fetchRoundTask
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    val round = Round.of(roundId, document.data)
                    Log.d(TAG, "Parsed round: $round")

                    callback(round)
                } else {
                    Log.d(TAG, "No such document")
                }
            }
    }

    fun selectCharacter(roundId: String, character: Character, callback: () -> Unit) {
        val payload = hashMapOf(
            "roundId" to roundId,
            "selectedCharacter" to character.toPayload()
        )

        functions
            .getHttpsCallable("selectCharacter")
            .call(payload)
            .continueWith { task ->
                Log.i(TAG, "Called function: $task")
                callback()
            }
    }

    fun subscribeToMatch(matchId: String, callback: (Match) -> Unit) {
        val matchRef = firestore.collection("matches").document(matchId)

        matchChangeListener = matchRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.w(TAG, "Failed to listen for match changes", exception)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val matchData = snapshot.data
                Log.i(TAG, "Current match data: $matchData")
                val currentMatch = Match.of(matchId, matchData)
                Log.i(TAG, "Current parsed match: $currentMatch")
                callback(currentMatch)
            } else {
                Log.i(TAG, "Round $matchId was deleted")
                throw IllegalArgumentException("Match was deleted")
            }
        }

    }

    fun subscribeToRound(roundId: String, callback: (Round) -> Unit) {
        // Listen in Real-Time change to Round object, matching the given roundId
        val roundRef = firestore.collection("rounds").document(roundId)

        roundChangeListener = roundRef.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.w(TAG, "Failed to listen for round changes", exception)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val roundData = snapshot.data
                Log.i(TAG, "Current round data: $roundData")
                val currentRound = Round.of(roundId, roundData)
                Log.i(TAG, "Current parsed round: $currentRound")
                callback(currentRound)
            } else {
                Log.i(TAG, "Round $roundId was deleted")
                throw IllegalArgumentException("Round was deleted")
            }
        }
    }

    fun askQuestion(roundId: String, attribute: Attribute) {
        val payload = hashMapOf(
            "roundId" to roundId,
            "askedQuestion" to attribute.toPayload()
        )

        functions
            .getHttpsCallable("askQuestion")
            .call(payload)
            .continueWith { task ->
                Log.i(TAG, "Asked question: $attribute")
            }
    }

    fun answerQuestion(roundId: String, answer: Boolean) {
        val payload = hashMapOf(
            "roundId" to roundId,
            "answeredQuestion" to answer
        )

        functions
            .getHttpsCallable("answerQuestion")
            .call(payload)
            .continueWith { task ->
                Log.i(TAG, "Answered question: $answer")
            }
    }

    fun passTurn(roundId: String, countFaceUp: Int) {
        val payload = hashMapOf(
            "roundId" to roundId,
            "countFaceUp" to countFaceUp
        )

        functions
            .getHttpsCallable("passTurn")
            .call(payload)
            .continueWith { task ->
                Log.i(TAG, "Turn passed")
            }
    }

    fun askGuess(roundId: String, guess: Guess) {
        val payload = hashMapOf(
            "roundId" to roundId,
            "guess" to guess.toPayload()
        )

        functions
            .getHttpsCallable("askGuess")
            .call(payload)
            .continueWith { task ->
                Log.i(TAG, "Ask guess character $guess")
            }
    }

    fun answerGuess(roundId: String, guess: Guess) {
        val payload = hashMapOf(
            "roundId" to roundId,
            "guess" to guess.toPayload()
        )

        functions
            .getHttpsCallable("answerGuess")
            .call(payload)
            .continueWith { task ->
                Log.i(TAG, "Answer guess character $payload")
            }
    }

    fun unsubscribeToRound() {
        Log.i(TAG, "Unsubscribing from round changes")
        roundChangeListener.remove()
    }

    fun unsubscribeToMatch() {
        Log.i(TAG, "Unsubscribing from match changes")
        matchChangeListener.remove()
    }

    fun quitMatch(matchId: String) {
        val payload = hashMapOf(
            "matchId" to matchId
        )

        functions
            .getHttpsCallable("quitMatch")
            .call(payload)
            .continueWith { task ->
                Log.i(TAG, "Quit game $payload: ${task.result}")
            }
    }
}
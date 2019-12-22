package ch.zhaw.moba1.guesswho.model

import ch.zhaw.moba1.guesswho.Attribute
import ch.zhaw.moba1.guesswho.Character
import com.google.gson.Gson
import java.io.Serializable

class Round(
    var roundId: String,
    var matchId: String,
    var player1: String,
    var player2: String,
    var state: String,
    var countFaceUpPlayer1: Int,
    var countFaceUpPlayer2: Int,
    var turnToPlayer: String?,
    var characterPlayer1: Character?,
    var characterPlayer2: Character?,
    var questionsPlayer1: Array<Attribute>?,
    var questionsPlayer2: Array<Attribute>?,
    var guessPlayer1: Array<Guess>?,
    var guessPlayer2: Array<Guess>?
) : Serializable {

    companion object {
        fun of(roundId: String, map: Map<String, Any>?): Round {
            val gson = Gson()
            val round = gson.fromJson<Round>(gson.toJsonTree(map), Round::class.java)
            round.roundId = roundId
            return round
        }
    }

    fun isSelectCharacter(): Boolean {
        return this.state == "SELECT_CHARACTER"
    }

    fun isGameRunning(): Boolean {
        return !isGameEnd() && !isSelectCharacter()
    }

    fun isMyTurn(myPlayerId: String): Boolean {
        return this.turnToPlayer!! == myPlayerId
    }

    fun isQuestionAsk(): Boolean {
        return this.state == "GAME_QUESTION_ASK"
    }

    fun isQuestionAnswer(): Boolean {
        return this.state == "GAME_QUESTION_ANSWER"
    }

    fun isGamePass(): Boolean {
        return this.state == "GAME_PASS"
    }

    fun isGuessAsk(): Boolean {
        return this.state == "GAME_GUESS_ASK"
    }

    fun isGuessAnswer(): Boolean {
        return this.state == "GAME_GUESS_ANSWER"
    }

    fun isGameEnd(): Boolean {
        return this.state == "GAME_END"
    }

    fun getQuestion(myPlayerId: String): Attribute {
        val opponentsQuestions = getOpponentsQuestions(myPlayerId)
        return opponentsQuestions.last()
    }

    fun getMyQuestion(myPlayerId: String): Attribute {
        val myQuestions = getMyQuestions(myPlayerId)
        return myQuestions.last()
    }

    private fun getMyQuestions(myPlayerId: String): Array<Attribute> {
        return if (myPlayerId == player1) {
            questionsPlayer1!!
        } else {
            questionsPlayer2!!
        }
    }

    private fun getOpponentsQuestions(myPlayerId: String): Array<Attribute> {
        return if (myPlayerId == player1) {
            questionsPlayer2!!
        } else {
            questionsPlayer1!!
        }
    }

    fun getMyCharacter(myPlayerId: String): Character? {
        return if (myPlayerId == player1) {
            characterPlayer1
        } else {
            characterPlayer2
        }
    }

    fun getOtherCharacter(myPlayerId: String): Character? {
        return if (myPlayerId == player1) {
            characterPlayer2
        } else {
            characterPlayer1
        }
    }

    override fun toString(): String {
        return "Round(roundId='$roundId', matchId='$matchId', player1='$player1', player2='$player2', state='$state', turnToPlayer=$turnToPlayer, characterPlayer1=$characterPlayer1, characterPlayer2=$characterPlayer2, questionsPlayer1=${questionsPlayer1?.contentToString()}, questionsPlayer2=${questionsPlayer2?.contentToString()})"
    }

    fun getGuess(myPlayerId: String): Guess {
        return getOpponentsGuesses(myPlayerId).last()
    }

    private fun getOpponentsGuesses(myPlayerId: String): Array<Guess> {
        return if (myPlayerId == player1) {
            guessPlayer2!!
        } else {
            guessPlayer1!!
        }
    }

    fun getMyGuess(myPlayerId: String): Guess {
        return getMyGuesses(myPlayerId).last()
    }

    private fun getMyGuesses(myPlayerId: String): Array<Guess> {
        return if (myPlayerId == player1) {
            guessPlayer1!!
        } else {
            guessPlayer2!!
        }
    }

    fun getMyFaceUpCounter(myPlayerId: String): Int {
        return if (myPlayerId == player1) {
            countFaceUpPlayer1
        } else {
            countFaceUpPlayer2
        }
    }

    fun getOpponentFaceUpCounter(myPlayerId: String): Int {
        return if (myPlayerId == player1) {
            countFaceUpPlayer2
        } else {
            countFaceUpPlayer1
        }
    }
}
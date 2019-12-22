package ch.zhaw.moba1.guesswho.model

import com.google.gson.Gson
import java.io.Serializable

class Match(
    var matchId: String,
    var player1: String,
    var player2: String,
    var state: String,
    var rounds: Array<RoundRef>
) : Serializable {

    fun currentRound(): RoundRef {
        return rounds.last()
    }

    fun didIWinTheMatch(myPlayerId: String): Boolean {
        val winCount = getWinCount(myPlayerId)
        return winCount.myWins > winCount.opponentWins
    }

    fun getWinCount(myPlayerId: String): WinCounts {
        var myWins = 0
        var opponentWins = 0
        for (round in rounds.filter { !it.winner.isNullOrBlank() }) {
            if (round.winner == myPlayerId) {
                myWins++
            } else {
                opponentWins++
            }
        }
        return WinCounts(myWins, opponentWins)
    }

    fun isAbort(): Boolean {
        return state == "ABORT"
    }

    override fun toString(): String {
        return "Match(matchId='$matchId', player1='$player1', player2='$player2', state='$state', rounds=${rounds.contentToString()})"
    }

    companion object {

        fun of(matchId: String, map: Map<String, Any>?): Match {
            val gson = Gson()
            val match = gson.fromJson<Match>(gson.toJsonTree(map), Match::class.java)
            match.matchId = matchId
            return match
        }
    }
}

class RoundRef(val roundId: String, var winner: String?)

data class WinCounts(val myWins: Int, val opponentWins: Int) {
    fun isGameOver(maxWins: Int) = myWins == maxWins || opponentWins == maxWins
}
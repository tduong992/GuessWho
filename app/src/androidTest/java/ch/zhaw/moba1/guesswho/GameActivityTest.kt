package ch.zhaw.moba1.guesswho

import ch.zhaw.moba1.guesswho.model.Match
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Assert
import org.junit.Test

class GameActivityTest {

    @Test
    fun fetchMatch() {
        val matchId = "EPskaQ1CxRxj5U2Epe0i"
        val matchRef = FirebaseFirestore.getInstance()
            .collection("matches").document(matchId)

        val task = matchRef.get()
        while (!task.isComplete);

        val document = task.result
        val match = Match.of(matchId, document!!.data)
        Assert.assertNotNull(match)

    }
}
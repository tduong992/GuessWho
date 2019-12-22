package ch.zhaw.moba1.guesswho

import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import ch.zhaw.moba1.guesswho.model.Challenge
import com.google.firebase.functions.FirebaseFunctions
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.question_button_dialog_open.view.*


class PopupWindowChallenge(
    private val inflater: LayoutInflater,
    private val challenge: Challenge,
    private val onAcceptChallengeListener: (String) -> Unit
) {

    private val TAG = PopupWindowChallenge::class.java.name

    fun showPopupWindow(view: View) {
        // Create Popup View object through inflater & create Popup window
        val popupView = inflater.inflate(R.layout.question_button_dialog_open, null)
        val popupWindow = PopupWindow(
            popupView,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            true
        )

        // Set location of Popup window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        // Initialise elements of Popup window
        val characterCard = popupView.findViewById<CardView>(R.id.zoomed_character)
        Picasso.with(inflater.context).load(challenge.imgUrl).into(characterCard.character_image)
        characterCard.character_name.text = challenge.displayName

        val questionText = popupView.findViewById<TextView>(R.id.question_text)
        questionText.text = "Challenge accepted?"

        var answerText = ""
        val yesButton = popupView.findViewById<Button>(R.id.answer_yes)
        yesButton.setOnClickListener {
            answerText = "ACCEPTED"
            answerChallenge(true)
            onAcceptChallengeListener(challenge.matchId)
            popupWindow.dismiss()
        }

        val noButton = popupView.findViewById<Button>(R.id.answer_no)
        noButton.setOnClickListener {
            answerText = "REJECTED"
            answerChallenge(false)
            popupWindow.dismiss()
        }

        // Set a dismiss listener for Popup window
        popupWindow.setOnDismissListener {
            // Show answer as a Toast, for debugging
            Toast.makeText(view.context, answerText, Toast.LENGTH_SHORT).show()
        }
    }

    private fun answerChallenge(answer: Boolean) {
        val functions = FirebaseFunctions.getInstance()

        val payload = hashMapOf(
            "matchId" to challenge.matchId,
            "answer" to answer
        )
        functions
            .getHttpsCallable("answerChallenge")
            .call(payload)
    }
}

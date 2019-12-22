package ch.zhaw.moba1.guesswho

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import ch.zhaw.moba1.guesswho.model.Guess
import kotlinx.android.synthetic.main.question_button_dialog_open.view.*


class PopupWindowGuessQuestion(
    private val inflater: LayoutInflater,
    private val selectedCharacter: Character,
    private val guess: Guess
) {

    fun showPopupWindow(view: View, callback: (Guess) -> Unit) {
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
        characterCard.character_image.setImageResource(selectedCharacter.img)
        characterCard.character_name.text = selectedCharacter.name

        val questionText = popupView.findViewById<TextView>(R.id.question_text)
        questionText.text = "Is the character ${guess.name}?"

        var answerText = ""
        val yesButton = popupView.findViewById<Button>(R.id.answer_yes)
        yesButton.setOnClickListener {
            answerText = "Yes, it is ${guess.name}"
            callback(Guess(guess.name, true))
            popupWindow.dismiss()
        }

        val noButton = popupView.findViewById<Button>(R.id.answer_no)
        noButton.setOnClickListener {
            answerText = "No, it is not ${guess.name}"
            callback(Guess(guess.name, false))
            popupWindow.dismiss()
        }

        // Set a dismiss listener for Popup window
        popupWindow.setOnDismissListener {
            // Show answer as a Toast, for debugging
//            Toast.makeText(view.context, answerText, Toast.LENGTH_SHORT).show()
        }

    }
}

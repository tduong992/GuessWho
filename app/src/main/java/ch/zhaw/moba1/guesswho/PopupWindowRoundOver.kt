package ch.zhaw.moba1.guesswho

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.question_button_dialog_open.view.*


class PopupWindowRoundOver(
    private val inflater: LayoutInflater,
    private val otherPlayerCharacter: Character,
    private val didIWin: Boolean
) {

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

        // Hide yes/no buttons
        popupView.findViewById<Button>(R.id.answer_yes).visibility = View.GONE
        popupView.findViewById<Button>(R.id.answer_no).visibility = View.GONE

        // Initialise elements of Popup window
        val characterCard = popupView.findViewById<CardView>(R.id.zoomed_character)
        characterCard.character_image.setImageResource(otherPlayerCharacter.img)
        characterCard.character_name.text = otherPlayerCharacter.name

        val gameOverText = popupView.findViewById<TextView>(R.id.question_text)
        gameOverText.text = if (didIWin) "You won the round!\n\nNext round!\n" else "You lost the round!\n\nNext round!\n"

        // Set a dismiss listener for Popup window
        popupView.setOnTouchListener { v, event ->
            //Close the window when clicked
            popupWindow.dismiss()
            true
        }
    }
}

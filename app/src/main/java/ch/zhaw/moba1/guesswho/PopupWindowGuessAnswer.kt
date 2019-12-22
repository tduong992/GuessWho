package ch.zhaw.moba1.guesswho

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.constraintlayout.widget.ConstraintLayout
import ch.zhaw.moba1.guesswho.model.Guess


class PopupWindowGuessAnswer(
    private val inflater: LayoutInflater,
    private val guess: Guess
) {

    private lateinit var viewFlipperAnswerIcon: ViewFlipper

    private var popupWindow: PopupWindow? = null

    fun showPopupWindow(view: View) {
        // Create Popup View object through inflater & create Popup window
        val popupView = inflater.inflate(R.layout.question_button_dialog_open_answer, null)

        viewFlipperAnswerIcon = popupView.findViewById(R.id.answer_icon_view_flipper)

        popupWindow = PopupWindow(
            popupView,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set location of Popup window on the screen
        popupWindow!!.showAtLocation(view, Gravity.BOTTOM, 0, 0)


        val questionText = popupView.findViewById<TextView>(R.id.question_text)
        // Initialise elements of Popup window
        val answerIcon = popupView.findViewById<ImageView>(R.id.answer_icon)
        if (guess.answer == null) {
            questionText.text = "Is the character ${guess.name}?"
            viewFlipperAnswerIcon.displayedChild = 0
        } else if (guess.answer!!) {
            questionText.text = "Yes, it is ${guess.name}. You won!"
            viewFlipperAnswerIcon.displayedChild = 1
            answerIcon.setImageResource(R.drawable.check_green_90)
        } else {
            questionText.text = "No, it is not ${guess.name}."
            viewFlipperAnswerIcon.displayedChild = 1
            answerIcon.setImageResource(R.drawable.cross_red_90)
        }

        // Set a dismiss listener for Popup window
        popupView.setOnTouchListener { v, event ->
            //Close the window when clicked
            popupWindow?.dismiss()
            true
        }
    }

    fun dismiss() {
        popupWindow?.dismiss()
        popupWindow = null
    }
}

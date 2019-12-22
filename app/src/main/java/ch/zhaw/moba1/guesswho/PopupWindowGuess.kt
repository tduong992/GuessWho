package ch.zhaw.moba1.guesswho

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.constraintlayout.widget.ConstraintLayout


class PopupWindowGuess(private val inflater: LayoutInflater) {

    private lateinit var viewFlipperAnswerIcon: ViewFlipper

    fun showPopupWindow(view: View) {
        // Create Popup View object through inflater & create Popup window
        val popupView = inflater.inflate(R.layout.question_button_dialog_open_answer, null)

        viewFlipperAnswerIcon = popupView.findViewById(R.id.answer_icon_view_flipper)

        val popupWindow = PopupWindow(
            popupView,
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set location of Popup window on the screen
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0)

        // Initialise elements of Popup window
        viewFlipperAnswerIcon.displayedChild = 0
        val progressBar = popupView.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.visibility = View.GONE

        val questionText = popupView.findViewById<TextView>(R.id.question_text)
        questionText.text = "\n\nSelect your guessed character"

        // Set a dismiss listener for Popup window
        popupView.setOnTouchListener { v, event ->
            //Close the window when clicked
            popupWindow.dismiss()
            true
        }
    }
}

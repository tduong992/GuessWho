package ch.zhaw.moba1.guesswho

import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class PopupWindowChallengePending(
    private val inflater: LayoutInflater,
    private val matchId: String,
    private val onFriendAnswered: (String) -> Unit
) {

    private val TAG = PopupWindowChallengePending::class.java.name

    private lateinit var viewFlipperAnswerIcon: ViewFlipper

    private lateinit var matchChangeListener: ListenerRegistration

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
        progressBar.visibility = View.VISIBLE

        val questionText = popupView.findViewById<TextView>(R.id.question_text)
        questionText.text = "Waiting for your friend answer"

        waitOnFriend(popupView, popupWindow)
    }

    private fun waitOnFriend(popupView: View, popupWindow: PopupWindow) {
        val docRef = FirebaseFirestore.getInstance()
            .collection("matches").document(matchId)

        this.matchChangeListener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                val state = snapshot.data?.get("state") as String

                if (state == "ACCEPTED") {
                    viewFlipperAnswerIcon.displayedChild = 1
                    popupView.findViewById<ImageView>(R.id.answer_icon)
                        .setImageResource(R.drawable.check_green_90)
                    Handler().postDelayed({
                        onFriendAnswered(matchId)
                        popupWindow.dismiss()
                    }, 1500)
                    this.matchChangeListener.remove()
                } else if (state == "REJECTED") {
                    this.matchChangeListener.remove()
                    viewFlipperAnswerIcon.displayedChild = 1
                    popupView.findViewById<ImageView>(R.id.answer_icon)
                        .setImageResource(R.drawable.cross_red_90)
                    Handler().postDelayed({
                        popupWindow.dismiss()
                    }, 2000)
                }
            } else {
                Log.d(TAG, "Match was deleted!")
            }
        }
    }
}

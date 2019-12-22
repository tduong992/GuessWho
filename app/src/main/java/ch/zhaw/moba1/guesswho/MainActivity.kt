package ch.zhaw.moba1.guesswho

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import ch.zhaw.moba1.guesswho.game.GameActivity
import ch.zhaw.moba1.guesswho.messaging.MyFirebaseMessagingService
import ch.zhaw.moba1.guesswho.model.Challenge
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.name

    private lateinit var inflater: LayoutInflater

    private lateinit var viewFlipper: ViewFlipper

    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        inflater = LayoutInflater.from(this) as LayoutInflater

        viewFlipper = findViewById(R.id.view_flipper)

        // when button is clicked, show the alert
        val achievementsButton = findViewById<Button>(R.id.home_achievements_button)
        achievementsButton.setOnClickListener {
            // build alert dialog
            val dialogBuilder = AlertDialog.Builder(this)

            // set message of alert dialog
            dialogBuilder.setMessage("Get Guess Who Premium to see your achievements!")
                // if the dialog is cancelable: close dialog by modal mask click (true)
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton("Become Premium", DialogInterface.OnClickListener { dialog, id ->
                    finish()
                })
                // negative button text and action
                .setNegativeButton("CLOSE", DialogInterface.OnClickListener { dialog, id ->
                    dialog.cancel()
                })

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("Get Premium!")
            // show alert dialog
            alert.show()
        }

    }

    override fun onResume() {
        super.onResume()

        user = FirebaseAuth.getInstance().currentUser
        setPlayerEmail()
        if (user != null) {
            subscribeToUserNotifications()
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val challenger =
                        intent!!.getSerializableExtra(MyFirebaseMessagingService.INVITATION_INTENT) as Challenge
                    Log.i(TAG, "Received message: $challenger")
                    showChallengerPopup(challenger)
                }
            }, IntentFilter(MyFirebaseMessagingService.INVITATION_INTENT))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val challenger =
            intent.getSerializableExtra(MyFirebaseMessagingService.INVITATION_INTENT)

        if (challenger != null) {
            Log.i(TAG, "Received challenge!")
            showChallengerPopup(challenger as Challenge)
        }

    }

    fun showChallengerPopup(challenge: Challenge) {
        val view = findViewById<View>(R.id.main_activity_layout)
        val popupWindowChallenge =
            PopupWindowChallenge(inflater, challenge, this::onChallengeAccepted)
        popupWindowChallenge.showPopupWindow(view)
//        Snackbar.make(
//            view, "Received invitation to a match from $challenge",
//            Snackbar.LENGTH_LONG
//        ).setAction("Join") { Log.i(TAG, "Join") }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onSearchFriendClick(view: View) {
        viewFlipper.displayedChild = 1
    }

    fun onSearchFriendBackClick(view: View) {
        viewFlipper.displayedChild = 0
    }

    fun onPlayClick(view: View) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("iAmPlayer", user!!.uid)
        startActivity(intent)
    }

    private fun onChallengeAccepted(matchId: String) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("matchId", matchId)
        intent.putExtra("iAmPlayer", user!!.uid)
        startActivity(intent)
    }

    fun onSearchFriend(view: View) {
        val input = findViewById<TextInputEditText>(R.id.search_friend_input)
        val email = input.editableText.toString()

        Log.i(TAG, "Sending challenge to $email")

        val payload = hashMapOf("email" to email)

        val functions = FirebaseFunctions.getInstance()

        functions
            .getHttpsCallable("searchFriend")
            .call(payload)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as HashMap<String, String>
                val matchId = result["matchId"]!!
                Log.i(TAG, "Sent challenge, got matchId $matchId")

                Snackbar.make(
                    view, "Sent challenge to $email",
                    Snackbar.LENGTH_LONG
                ).show()

                val popupWindowChallengePending =
                    PopupWindowChallengePending(inflater, matchId, this::onChallengeAccepted)
                popupWindowChallengePending.showPopupWindow(main_activity_layout)

                result
            }
    }

    fun onLoginIntent(view: View) {

        if (user == null) {
            // Choose authentication providers
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )

            // Create and launch sign-in intent
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                RC_SIGN_IN
            )
        } else {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    val topic = user!!.email!!.replace("@", "")
                    unsubscribeToUserNotifications(topic)
                    user = null
                    setPlayerEmail()
                    Snackbar.make(view, "Logged out of 'Guess Who'!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                user = FirebaseAuth.getInstance().currentUser
                Log.i(TAG, "Authentication success: $user")

                setPlayerEmail()

                Snackbar.make(
                    findViewById(R.id.fab),
                    "Welcome to 'Guess Who'!",
                    Snackbar.LENGTH_LONG
                ).setAction("Action", null).show()

                subscribeToUserNotifications()
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                if (response == null) {
                    Log.i(TAG, "Login aborted")
                } else {
                    Log.e(TAG, "Login failed", response.error)
                }
            }
        }
    }

    private fun setPlayerEmail() {
        val textView = findViewById<TextView>(R.id.player_nr)
        textView.text = user?.email ?: "Please login"
    }

    private fun subscribeToUserNotifications() {
        val topic = user!!.email!!.replace("@", "")
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribed to $topic"
                if (!task.isSuccessful) {
                    msg = "Failed to subscribe!"
                }
                Log.d(TAG, msg)
            }
    }

    private fun unsubscribeToUserNotifications(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }
}

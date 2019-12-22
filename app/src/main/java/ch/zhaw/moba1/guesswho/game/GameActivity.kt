package ch.zhaw.moba1.guesswho.game

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.zhaw.moba1.guesswho.*
import ch.zhaw.moba1.guesswho.model.Guess
import ch.zhaw.moba1.guesswho.model.WinCounts
import kotlinx.android.synthetic.main.activity_game_container.*
import kotlinx.android.synthetic.main.question_button_dialog_open.view.*
import kotlin.math.min


class GameActivity : AppCompatActivity() {

    private val TAG = GameActivity::class.java.name

    private lateinit var gamePresenter: GamePresenter

    private lateinit var inflater: LayoutInflater

    private lateinit var viewFlipper: ViewFlipper

    private lateinit var recyclerView: RecyclerView

    private lateinit var viewAdapter: RecyclerView.Adapter<*>

    private lateinit var viewManager: RecyclerView.LayoutManager

    private lateinit var questionRecyclerViewDisabled: RecyclerView

    private lateinit var questionViewAdapterDisabled: RecyclerView.Adapter<*>

    private lateinit var questionViewManagerDisabled: RecyclerView.LayoutManager

    private lateinit var questionRecyclerView: RecyclerView

    private lateinit var questionViewAdapter: RecyclerView.Adapter<*>

    private lateinit var questionViewManager: RecyclerView.LayoutManager

    private var popupWindowAnswerPending: PopupWindowAnswer? = null

    private var popupWindowGuessAnswerPending: PopupWindowGuessAnswer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_container)

        val iAmPlayer = intent.getStringExtra("iAmPlayer")!!
        val matchId = intent.getStringExtra("matchId") ?: "ctfYkpuZ0ffs8i8Wlw8O"

        gamePresenter = GamePresenter(this, GameModel())
        gamePresenter.initGameView(matchId, iAmPlayer)

        viewFlipper = findViewById(R.id.view_flipper)

        inflater = LayoutInflater.from(this) as LayoutInflater

    }

    fun createCharacterListView(characters: MutableList<Character>) {
        viewManager = GridLayoutManager(this, 4)
        viewAdapter = CharacterAdapter(
            characters,
            this::onCharacterClick
        ) // get list directly from CharacterList.kt

        recyclerView = findViewById<RecyclerView>(R.id.face_grid_recycler_view).apply {
            // use this setting to improve performance if it is known that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify a viewAdapter
            adapter = viewAdapter
        }
    }

    fun createQuestionListViewDisabled(attributes: List<Attribute>) {
        questionViewManagerDisabled = GridLayoutManager(this, 4)
        questionViewAdapterDisabled =
            AttributeDisabledAdapter(attributes) // get list directly from AttributeList.kt

        questionRecyclerViewDisabled =
            findViewById<RecyclerView>(R.id.question_grid_recycler_view_disabled).apply {
                // use this setting to improve performance if it is known that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = questionViewManagerDisabled

                // specify a question viewAdapter
                adapter = questionViewAdapterDisabled
            }
    }

    fun createQuestionListView(attributes: List<Attribute>) {
        questionViewManager = GridLayoutManager(this, 4)
        questionViewAdapter = AttributeAdapter(
            attributes,
            this::onQuestionClick
        ) // get list directly from AttributeList.kt

        questionRecyclerView = findViewById<RecyclerView>(R.id.question_grid_recycler_view).apply {
            // use this setting to improve performance if it is known that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = questionViewManager

            // specify a question viewAdapter
            adapter = questionViewAdapter
        }
    }

    fun onQuitClick(view: View) {
        gamePresenter.quitMatch()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun onGuessClick(view: View) {
        // Create & show Popup window, displaying a hint to select the guessed character
        gamePresenter.enterGuessState()

        val popupViewGuess = PopupWindowGuess(inflater)
        popupViewGuess.showPopupWindow(game_question_buttons_container)
//        Toast.makeText(applicationContext, "Select the guessed character", Toast.LENGTH_SHORT).show()
    }

    fun onPassClick(view: View) {
        Toast.makeText(applicationContext, "Pass: game turn to opponent", Toast.LENGTH_SHORT).show()
        gamePresenter.passTurn()
    }

    private fun onCharacterClick(character: Character) {
        gamePresenter.onCharacterClick(character)
    }

    /**
     * Update list items change: notify to view adapter
     */
    fun notifyCharacterGridViewAdapter() {
        viewAdapter.notifyDataSetChanged()
    }

    /**
     * Update list items change: notify to view adapter
     */
    fun notifyAttributeGridViewAdapter() {
        questionViewAdapter.notifyDataSetChanged()
    }

    /**
     * Update list items change: notify to view adapter
     */
    fun notifyDisabledAttributeGridViewAdapter() {
        questionViewAdapterDisabled.notifyDataSetChanged()
    }

    fun setRecyclerViewAdapterDatasets(characters: List<Character>, attributes: List<Attribute>) {
        (viewAdapter as CharacterAdapter).setDataset(characters)
        (questionViewAdapter as AttributeAdapter).setDataset(attributes)
        (questionViewAdapterDisabled as AttributeDisabledAdapter).setDataset(attributes)
    }

    fun setSelectedCharacterOnGameScreen(character: Character?) {
        val selectedCharacterCard = findViewById<LinearLayout>(R.id.selected_character)
        val image = character?.img ?: R.drawable.icons8_face_64
        val name = character?.name ?: "NAME"

        selectedCharacterCard.character_image.setImageResource(image)
        selectedCharacterCard.character_name.text = name
    }

    fun showSelectCharacterHint() {
        viewFlipper.displayedChild = 0
//        Toast.makeText(applicationContext, "Select character", Toast.LENGTH_LONG).show()
    }

    fun showWaitingOnOpponentCharacterSelectionHint() {
        viewFlipper.displayedChild = 1
//        Toast.makeText(applicationContext, "Wait for opponent to select a character", Toast.LENGTH_LONG).show()
    }

    fun showWaitingOnOpponentAction() {
        viewFlipper.displayedChild = 2
        Toast.makeText(applicationContext, "Wait for opponent action", Toast.LENGTH_LONG).show()
    }

    fun showPlayerAction() {
        viewFlipper.displayedChild = 3
        Toast.makeText(applicationContext, "Your turn", Toast.LENGTH_SHORT).show()
    }

    private fun onQuestionClick(attribute: Attribute) {
        gamePresenter.onQuestionClick(attribute)
    }

    fun showPopupWindowAnswer(attribute: Attribute, isPositive: Boolean?) {
        popupWindowAnswerPending?.dismiss()
        popupWindowAnswerPending = PopupWindowAnswer(inflater, attribute, isPositive)
        popupWindowAnswerPending!!.showPopupWindow(game_question_buttons_container)
    }

    fun showPopupWindowQuestion(selectedCharacter: Character, attribute: Attribute) {
        val popupWindow = PopupWindowQuestion(inflater, selectedCharacter, attribute)
        popupWindow.showPopupWindow(game_container) {
            gamePresenter.answerQuestion(it)
        }
    }

    fun showPopupWindowGuessQuestion(selectedCharacter: Character, guess: Guess) {
        val popupWindow = PopupWindowGuessQuestion(inflater, selectedCharacter, guess)
        popupWindow.showPopupWindow(game_container) {
            gamePresenter.answerGuess(it)
        }
    }

    fun setTrophies(winCounts: WinCounts) {
        if (winCounts.myWins > 0) {
            val myTrophyLayout = findViewById<LinearLayout>(R.id.my_trophies)
            setTrophyWon(winCounts.myWins, myTrophyLayout)
        }

        if (winCounts.opponentWins > 0) {
            val opponentsTrophyLayout = findViewById<LinearLayout>(R.id.opponent_trophies)
            setTrophyWon(winCounts.opponentWins, opponentsTrophyLayout)
        }
    }

    private fun setTrophyWon(playerWins: Int, trophyLayout: LinearLayout) {
        val imageViews = trophyLayout.children.toList()
        for (i in 0 until min(playerWins, imageViews.size)) {
            val imageView = imageViews[i]
            if (imageView is ImageView) {
                imageView.setImageResource(R.drawable.icons8_trophy_cup_64)
            }
        }
    }

    fun showNextRoundToast() {
        Toast.makeText(applicationContext, "Another one!", Toast.LENGTH_LONG).show()
    }

    fun showPopupWindowGuessAnswer(guess: Guess) {
        popupWindowGuessAnswerPending?.dismiss()
        popupWindowGuessAnswerPending = PopupWindowGuessAnswer(inflater, guess)
        popupWindowGuessAnswerPending!!.showPopupWindow(game_container)
    }

    fun dismissPopupWindowGuessAnswer() {
        popupWindowGuessAnswerPending?.dismiss()
    }

    fun showPopupWindowRoundOver(otherPlayerCharacter: Character, didIWin: Boolean) {
        val popup = PopupWindowRoundOver(inflater, otherPlayerCharacter, didIWin)
        popup.showPopupWindow(game_container)
    }

    fun showPopupWindowGameOver(otherPlayerCharacter: Character, didIWin: Boolean) {
        val popup = PopupWindowGameOver(inflater, otherPlayerCharacter, didIWin)
        popup.showPopupWindow(game_container) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    fun setFaceUpCounters(myFaceUpCounter: Int, opponentFaceUpCounter: Int) {
        findViewById<TextView>(R.id.my_face_up_count).text = myFaceUpCounter.toString()
        findViewById<TextView>(R.id.opponent_face_up_count).text = opponentFaceUpCounter.toString()
    }

    fun opponentQuit() {
        Toast.makeText(applicationContext, "Opponent quit the match!", Toast.LENGTH_LONG).show()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

package ch.zhaw.moba1.guesswho

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.face_tile_card.view.*


class CharacterAdapter(private var dataset : List<Character>, private val clickListener: (Character) -> Unit) :
    RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder>() {

    /**
     * Provide a reference to the views for each data item.
     *
     * Complex data items may need more than one view per item,
     * provide access to all the view for a data item in a view holder.
     */
    class CharacterViewHolder(val linearView : LinearLayout) : RecyclerView.ViewHolder(linearView) {
        fun bind(character: Character, clickListener: (Character) -> Unit) {
            linearView.setOnClickListener {
                clickListener(character)
            }
        }
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        // create a new view
        val linearView = LayoutInflater.from(parent.context).inflate(R.layout.face_tile_card, parent, false) as LinearLayout

        return CharacterViewHolder(linearView)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val character = dataset[position]
        val displayImage = if (!character.isFaceDown) character.img else R.drawable.character_image_back
        holder.linearView.character_image.setImageResource(displayImage)
        holder.linearView.character_name.text = character.name
        holder.bind(character, clickListener)
    }

    /**
     * Return the size of dataset (invoked by the layout manager)
     */
    override fun getItemCount(): Int {
        return dataset.size
    }

    fun setDataset(newDataset: List<Character>) {
        dataset = newDataset
        notifyDataSetChanged()
    }
}

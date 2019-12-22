package ch.zhaw.moba1.guesswho

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.question_button.view.*

class AttributeAdapter(
    private var dataset: List<Attribute>,
    private val clickListener: (Attribute) -> Unit
) :
    RecyclerView.Adapter<AttributeAdapter.AttributeViewHolder>() {

    /**
     * Provide a reference to the views for each data item.
     *
     * Complex data items may need more than one view per item,
     * provide access to all the view for a data item in a view holder.
     */
    class AttributeViewHolder(val linearView: ConstraintLayout) :
        RecyclerView.ViewHolder(linearView)

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttributeViewHolder {
        // create a new view
        val linearView = LayoutInflater.from(parent.context).inflate(
            R.layout.question_button,
            parent,
            false
        ) as ConstraintLayout

        return AttributeViewHolder(linearView)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: AttributeViewHolder, position: Int) {
        val attribute = dataset[position]

        holder.linearView.attribute_image.isClickable = !attribute.disabled
        holder.linearView.attribute_image.alpha = if (!attribute.disabled) 1F else 0.4F
        holder.linearView.attribute_image.setImageResource(attribute.getImage())
        holder.linearView.attribute_image.setOnClickListener {
            clickListener(attribute)
        }
    }

    /**
     * Return the size of dataset (invoked by the layout manager)
     */
    override fun getItemCount(): Int {
        return dataset.size
    }

    fun setDataset(newDataset: List<Attribute>) {
        dataset = newDataset
        notifyDataSetChanged()
    }
}

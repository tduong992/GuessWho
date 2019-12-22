package ch.zhaw.moba1.guesswho

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.question_button.view.*

class AttributeDisabledAdapter(private var dataset : List<Attribute>) :
    RecyclerView.Adapter<AttributeDisabledAdapter.AttributeDisabledViewHolder>() {

    /**
     * Provide a reference to the views for each data item.
     *
     * Complex data items may need more than one view per item,
     * provide access to all the view for a data item in a view holder.
     */
    class AttributeDisabledViewHolder(val linearView : ConstraintLayout) : RecyclerView.ViewHolder(linearView)

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttributeDisabledViewHolder {
        // create a new view
        val linearView = LayoutInflater.from(parent.context).inflate(R.layout.question_button, parent, false) as ConstraintLayout

        return AttributeDisabledViewHolder(linearView)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: AttributeDisabledViewHolder, position: Int) {
        holder.linearView.attribute_image.isClickable = false
        holder.linearView.attribute_image.alpha = 0.4F
        holder.linearView.attribute_image.setImageResource(dataset[position].getImage())
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

package ubb.thesis.david.monumental.view.history

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_custom.view.*

class CustomizableViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val idLabel: TextView = view.label_id
    val topLabel: TextView = view.label_top
    val bottomLabel: TextView = view.label_bottom
    val actionButton: ImageView = view.button_action
}
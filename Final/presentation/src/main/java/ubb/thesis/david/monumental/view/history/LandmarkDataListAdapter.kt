package ubb.thesis.david.monumental.view.history

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.runOnUiThread
import ubb.thesis.david.domain.entities.Discovery
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.monumental.R
import java.text.SimpleDateFormat
import java.util.*

class LandmarkDataListAdapter(private val context: Context,
                              private var landmarks: Map<Landmark, Discovery?>,
                              private val onDownloadRequested: (photoId: String) -> Unit) :
    RecyclerView.Adapter<CustomizableViewHolder>() {

    private val onDownloadClicked = View.OnClickListener { view ->
        val photoId = view.tag.toString()
        onDownloadRequested(photoId)
    }

    override fun getItemCount(): Int = landmarks.size

    fun setItems(landmarks: Map<Landmark, Discovery?>) {
        this.landmarks = landmarks
        context.runOnUiThread { notifyDataSetChanged() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomizableViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_custom, parent, false)
        return CustomizableViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CustomizableViewHolder, position: Int) {
        val landmark = landmarks.keys.toTypedArray()[position]
        val discovery = landmarks[landmark]

        val discovered = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(discovery?.time)

        with(holder) {
            idLabel.text = "#${position + 1}"
            topLabel.text = landmark.label
            bottomLabel.text = discovered
            actionButton.imageResource = R.drawable.baseline_get_app_black_36dp
            actionButton.tag = discovery?.photoId
            actionButton.setOnClickListener(onDownloadClicked)
        }
    }

}
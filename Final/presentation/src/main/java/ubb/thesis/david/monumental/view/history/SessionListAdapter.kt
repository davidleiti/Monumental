package ubb.thesis.david.monumental.view.history

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.runOnUiThread
import ubb.thesis.david.domain.entities.Session
import ubb.thesis.david.monumental.R
import java.text.SimpleDateFormat
import java.util.*

class SessionListAdapter(private val context: Context,
                         private var sessions: List<Session>,
                         private val onDetailsRequested: (sessionId: String) -> Unit) :
    RecyclerView.Adapter<CustomizableViewHolder>() {

    private val onDetailButtonClicked = View.OnClickListener { view ->
        val sessionId = view.tag.toString()
        onDetailsRequested(sessionId)
    }

    override fun getItemCount() = sessions.size

    fun setItems(sessions: List<Session>) {
        this.sessions = sessions
        context.runOnUiThread { notifyDataSetChanged() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomizableViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_custom, parent, false)
        return CustomizableViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CustomizableViewHolder, position: Int) {
        val session = sessions[position]
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val startDate = dateFormat.format(session.timeStarted)
        val endDate = if (session.timeFinished != null)
            dateFormat.format(session.timeFinished)
        else
            holder.itemView.context.getString(R.string.label_now)

        with(holder) {
            idLabel.text = "#${position + 1}"
            topLabel.text = topLabel.context.getString(R.string.label_interval, startDate, endDate)
            bottomLabel.text = bottomLabel.context.getString(R.string.label_landmarks, session.landmarkCount)
            itemView.tag = session.sessionId
            itemView.setOnClickListener(onDetailButtonClicked)
        }
    }

}
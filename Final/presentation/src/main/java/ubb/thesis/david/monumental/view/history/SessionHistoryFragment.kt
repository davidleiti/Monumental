package ubb.thesis.david.monumental.view.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_session_history.*
import ubb.thesis.david.data.FirebaseDataAdapter
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.domain.entities.Session
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.BaseFragment
import ubb.thesis.david.monumental.common.TextDialog
import ubb.thesis.david.monumental.databinding.FragmentSessionHistoryBinding
import ubb.thesis.david.monumental.utils.getViewModel

class SessionHistoryFragment : BaseFragment() {

    private lateinit var viewModel: SessionHistoryViewModel
    private lateinit var listAdapter: SessionListAdapter

    override fun usesNavigationDrawer() = true

    override fun title(): String? = getString(R.string.title_session_history)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding: FragmentSessionHistoryBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_session_history, container, false)
        binding.lifecycleOwner = this

        viewModel = getViewModel { SessionHistoryViewModel(FirebaseDataAdapter()) }
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()

        viewModel.sessionsRetrieved.value ?: run {
            displayProgress()
            viewModel.fetchSessions(getUserId()!!)
        }
    }

    private fun observeData() {
        viewModel.sessionsRetrieved.observe(viewLifecycleOwner, Observer { sessions ->
            onSessionsRetrieved(sessions)
        })
        viewModel.errors.observe(viewLifecycleOwner, Observer { error ->
            onErrorOccurred(error)
        })
    }

    private fun onSessionsRetrieved(sessions: List<Session>) {
        info(TAG_LOG, "Retrieved ${sessions.size} sessions from the cloud successfully!")
        hideProgress()
        if (sessions.isNotEmpty()) {
            listAdapter = SessionListAdapter(sessions.sortedByDescending { it.timeStarted }) { sessionId ->
                Navigation.findNavController(view!!)
                        .navigate(SessionHistoryFragmentDirections.actionLoadDetails(sessionId))
            }

            list_sessions.adapter = listAdapter
            list_sessions.layoutManager = LinearLayoutManager(context!!)
        }
    }

    private fun onErrorOccurred(error: Throwable) {
        debug(TAG_LOG, "Failed to retrieve sessions from the cloud with error ${error.message}")
        TextDialog(context!!, getString(R.string.label_error), getString(R.string.message_error_operation))
                .also { dialog ->
                    dialog.updatePositiveButton(getString(R.string.label_ok)) {
                        Navigation.findNavController(view!!).navigateUp()
                    }
                    dialog.show()
                }
    }

    companion object {
        private const val TAG_LOG = "SessionHistoryViewLogger"
    }

}

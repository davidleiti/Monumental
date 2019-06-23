package ubb.thesis.david.monumental.view.setup


import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_results.*
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.BaseFragment
import ubb.thesis.david.monumental.common.TextDialog
import ubb.thesis.david.monumental.utils.getViewModel

class ResultFragment : BaseFragment(), View.OnClickListener {

    private lateinit var viewModel: ResultViewModel
    private lateinit var landmarksRetrieved: List<Landmark>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_results, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = getViewModel {
            ResultViewModel(getBeaconManager(), getDataSource())
        }
        observeData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_cancel.setOnClickListener(this)
        button_next.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (!::landmarksRetrieved.isInitialized)
            loadLandmarks()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_next -> setupSession()
            R.id.button_cancel -> navigateStart()
        }
    }

    override fun usesNavigationDrawer(): Boolean = true

    override fun title(): String? = getString(R.string.title_discovered)

    private fun observeData() {
        viewModel.foundLandmarks.observe(viewLifecycleOwner, Observer { landmarks ->
            info(TAG_LOG, "Landmarks retrieved successfully from the api.")
            landmarks.forEach { info(TAG_LOG, it.toString()) }

            landmarksRetrieved = landmarks
            hideProgress()
            updateUi()
        })
        viewModel.sessionCreated.observe(viewLifecycleOwner, Observer {
            info(TAG_LOG, "Session created successfully!")
            hideProgress()
            beginSession()
        })
        viewModel.errors.observe(viewLifecycleOwner, Observer {
            debug(TAG_LOG, "An unexpected error has occurred. See stacktrace in log")

            hideProgress()
            displayError()
        })
    }

    private fun loadLandmarks() {
        displayProgress()

        val args = ResultFragmentArgs.fromBundle(arguments!!)
        val limit = args.limit
        val radius = args.radius
        val categories = args.categories
        val lat = args.location.latitude
        val long = args.location.longitude

        viewModel.searchLandmarks(lat, long, radius, limit, categories)
    }

    private fun setupSession() {
        displayProgress()
        viewModel.setupSession(getUserId()!!, landmarksRetrieved)
    }

    private fun updateUi() {
        if (landmarksRetrieved.isEmpty()) {
            TextDialog(context!!, getString(R.string.message_oops), getString(R.string.message_no_landmarks))
                    .also { dialog ->
                        dialog.updatePositiveButton(getString(R.string.label_ok)) {
                            Navigation.findNavController(view!!).navigate(ResultFragmentDirections.actionRestartSetup())
                        }
                        dialog.show()
                    }
        } else {
            TransitionManager.beginDelayedTransition(container_fragment)
            label_ready.visibility = View.VISIBLE
            label_results.visibility = View.VISIBLE
            label_results.text = getString(R.string.venues_found, landmarksRetrieved.size)
        }
    }

    private fun displayError() =
        TextDialog(context!!, getString(R.string.label_error), getString(R.string.default_error_message))
                .also { dialog ->
                    dialog.updatePositiveButton(getString(R.string.label_retry)) {
                        loadLandmarks()
                    }
                    dialog.setupNegativeButton(getString(R.string.cancel)) {
                        navigateStart()
                    }
                }.show()

    private fun beginSession() =
        Navigation.findNavController(view!!).navigate(ResultFragmentDirections.actionBeginSession())

    private fun navigateStart() =
        Navigation.findNavController(view!!).popBackStack(R.id.startDestination, false)

    companion object {
        private const val TAG_LOG = "SetupLogger"
    }
}

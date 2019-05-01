package ubb.thesis.david.monumental.presentation.session.setup


import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_results.*
import ubb.thesis.david.monumental.R
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.monumental.presentation.common.BaseFragment
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.monumental.utils.getViewModel
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.monumental.utils.shortToast

class ResultFragment : BaseFragment(), View.OnClickListener {

    private val disposable = CompositeDisposable()
    private lateinit var viewModel: ResultViewModel
    private lateinit var landmarksRetrieved: List<ubb.thesis.david.domain.entities.Landmark>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel {
            ResultViewModel(getGeofencingClient())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_results, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_cancel.setOnClickListener(this)
        button_retry.setOnClickListener(this)
        button_next.setOnClickListener(this)
        observeData()
    }

    override fun onStart() {
        super.onStart()
        if (!::landmarksRetrieved.isInitialized)
            loadVenues()
    }

    override fun onStop() {
        super.onStop()
        hideLoading()
        disposable.dispose()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_next -> setupSession()
            R.id.button_cancel -> navigateStart()
            R.id.button_retry -> loadVenues()
        }
    }

    override fun usesNavigationDrawer(): Boolean = true

    private fun observeData() {
        viewModel.getVenuesObservable().observe(viewLifecycleOwner, Observer { venues ->
            info(TAG_LOG, "Landmarks successfully retrieved from the api.")
            venues.forEach { info(TAG_LOG, it.toString()) }
            landmarksRetrieved = venues
            hideLoading()
            displayResult()
        })
        viewModel.getErrorsObservable().observe(viewLifecycleOwner, Observer { errorMessage ->
            debug(TAG_LOG, "An error has occurred, cause: $errorMessage")
            hideLoading()
            context!!.shortToast(getString(R.string.default_error_message))
        })
        viewModel.getSessionCreatedObservable().subscribe {
            hideLoading()
            beginSession()
        }.also { disposable.add(it) }
    }

    private fun loadVenues() {
        showLoading()

        val args = ResultFragmentArgs.fromBundle(arguments!!)
        val limit = args.limit
        val radius = args.radius
        val categories = args.categories
        val location = "${args.location.latitude},${args.location.longitude}"

        viewModel.searchLandmarks(location, radius, limit, categories)
    }

    private fun setupSession() {
        showLoading()
        viewModel.setupSession(getUserId(), landmarksRetrieved)
    }

    private fun displayResult() {
        TransitionManager.beginDelayedTransition(container_fragment)
        label_ready.visibility = View.VISIBLE
        label_results.visibility = View.VISIBLE
        label_results.text = getString(R.string.venues_found, landmarksRetrieved.size)
    }

    private fun beginSession() =
        Navigation.findNavController(view!!).navigate(ResultFragmentDirections.actionBeginSession())

    private fun navigateStart() =
        Navigation.findNavController(view!!).popBackStack(R.id.startDestination, false)

    companion object {
        private const val TAG_LOG = "SetupLogger"
    }
}

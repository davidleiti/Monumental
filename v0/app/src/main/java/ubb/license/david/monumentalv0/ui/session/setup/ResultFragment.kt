package ubb.license.david.monumentalv0.ui.session.setup


import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_results.*
import ubb.license.david.monumentalv0.Injection
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.persistence.model.Landmark
import ubb.license.david.monumentalv0.ui.MainActivity
import ubb.license.david.monumentalv0.utils.getViewModel
import ubb.license.david.monumentalv0.utils.info
import ubb.license.david.monumentalv0.utils.shortToast
import ubb.license.david.monumentalv0.utils.warn

class ResultFragment : Fragment(), View.OnClickListener {

    private val logTag = "SessionSetup"

    private lateinit var mViewModel: ResultViewModel
    private lateinit var landmarksRetrieved: Array<Landmark>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = getViewModel()
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
        mViewModel.cancelRequests()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_next -> setupSession()
            R.id.button_cancel -> navigateStart()
            R.id.button_retry -> loadVenues()
        }
    }

    private fun observeData() {
        mViewModel.getVenuesObservable().observe(viewLifecycleOwner, Observer { venues ->
            info(logTag, "Venues successfully retrieved from the api. $venues")
            landmarksRetrieved = venues
            hideLoading()
            displayResult()
        })
        mViewModel.getErrorsObservable().observe(viewLifecycleOwner, Observer { errorMessage ->
            warn(logTag, "Failed to retrieve venues, cause: $errorMessage")
            hideLoading()
            context!!.shortToast("An error has occurred, please try again!")
        })
        mViewModel.getSessionIdObservable().observe(viewLifecycleOwner, Observer { sessionId ->
            info(logTag, "Session created with id $sessionId")
            hideLoading()
            beginSession(sessionId)
        })
    }

    private fun loadVenues() {
        showLoading()

        val args = ResultFragmentArgs.fromBundle(arguments!!)
        val limit = args.limit
        val radius = args.radius
        val categories = args.categories
        val location = "${args.location.latitude},${args.location.longitude}"

        if (limit > 0)
            mViewModel.searchLandmarks(location, radius, limit, categories)
        else
            mViewModel.searchLandmarks(location, radius, categories)
    }

    private fun setupSession() {
        showLoading()
        mViewModel.setupSession("dummyUser", "dummyCiy", landmarksRetrieved)
    }

    private fun displayResult() {
        TransitionManager.beginDelayedTransition(container_fragment)
        label_ready.visibility = View.VISIBLE
        label_results.visibility = View.VISIBLE
        label_results.text = getString(R.string.venues_found, landmarksRetrieved.size)
    }

    private fun showLoading() =
        (activity as? MainActivity)?.showLoading()

    private fun hideLoading() =
        (activity as? MainActivity)?.hideLoading()

    private fun beginSession(sessionId: Long) =
        ResultFragmentDirections.actionBeginSession(sessionId).run {
            Navigation.findNavController(button_next).navigate(this)
        }

    private fun navigateStart() =
        Navigation.findNavController(button_cancel).popBackStack(R.id.startDestination, false)
}

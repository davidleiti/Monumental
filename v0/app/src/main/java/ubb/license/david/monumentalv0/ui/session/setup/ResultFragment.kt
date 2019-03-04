package ubb.license.david.monumentalv0.ui.session.setup


import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_results.*
import ubb.license.david.foursquareapi.model.Venue
import ubb.license.david.monumentalv0.Injection
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.ui.MainActivity
import ubb.license.david.monumentalv0.utils.info
import ubb.license.david.monumentalv0.utils.shortSnack
import ubb.license.david.monumentalv0.utils.warn

class ResultFragment : Fragment(), View.OnClickListener {

    private val logTag = "SessionSetup"
    private var mErrorSnack: Snackbar? = null

    private lateinit var mDisposable: Disposable
    private lateinit var mViewModel: ResultViewModel
    private lateinit var venuesFound: Array<Venue>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = Injection.provideViewModelFactory(context!!).create(
            ResultViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_results, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_cancel.setOnClickListener(this)
        button_retry.setOnClickListener(this)
        button_next.setOnClickListener(this)
        listenToObservables()
        loadVenues()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_next -> setupSession()
            R.id.button_cancel -> navigateStart()
            R.id.button_retry -> loadVenues()
        }
    }

    private fun listenToObservables() {
        mViewModel.venuesObservable.observe(this, Observer { venues ->
            venuesFound = venues
            info(logTag, "Venues successfully retrieved from the api.")
            hideLoading()
            displayResult()
        })
        mViewModel.errorsObservable.observe(this, Observer { errorMessage ->
            warn(logTag, "Failed to retrieve venues, cause: $errorMessage")
            hideLoading()
            label_results.shortSnack("An error has occurred, please try again!")
        })
        mDisposable = mViewModel.sessionIdObservable.subscribe { sessionId ->
            info(logTag, "Session created with id $sessionId")
            hideLoading()
            beginSession(sessionId)
        }
    }

    private fun loadVenues() {
        showLoading()

        val args = ResultFragmentArgs.fromBundle(arguments!!)
        val limit = args.limit
        val radius = args.radius
        val categories = args.categories
        val location = "${args.location.latitude},${args.location.longitude}"

        if (limit > 0)
            mViewModel.searchVenuesLimited(location, radius, limit, categories)
        else
            mViewModel.searchVenues(location, radius, categories)
    }

    private fun setupSession() {
        showLoading()
        mViewModel.setupSession("dummyUser", "dummyCiy", venuesFound)
    }

    private fun displayResult() {
        TransitionManager.beginDelayedTransition(container_fragment)
        label_ready.visibility = View.VISIBLE
        label_results.visibility = View.VISIBLE
        label_results.text = getString(R.string.venues_found, venuesFound.size)
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

    override fun onStop() {
        super.onStop()
        mDisposable.dispose()
        mViewModel.cancelRequests()
        mErrorSnack?.dismiss()
    }
}

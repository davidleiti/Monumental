package ubb.license.david.monumentalv0.ui.session.setup


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_results.*
import ubb.license.david.foursquareapi.FoursquareApi
import ubb.license.david.foursquareapi.model.Venue
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.ui.ProgressOverlayActivity
import ubb.license.david.monumentalv0.utils.debug
import ubb.license.david.monumentalv0.utils.info

class ResultsFragment : Fragment(), View.OnClickListener {

    private val logTag = "SessionSetup"
    private var mErrorSnack: Snackbar? = null

    private lateinit var mDisposable: Disposable
    private lateinit var venuesFound: Array<Venue>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_results, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_cancel.setOnClickListener(this)
        button_retry.setOnClickListener(this)
        button_next.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        loadVenues()
    }

    override fun onStop() {
        super.onStop()
        mDisposable.dispose()
        mErrorSnack?.dismiss()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_next -> activity?.finish()
            R.id.button_cancel -> navigateStart()
            R.id.button_retry -> navigateDetails()
        }
    }

    private fun loadVenues() {
        showLoading()

        val args = ResultsFragmentArgs.fromBundle(arguments!!)
        val limit = args.limit
        val radius = args.radius
        val categories = args.categories
        val location = "${args.location.latitude},${args.location.longitude}"

        val searchObservable = if (limit > 0)
            FoursquareApi.Instance.searchVenuesLimited(location, radius, limit, categories)
        else
            FoursquareApi.Instance.searchVenues(location, radius, categories)

        mDisposable = searchObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ venues ->
                venuesFound = venues
                info(logTag, "Retrieved the following venues from the FourSquareApi: $venues")
                displayResult()
                hideLoading()
            }, {
                debug(logTag, "Error while loading venues, cause: ${it.message}")
                displayError()
                hideLoading()
            })
    }

    private fun displayResult() {
        label_results.text = getString(R.string.venues_found, venuesFound.size)
    }

    private fun displayError() {
        mErrorSnack = Snackbar.make(label_results, getString(R.string.warning_error_search), Snackbar.LENGTH_INDEFINITE)
            .setAction("Retry") { loadVenues() }
            .setActionTextColor(getColor(context!!, R.color.accent)).apply {
                show()
            }
    }

    private fun showLoading() {
        (activity as? ProgressOverlayActivity)?.showLoading()
    }

    private fun hideLoading() {
        (activity as? ProgressOverlayActivity)?.hideLoading()
    }

    private fun navigateDetails() {
        Navigation.findNavController(button_retry).popBackStack(R.id.detailsDestination, false)
    }

    private fun navigateStart() {
        Navigation.findNavController(button_cancel).popBackStack(R.id.startDestination, false)
    }
}

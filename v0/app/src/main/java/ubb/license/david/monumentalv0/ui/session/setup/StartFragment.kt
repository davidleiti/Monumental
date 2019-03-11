package ubb.license.david.monumentalv0.ui.session.setup


import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlinx.android.synthetic.main.fragment_start.*
import ubb.license.david.monumentalv0.GeofencingClientWrapper
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.persistence.model.Session
import ubb.license.david.monumentalv0.ui.BaseFragment
import ubb.license.david.monumentalv0.utils.debug
import ubb.license.david.monumentalv0.utils.getViewModel
import ubb.license.david.monumentalv0.utils.info
import ubb.license.david.monumentalv0.utils.shortSnack
import java.text.SimpleDateFormat


class StartFragment : BaseFragment(), View.OnClickListener {

    private var runningSession: Session? = null
    private lateinit var viewModel: StartViewModel
    private lateinit var fencingClient: GeofencingClientWrapper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_start, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_start_new.setOnClickListener(this)
        button_resume.setOnClickListener(this)
        enableUserNavigation()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fencingClient = getGeofencingClient()
        viewModel = getViewModel()
        observeData()
        checkRunningSession()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_start_new -> {
                runningSession?.let { wipeRunningSession() }
                requestGpsSettings(RC_SETUP_AFTER)
            }
            R.id.button_resume -> requestGpsSettings(RC_RESUME_AFTER)
        }
    }

    private fun observeData() {
        viewModel.getRunningSessionObservable().observe(viewLifecycleOwner, Observer {
            runningSession = it
            it?.let { updateUi() } ?: run { resetUi() }
            hideLoading()
        })
    }

    private fun checkRunningSession() {
        showLoading()
        viewModel.queryRunningSession(getUserId())
    }

    private fun updateUi() {
        TransitionManager.beginDelayedTransition(button_start_new)
        val date = SimpleDateFormat("dd/MM/yyyy", context!!.resources.configuration.locales[0])
            .format(runningSession?.timeStarted)
        label_start.text = getString(R.string.message_journey_resume, date)
        button_resume.visibility = View.VISIBLE
        button_start_new.setText(R.string.start_new)
    }

    private fun resetUi() {
        TransitionManager.beginDelayedTransition(button_start_new)
        label_start.text = getString(R.string.message_journey_start)
        button_resume.visibility = View.GONE
        button_start_new.setText(R.string.start)
    }

    private fun wipeRunningSession() {
        viewModel.wipeSessionData(getUserId())
        fencingClient.removeGeofences(getUserId())
    }

    private fun requestGpsSettings(requestCode: Int) {
        val enableLocationRequest: LocationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 30 * 1000
            fastestInterval = 5 * 1000
        }

        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(enableLocationRequest)
            .setAlwaysShow(true)
            .build()

        LocationServices.getSettingsClient(context!!)
            .checkLocationSettings(locationSettingsRequest)
            .apply {
                addOnSuccessListener { onLocationEnabled(requestCode) }
                addOnFailureListener(activity as Activity) { error ->
                    if (error is ResolvableApiException) {
                        try {
                            error.startResolutionForResult(activity!!, requestCode)
                        } catch (sendException: IntentSender.SendIntentException) {
                            // Ignore this error as suggested in the documentation
                        }
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            onLocationEnabled(requestCode)
        } else {
            view!!.shortSnack(getString(R.string.warning_gps_required))
        }
    }

    private fun onLocationEnabled(requestCode: Int) {
        if (requestCode == RC_RESUME_AFTER) {
            resumeSession()
        } else {
            setupSession()
        }
    }

    private fun setupSession() =
        Navigation.findNavController(activity!!, R.id.nav_host_fragment)
            .navigate(StartFragmentDirections.actionSetupSession())

    private fun resumeSession() =
        Navigation.findNavController(view!!)
            .navigate(StartFragmentDirections.actionResumeSession())

    companion object {
        private const val TAG_LOG = "SetupLogger"
        private const val RC_RESUME_AFTER = 10
        private const val RC_SETUP_AFTER = 20
    }
}

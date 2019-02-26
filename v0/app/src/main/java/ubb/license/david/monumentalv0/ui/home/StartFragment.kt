package ubb.license.david.monumentalv0.ui.home


import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_start.*
import ubb.license.david.monumentalv0.R



class StartFragment : Fragment(), View.OnClickListener {

    private val REQUEST_CHECK_SETTINGS = 1234
    private val mEnableLocationRequest: LocationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 30 * 1000
        fastestInterval = 5 * 1000
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_start, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_start.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_start -> {
                val locationSettingsRequest = LocationSettingsRequest.Builder()
                    .addLocationRequest(mEnableLocationRequest)
                    .setAlwaysShow(true)
                    .build()

                LocationServices.getSettingsClient(context!!)
                    .checkLocationSettings(locationSettingsRequest)
                    .apply {
                        addOnSuccessListener {
                            advance()
                        }
                        addOnFailureListener(activity as Activity) { error ->
                            if (error is ResolvableApiException)
                                try {
                                    // Not calling error.startResolutionForResult() because in the background
                                    // the dialog is launched by the activity with the given intent, and as such
                                    // the onActivityResult call is not propagated down from the activity to this fragment
                                    // unless explicitly called from the activity's onActivityResult()
                                    startIntentSenderForResult(
                                        error.resolution.intentSender, REQUEST_CHECK_SETTINGS,
                                        null, 0, 0, 0, null)
                                } catch (sendException: IntentSender.SendIntentException) {
                                    // Ignore the error
                                }
                        }
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CHECK_SETTINGS)
            if (resultCode == RESULT_OK)
                advance()
            else {
                Snackbar.make(button_start, "GPS access is required to move forward", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun advance() {
        val extras = FragmentNavigator.Extras.Builder().addSharedElement(header_start, "transition_header").build()
        Navigation.findNavController(activity!!, R.id.nav_host_fragment).navigate(StartFragmentDirections.actionAdvance(), extras)
    }
}

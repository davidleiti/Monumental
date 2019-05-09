package ubb.thesis.david.monumental.session.tracking


import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.lifecycle.Observer
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.android.synthetic.main.fragment_navigation.*
import ubb.thesis.david.data.navigation.FusedNavigator
import ubb.thesis.david.data.navigation.Navigator
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.LocationTrackerFragment
import ubb.thesis.david.monumental.utils.getViewModel
import ubb.thesis.david.monumental.utils.shortSnack

class NavigationFragment : LocationTrackerFragment() {

    private var currentDegree: Float = 0F
    private var navigator: Navigator? = null
    private val locationUpdateCallback: LocationCallback by lazy { createLocationCallback() }
    private val navigatorListener: Navigator.OnHeadingChangedListener by lazy { createNavigatorListener() }

    private var landmarks: List<Landmark>? = null
    private lateinit var viewModel: SessionViewModel

    override fun usesNavigationDrawer(): Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_navigation, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = getViewModel()
        observeData()
        showLoading()
        viewModel.loadSessionLandmarks(getUserId())
    }

    override fun onResume() {
        super.onResume()
        requestLocationUpdates(locationUpdateCallback)
    }

    override fun onPause() {
        super.onPause()
        disableLocationUpdates()
    }

    override fun createLocationRequest(): LocationRequest =
        LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10 * 1000
            fastestInterval = 5 * 1000
        }

    private fun createLocationCallback(): LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            navigator?.updateLocation(locationResult.lastLocation) ?: run {
                // TODO Display distance to target
                navigator = FusedNavigator(context!!, locationResult.lastLocation).apply {
                    setListener(navigatorListener)
                    target = defaultTarget()
                }
            }
        }
    }

    private fun createNavigatorListener(): Navigator.OnHeadingChangedListener =
        object : Navigator.OnHeadingChangedListener {
            override fun onChanged(direction: Float) {
                RotateAnimation(currentDegree, direction,
                                Animation.RELATIVE_TO_SELF, 0.5F,
                                Animation.RELATIVE_TO_SELF, 0.5F)
                        .also { anim ->
                            anim.duration = 500
                            anim.repeatCount = 0
                            anim.fillAfter = true
                            navigation_arrow?.startAnimation(anim)
                        }
                currentDegree = direction
            }
        }

    private fun observeData() {
        viewModel.getLandmarksObservable().observe(viewLifecycleOwner, Observer { landmarks ->
            this@NavigationFragment.landmarks = landmarks
            reinitializeBeaconsIfNeeded()
            hideLoading()
        })
        viewModel.getErrorsObservable().observe(viewLifecycleOwner, Observer { error ->
            debug(TAG_LOG, "The following error has occurred: $error")
            view?.shortSnack("An error has occurred!")
            hideLoading()
        })
    }

    private fun reinitializeBeaconsIfNeeded() {
        val sharedPrefs = context!!.getSharedPreferences(getUserId(), Context.MODE_PRIVATE)
        sharedPrefs?.let {
            for (landmark in landmarks!!) {
                if (!sharedPrefs.contains(landmark.id))
                    getBeaconManager().setupBeacon(landmark.id, landmark.lat, landmark.lng, getUserId())
            }
        }
    }

    private fun defaultTarget(): Location = Location("").apply {
        latitude = 46.777366
        longitude = 23.615983
    }

    companion object {
        private const val TAG_LOG = "SessionLogger"
    }
}

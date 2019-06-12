package ubb.thesis.david.monumental.session.tracking


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.android.synthetic.main.fragment_navigation.*
import ubb.thesis.david.data.navigation.FusedNavigator
import ubb.thesis.david.data.navigation.Navigator
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
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

    private lateinit var viewModel: SessionViewModel

    override fun usesNavigationDrawer(): Boolean = true

    override fun title(): String? = "Navigation"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_navigation, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = getViewModel()
        observeData()
        displayProgress()
        viewModel.loadSessionLandmarks(getUserId())

        button_take_photo.setOnClickListener {
            Navigation.findNavController(it)
                    .navigate(NavigationFragmentDirections.actionNavigateSnapshot(viewModel.nearestLandmark.value!!.id))
        }
    }

    override fun onStart() {
        super.onStart()
        navigator?.let {
            requestLocationUpdates(locationUpdateCallback)
        }
    }

    override fun onStop() {
        super.onStop()
        disableLocationUpdates()
    }

    override fun createLocationRequest(): LocationRequest =
        LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5 * 1000
            fastestInterval = 5 * 1000
        }

    private fun createLocationCallback(): LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            val location = locationResult.lastLocation
            info(TAG_LOG, "Location update received: " +
                    "Coordinates(lat: ${location.latitude}, long: ${location.longitude})")
            navigator?.updateLocation(location) ?: run {
                navigator = FusedNavigator(context!!, location).apply {
                    setListener(navigatorListener)
                }
            }
            viewModel.queryNearestLandmark(location)
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

    @SuppressLint("SetTextI18n")
    private fun observeData() {
        viewModel.sessionLandmarks.observe(viewLifecycleOwner, Observer { landmarks ->
            reinitializeBeaconsIfNeeded(landmarks)
            requestLocationUpdates(locationUpdateCallback)
            hideProgress()
        })
        viewModel.nearestLandmark.observe(viewLifecycleOwner, Observer { landmark ->
            navigator?.target = landmark.transformToLocation()
            button_take_photo.visibility = View.VISIBLE
            label_target.text = "Target: ${landmark.label}"
        })
        viewModel.distanceToTarget.observe(viewLifecycleOwner, Observer { distance ->
            label_distance.text = "Distance: $distance"
        })
        viewModel.errorMessages.observe(viewLifecycleOwner, Observer { error ->
            debug(TAG_LOG, "The following error has occurred: $error")
            view?.shortSnack("An error has occurred!")
            hideProgress()
        })
    }

    private fun reinitializeBeaconsIfNeeded(landmarks: List<Landmark>) {
        val sharedPrefs = context!!.getSharedPreferences(getUserId(), Context.MODE_PRIVATE)
        sharedPrefs?.let {
            for (landmark in landmarks) {
                if (!sharedPrefs.contains(landmark.id))
                    getBeaconManager().setupBeacon(landmark.id, landmark.lat, landmark.lng, getUserId())
            }
        }
    }

    companion object {
        private const val TAG_LOG = "SessionLogger"
    }
}

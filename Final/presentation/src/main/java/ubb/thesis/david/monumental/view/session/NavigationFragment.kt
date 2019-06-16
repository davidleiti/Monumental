package ubb.thesis.david.monumental.view.session


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
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
import ubb.thesis.david.data.FirebaseDataSource
import ubb.thesis.david.data.navigation.FusedNavigator
import ubb.thesis.david.data.navigation.Navigator
import ubb.thesis.david.data.utils.debug
import ubb.thesis.david.data.utils.info
import ubb.thesis.david.data.utils.toLocation
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.LocationTrackerFragment
import ubb.thesis.david.monumental.common.SimpleDialog
import ubb.thesis.david.monumental.geofencing.GeofencingClientAdapter
import ubb.thesis.david.monumental.utils.getViewModel

class NavigationFragment : LocationTrackerFragment() {

    private var currentDegree: Float = 0F
    private var navigator: Navigator? = null
    private val locationUpdateCallback: LocationCallback by lazy { createLocationCallback() }
    private val navigatorListener: Navigator.OnHeadingChangedListener by lazy { createNavigatorListener() }

    private lateinit var viewModel: NavigationViewModel

    override fun usesNavigationDrawer(): Boolean = true

    override fun title(): String? = "Navigation"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_navigation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = getViewModel { NavigationViewModel(FirebaseDataSource(), GeofencingClientAdapter(context!!)) }
        observeData()
        displayProgress()
        viewModel.loadSessionLandmarks(getUserId())

        button_take_photo.setOnClickListener { navigateToSnapshot() }
        button_save_progress.setOnClickListener {
            displayProgress()
            viewModel.saveSessionProgress(getUserId())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
                updateNavigationArrow(direction)
            }
        }

    private fun updateNavigationArrow(direction: Float) {
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

    @SuppressLint("SetTextI18n")
    private fun observeData() {
        viewModel.sessionLandmarks.observe(viewLifecycleOwner, Observer { landmarks ->
            onLandmarksRetrieved(landmarks)
        })
        viewModel.nearestLandmark.observe(viewLifecycleOwner, Observer { landmark ->
            onNearestRetrieved(landmark)
        })
        viewModel.distanceToTarget.observe(viewLifecycleOwner, Observer { distance ->
            onDistanceRetrieved(distance)
        })
        viewModel.progressSaved.observe(viewLifecycleOwner, Observer {
            onProgressSaved()
        })
        viewModel.errorsOccurred.observe(viewLifecycleOwner, Observer { error ->
            onErrorOccurred(error)
        })
    }

    private fun onLandmarksRetrieved(landmarks: List<Landmark>) {
        hideProgress()
        if (landmarks.isEmpty()) {
            onSessionFinished()
        } else {
            reinitializeBeaconsIfNeeded(landmarks)
            requestLocationUpdates(locationUpdateCallback)
            TransitionManager.beginDelayedTransition(container_fragment)
            label_remaining.visibility = View.VISIBLE
            label_remaining.text = getString(R.string.label_remaining, landmarks.size)
        }
    }

    private fun onNearestRetrieved(landmark: Landmark) {
        navigator?.target = landmark.toLocation()
        TransitionManager.beginDelayedTransition(container_fragment)
        navigation_arrow.visibility = View.VISIBLE
        button_take_photo.visibility = View.VISIBLE     //  TODO Remove this line after finishing below todo
        label_target.visibility = View.VISIBLE
        label_target.text = getString(R.string.label_target, landmark.label)
    }

    private fun onDistanceRetrieved(distance: Float) {
        TransitionManager.beginDelayedTransition(container_fragment)
        label_distance.visibility = View.VISIBLE
        label_distance.text = getString(R.string.label_distance, distance)
        // TODO Adjust button visibility based on the distance to target
    }

    private fun onProgressSaved() {
        hideProgress()
        SimpleDialog(context!!, getString(R.string.label_success),
                     getString(R.string.message_progress_saved)).also { dialog ->
            dialog.updatePositiveButton(getString(R.string.label_ok)) {
                Navigation.findNavController(view!!).navigateUp()
            }
            dialog.show()
        }
    }

    private fun onErrorOccurred(error: Throwable) {
        debug(TAG_LOG, "The following error has occurred: ${error.message}")
        hideProgress()
        SimpleDialog(context!!, getString(R.string.label_error), getString(R.string.message_error_operation))
                .also { dialog ->
                    dialog.updatePositiveButton(getString(R.string.label_ok)) {
                        Navigation.findNavController(view!!).navigateUp()
                    }
                    dialog.show()
                }
    }

    private fun onSessionFinished() {
        SimpleDialog(context!!, getString(R.string.label_good_job), getString(R.string.message_session_finished))
                .also { dialog ->
                    dialog.updatePositiveButton(getString(R.string.label_ok)) {
                        displayProgress()
                        viewModel.saveSessionProgress(getUserId())
                        viewModel.wipeSessionCache(getUserId())
                    }
                    dialog.show()
                }
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

    private fun navigateToSnapshot() {
        Navigation.findNavController(view!!)
                .navigate(NavigationFragmentDirections.actionNavigateSnapshot(viewModel.nearestLandmark.value!!))
    }

    companion object {
        private const val TAG_LOG = "SessionLogger"
    }
}

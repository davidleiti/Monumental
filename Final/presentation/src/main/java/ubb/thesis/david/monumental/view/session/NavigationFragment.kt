package ubb.thesis.david.monumental.view.session


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.databinding.DataBindingUtil
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
import ubb.thesis.david.data.utils.toLocation
import ubb.thesis.david.domain.entities.Landmark
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.LocationTrackerFragment
import ubb.thesis.david.monumental.common.TextDialog
import ubb.thesis.david.monumental.databinding.FragmentNavigationBinding
import ubb.thesis.david.monumental.utils.getViewModel

class NavigationFragment : LocationTrackerFragment() {

    private var currentDegree: Float = 0F
    private var navigator: Navigator? = null
    private val locationUpdateCallback: LocationCallback by lazy { createLocationCallback() }
    private val navigatorListener: Navigator.OnHeadingChangedListener by lazy { createNavigatorListener() }

    private lateinit var viewModel: NavigationViewModel

    override fun usesNavigationDrawer(): Boolean = true

    override fun title(): String? = getString(R.string.title_navigation)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentNavigationBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_navigation, container, false)
        binding.lifecycleOwner = this

        viewModel = getViewModel { NavigationViewModel(getDataSource(), getBeaconManager()) }
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_take_photo.setOnClickListener { navigateToSnapshot() }
        button_finish_session.setOnClickListener {
            displayProgress()
            viewModel.finishSession(getUserId()!!)
        }
        button_save_progress.setOnClickListener {
            displayProgress()
            viewModel.saveSessionProgress(getUserId()!!)
        }

        observeData()
        displayProgress()
        viewModel.loadSessionLandmarks(getUserId()!!)
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
        viewModel.sessionFinished.observe(viewLifecycleOwner, Observer {
            viewModel.wipeSessionCache(getUserId()!!)
            onTaskFinished(getString(R.string.label_success), getString(R.string.label_session_ended))
        })
        viewModel.progressSaved.observe(viewLifecycleOwner, Observer {
            onTaskFinished(getString(R.string.label_success), getString(R.string.message_progress_saved))
        })
        viewModel.errors.observe(viewLifecycleOwner, Observer { error ->
            onError(error)
        })
    }

    private fun onError(error: Throwable) {
        debug(TAG_LOG, "The following error has occurred: ${error.message}")
        hideProgress()
        TextDialog(context!!, getString(R.string.label_error), getString(R.string.message_error_operation)).show()
    }

    private fun onLandmarksRetrieved(landmarks: List<Landmark>) {
        hideProgress()
        if (landmarks.isEmpty()) {
            onLandmarksDiscovered()
        } else {
            reinitializeBeaconsIfNeeded(landmarks)
            requestLocationUpdates(locationUpdateCallback)
        }
    }

    private fun onNearestRetrieved(landmark: Landmark) {
        navigator?.target = landmark.toLocation()
    }

    private fun onTaskFinished(messageTitle: String, messageDescription: String) {
        hideProgress()
        TextDialog(context!!, messageTitle, messageDescription)
                .also { dialog ->
                    dialog.updatePositiveButton(getString(R.string.label_ok)) {
                        Navigation.findNavController(view!!).navigateUp()
                    }
                    dialog.show()
                }
    }

    private fun onLandmarksDiscovered() {
        TextDialog(context!!, getString(R.string.label_good_job), getString(R.string.message_session_finished))
                .also { dialog ->
                    dialog.updatePositiveButton(getString(R.string.label_ok)) {
                        displayProgress()
                        viewModel.finishSession(getUserId()!!)
                    }
                    dialog.show()
                }
    }

    private fun reinitializeBeaconsIfNeeded(landmarks: List<Landmark>) {
        val sharedPrefs = context!!.getSharedPreferences(getUserId()!!, Context.MODE_PRIVATE)
        sharedPrefs?.let {
            for (landmark in landmarks) {
                if (!sharedPrefs.contains(landmark.id))
                    getBeaconManager().setupBeacon(landmark.id, landmark.lat, landmark.lng, getUserId()!!)
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

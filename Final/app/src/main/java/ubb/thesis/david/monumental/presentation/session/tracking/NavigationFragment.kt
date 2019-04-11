package ubb.thesis.david.monumental.presentation.session.tracking


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ubb.thesis.david.monumental.data.geofencing.GeofencingClientWrapper
import ubb.thesis.david.monumental.domain.entities.Landmark
import ubb.thesis.david.monumental.presentation.common.BaseFragment
import ubb.thesis.david.monumental.utils.debug
import ubb.thesis.david.monumental.utils.getViewModel
import ubb.thesis.david.monumental.utils.shortSnack
import ubb.thesis.david.monumental.R

class NavigationFragment : BaseFragment(), OnMapReadyCallback {

    private var landmarks: List<Landmark>? = null
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var viewModel: SessionViewModel
    private lateinit var fencingClient: GeofencingClientWrapper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_navigation, container, false)
        mapView = root.findViewById(R.id.fences_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fencingClient = getGeofencingClient()
        viewModel = getViewModel()
        observeData()
        showLoading()

        viewModel.loadSessionLandmarks(getUserId())
    }

    override fun usesNavigationDrawer(): Boolean = true

    private fun observeData() {
        viewModel.getLandmarksObservable().observe(viewLifecycleOwner, Observer { landmarks ->
            initializeFencesIfNeeded(landmarks)
            this@NavigationFragment.landmarks = landmarks
            setupMarkers()
            hideLoading()
        })
        viewModel.getErrorsObservable().observe(viewLifecycleOwner, Observer { error ->
            debug(TAG_LOG, "The following error has occurred: $error")
            view?.shortSnack("An error has occurred!")
            hideLoading()
        })
    }

    override fun onMapReady(map: GoogleMap) {
        try {
            MapsInitializer.initialize(context)
        } catch (ex: GooglePlayServicesNotAvailableException) {
            debug("WahtevesLogger", "Failed to access google play services, cause: ${ex.message}")
        }
        googleMap = map
        landmarks?.run { setupMarkers() }
    }

    private fun setupMarkers() {
        val coords = LatLng(46.775910, 23.620980)
        for (landmark in landmarks!!) {
            val circleOptions = CircleOptions().apply {
                center(LatLng(landmark.lat, landmark.lng))
                radius(100.0)
                fillColor(context!!.getColor(R.color.primary_opaque))
                strokeColor(context!!.getColor(R.color.primary))
            }

            val markerOptions = MarkerOptions().apply {
                position(LatLng(landmark.lat, landmark.lng))
                title(landmark.label)
            }

            googleMap.addMarker(markerOptions)
            googleMap.addCircle(circleOptions)
        }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 16F))
    }

    private fun initializeFencesIfNeeded(landmarks: List<Landmark>) {
        val sharedPrefs = context!!.getSharedPreferences(getUserId(), Context.MODE_PRIVATE)
        sharedPrefs?.let {
            for (landmark in landmarks) {
                if (!sharedPrefs.contains(landmark.id))
                    createGeofence(landmark, sharedPrefs)
            }
        }
    }

    private fun createGeofence(landmark: Landmark, sharedPrefs: SharedPreferences) {
        fencingClient.createGeofence(landmark.id, landmark.lat, landmark.lng, sharedPrefs)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val TAG_LOG = "SessionLogger"
    }
}

package ubb.thesis.david.monumental.view.setup


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.navigation.Navigation
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.fragment_details.*
import ubb.thesis.david.monumental.R
import ubb.thesis.david.monumental.common.LocationTrackerFragment
import ubb.thesis.david.monumental.utils.*

class DetailsFragment : LocationTrackerFragment(), View.OnClickListener, OnMapReadyCallback,
    OnSuccessListener<Location> {

    private var locationCircle: Circle? = null
    private var lastLocation: LatLng? = null

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_details, container, false)

        mapView = root.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_decrease.setOnClickListener(this)
        button_increase.setOnClickListener(this)
        button_next.setOnClickListener(this)

        cb_limit.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) container_limit.expand()
            else container_limit.collapse()
        }

        sb_radius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val radius = (progress + 1) * RADIUS_FACTOR
                label_radius.text = "$radius m"
                updateMap()
            }
        })
    }

    override fun usesNavigationDrawer(): Boolean = true

    override fun title(): String? = null

    override fun createLocationRequest(): LocationRequest =
        LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
            interval = 15 * 1000
            fastestInterval = 15 * 1000
        }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_decrease -> decreaseLimit()
            R.id.button_increase -> increaseLimit()
            R.id.button_next -> advance()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setupMapUi()
        initLocation()
    }

    private fun initLocation() {
        if (context!!.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            with(googleMap) {
                isMyLocationEnabled = true
                setOnMyLocationButtonClickListener {
                    requestLastLocation(this@DetailsFragment)
                    false
                }
            }
            requestLastLocation(this)
        } else {
            button_next.visibility = View.GONE
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, RC_LOCATION_REQUEST)
        }
    }

    override fun onSuccess(location: Location?) {
        location?.let {
            lastLocation = LatLng(location.latitude, location.longitude)
            updateMap()
        } ?: run {
            lastLocation ?: run { requestLastLocation(this) }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == RC_LOCATION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocation()
                button_next.fadeIn()
            } else {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, RC_LOCATION_REQUEST)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun updateMap() {
        val center = lastLocation
        val radius = ((sb_radius.progress + 1) * RADIUS_FACTOR).toDouble()

        locationCircle?.remove()
        locationCircle = googleMap.addCircle(CircleOptions()
                                                     .center(center)
                                                     .radius(radius)
                                                     .fillColor(context!!.getColor(R.color.primary_opaque))
                                                     .strokeColor(context!!.getColor(R.color.primary)))

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, zoomValue()))
    }

    private fun zoomValue(): Float {
        val multiplier = sb_radius.progress + 1
        return 15f - multiplier * 0.5f
    }

    /**
     *  Enables the "My location" button and repositions it in the bottom right corner of the view
     */
    private fun setupMapUi() {
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        // Only way to retrieve the "My location" button, if crashing in the future revisit the provided ID
        val locationButton = mapView.findViewById<View>(Integer.parseInt("2"))
        val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)

        val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
        layoutParams.setMargins(margin, margin, margin, margin * 3)
    }

    private fun decreaseLimit() {
        val currentValue = field_limit.text.toString().toInt()

        if (currentValue > 5)
            field_limit.setText(Math.max(currentValue - 5, 5).toString())
    }

    private fun increaseLimit() {
        val currentValue = field_limit.text.toString().toInt()

        if (currentValue < 50)
            field_limit.setText(Math.min(currentValue + 5, 50).toString())
    }

    private fun advance() {
        lastLocation?.let { location ->
            val limitArg = if (cb_limit.isChecked) field_limit.text.toString().toInt() else 0
            val radiusArg = (sb_radius.progress + 1) * RADIUS_FACTOR

            val advanceAction = DetailsFragmentDirections.actionAdvance(
                    location).apply {
                limit = limitArg
                radius = radiusArg
            }

            Navigation.findNavController(view!!).navigate(advanceAction)
        } ?: run {
            mapView.shortSnack("Please wait until localization finishes.")
        }
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
        private const val RADIUS_FACTOR = 500
        private const val RC_LOCATION_REQUEST = 200
    }
}

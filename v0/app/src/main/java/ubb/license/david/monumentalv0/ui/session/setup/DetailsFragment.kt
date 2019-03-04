package ubb.license.david.monumentalv0.ui.session.setup


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
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_details.*
import ubb.license.david.monumentalv0.R
import ubb.license.david.monumentalv0.utils.*

class DetailsFragment : Fragment(), View.OnClickListener, OnMapReadyCallback, OnSuccessListener<Location> {

    private val radiusFactor = 500
    private val locationRequestCode = 200
    private val logTag = "SessionSetup"

    private var mLocationCircle: Circle? = null
    private var mLastLocation: LatLng? = null
    private var mPermissionSnack: Snackbar? = null

    private lateinit var mMapView: MapView
    private lateinit var mGoogleMap: GoogleMap

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_details, container, false)

        mMapView = root.findViewById(R.id.mapView)
        mMapView.onCreate(savedInstanceState)
        mMapView.getMapAsync(this)

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
                val radius = (progress + 1) * radiusFactor
                label_radius.text = "$radius m"
                updateMap()
            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_decrease -> decreaseLimit()
            R.id.button_increase -> increaseLimit()
            R.id.button_next -> advance()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        try {
            MapsInitializer.initialize(context)
        } catch (ex: GooglePlayServicesNotAvailableException) {
            debug(logTag, "Failed to access google play services, cause: ${ex.message}")
        }

        mGoogleMap = map
        setupMapUi()
        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (context!!.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            with(mGoogleMap) {
                isMyLocationEnabled = true
                setOnMyLocationButtonClickListener {
                    requestLocation()
                    false
                }
            }
            requestLocation()
        } else {
            button_next.visibility = View.GONE
            requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, locationRequestCode)
        }
    }

    private fun requestLocation() {
        if (context!!.checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            LocationServices.getFusedLocationProviderClient(activity!!).lastLocation
                .addOnSuccessListener(this)
        } else {
            requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, locationRequestCode)
        }
    }

    override fun onSuccess(location: Location?) {
        location?.let {
            mLastLocation = LatLng(location.latitude, location.longitude)
            updateMap()
        } ?: run {
            mLastLocation ?: run { requestLocation() }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            locationRequestCode -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                    button_next.fadeIn()
                } else {
                    mPermissionSnack = Snackbar.make(mMapView, "GPS permissions are required for localization!",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(context!!.getString(R.string.grant)) {
                            requestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, locationRequestCode)
                        }
                        .setActionTextColor(getColor(context!!, R.color.accent)).also { it.show() }
                }
            }
        }
    }

    private fun updateMap() {
        val center = mLastLocation
        val radius = ((sb_radius.progress + 1) * radiusFactor).toDouble()

        mLocationCircle?.remove()
        mLocationCircle = mGoogleMap.addCircle(CircleOptions()
            .center(center)
            .radius(radius)
            .fillColor(context!!.getColor(R.color.primary_opaque))
            .strokeColor(context!!.getColor(R.color.primary)))

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, zoomValue()))
    }

    private fun zoomValue(): Float {
        val multiplier = sb_radius.progress + 1
        return 15f - multiplier * 0.5f
    }

    /**
     *  Enables the "My location button and repositions it the bottom right corner of the view
     */
    private fun setupMapUi() {
        mGoogleMap.uiSettings.isCompassEnabled = false
        mGoogleMap.uiSettings.isMyLocationButtonEnabled = true

        // Only way to retrieve the "My location" button, if crashing in the future revisit the provided ID
        val locationButton = mMapView.findViewById<View>(Integer.parseInt("2"))
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
        mLastLocation?.let { location ->
            val limitArg = if (cb_limit.isChecked) field_limit.text.toString().toInt() else 0
            val radiusArg = (sb_radius.progress + 1) * radiusFactor

            val advanceAction = DetailsFragmentDirections.actionAdvance(
                location).apply {
                limit = limitArg
                radius = radiusArg
            }

            Navigation.findNavController(activity!!, R.id.nav_host_fragment).navigate(advanceAction)
        } ?: run {
            mMapView.shortSnack("Please wait until localization finishes.")
        }
    }

    override fun onStart() {
        super.onStart()
        mMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mMapView.onStop()
        mPermissionSnack?.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }
}

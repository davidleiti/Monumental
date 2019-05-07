package ubb.thesis.david.data.navigation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location

abstract class Navigator(context: Context,
                         protected var location: Location) : SensorEventListener {

    var target: Location? = null
        set(value) {
            field = value
            start()
        }

    protected val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    protected var onChangedListener: OnHeadingChangedListener? = null

    abstract fun start()

    abstract fun stop()

    fun updateLocation(location: Location) {
        this.location = location
    }

    fun setListener(listener: OnHeadingChangedListener?) {
        onChangedListener = listener
    }

    // Not used
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    interface OnHeadingChangedListener {
        fun onChanged(direction: Float)
    }
}
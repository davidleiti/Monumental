package ubb.thesis.david.navigationdemo

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location

abstract class Navigator(context: Context) : SensorEventListener {

    protected var target: Location? = null
    protected var location: Location? = null

    protected val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    protected var onChangedListener: OnHeadingChangedListener? = null

    abstract fun start()

    abstract fun stop()

    fun hasTarget(): Boolean = target != null && location != null

    fun setEndpoints(location: Location, target: Location) {
        this.location = location
        this.target = target
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
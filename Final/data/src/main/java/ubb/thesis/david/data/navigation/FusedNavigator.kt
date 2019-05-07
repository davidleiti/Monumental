package ubb.thesis.david.data.navigation

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.location.Location

class FusedNavigator(context: Context, location: Location) : Navigator(context, location) {

    private val accelerometer: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)
    private val rMat = FloatArray(9)
    private val iMat = FloatArray(9)

    override fun start() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun stop() = sensorManager.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.97f
        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]
            }

            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic[0] = alpha * geomagnetic[0] + (1 - alpha) * event.values[0]
                geomagnetic[1] = alpha * geomagnetic[1] + (1 - alpha) * event.values[1]
                geomagnetic[2] = alpha * geomagnetic[2] + (1 - alpha) * event.values[2]
            }

            val heading = calculateHeading() + getDeclination(location)

            if (target != null) {
                val relativeBearing = calculateRelativeBearing(heading)
                onChangedListener?.onChanged(relativeBearing)
            }
            onChangedListener?.onChanged(heading)
        }
    }

    private fun calculateHeading(): Float {
        val success = SensorManager.getRotationMatrix(rMat, iMat, gravity, geomagnetic)
        if (success) {
            val orientation = FloatArray(3)
            return ((Math.toDegrees(
                    SensorManager.getOrientation(rMat, orientation)[0].toDouble()) + 360) % 360).toFloat()
        }
        return -1F
    }

    private fun getDeclination(location: Location): Float =
        GeomagneticField(location.latitude.toFloat(),
                         location.longitude.toFloat(),
                         location.altitude.toFloat(),
                         System.currentTimeMillis()).declination

    private fun calculateRelativeBearing(heading: Float): Float {
        val relativeBearing = (location.bearingTo(target!!) + 360) % 360
        return (relativeBearing - heading + 360) % 360
    }
}
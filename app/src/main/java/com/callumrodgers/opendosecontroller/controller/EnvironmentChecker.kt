package com.callumrodgers.opendosecontroller.controller

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class EnvironmentChecker(val context: Context) {

    /**
     * Main manager for Android sensors.
     */
    private val sensorManager: SensorManager

    /**
     * The device's main light sensor, available in most phones due to screen brightness control.
     * Its data is combined with other collected information (such as device location) to determine
     * whether the user is potentially subject to exposure, and thus the limiters need to be
     * activated.
     */
    private val lightSensor: Sensor?
    private val restSensor: Sensor?
    private val motionSensor: Sensor?

    private val lightListener  = LightListener()
    private val restListener   = RestListener()
    private val motionListener = MotionListener()

    /**
     * Lightness of the device's surroundings, in lumen's.
     */
    private var lightness: Float = 0.0f

    /**
     * Whether the device is currently at motion rest for the last
     * 5 seconds.
     */
    private var isAtRest: Boolean = false

    init {
        // Getting the sensor manager.
        this.sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Initializing sensors.
        this.lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        this.restSensor =  sensorManager.getDefaultSensor(Sensor.TYPE_STATIONARY_DETECT)
        this.motionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MOTION_DETECT)

        // Final checker procedures.
        registerSensors()
    }

    private fun registerSensors() {
        sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(restListener, restSensor, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(motionListener, motionSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    private fun checkConditions() {

    }

    // ################ //
    // SENSOR LISTENERS //
    // ################ //

    /**
     * Base class for creating the application's sensor listeners.
     */
    abstract inner class SensorListener: SensorEventListener {

        private var isDataUseful: Boolean = false

        override fun onSensorChanged(event: SensorEvent?) {
            if (isDataUseful and (event != null)) {
                onUsefulDataUpdate(event!!)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            this.isDataUseful = (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_HIGH) or
                                (accuracy == SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM)
        }

        /**
         * Triggered when an event with high or medium quality data accuracy is passed.
         */
        abstract fun onUsefulDataUpdate(event: SensorEvent)
    }

    inner class LightListener: SensorListener() {
        override fun onUsefulDataUpdate(event: SensorEvent) {
            lightness = event.values[0]
            checkConditions()
        }
    }

    inner class RestListener: SensorListener() {
        override fun onUsefulDataUpdate(event: SensorEvent) {
            // We are not checking event.values[0] here because it always only returns 1.0
            // for this sensor. It basically means that the "true" condition is given when
            // this event is fired, and for the "false" condition we'll need to figure it out
            // ourselves with other sensors.
            isAtRest = true
            checkConditions()
        }
    }

    inner class MotionListener: SensorListener() {
        override fun onUsefulDataUpdate(event: SensorEvent) {
            // Likewise with the RestListener, actual event.values[0] is useless, we only care
            // that the event was triggered in the first place.
            isAtRest = false
            checkConditions()
        }
    }

}
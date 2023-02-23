/*
 * Copyright AziKar24 23/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ShakeDetector(val listener: OnShakeListener) : SensorEventListener {
    private val gravityThreshold = 3f
    private val timeThreshold = 1000
    private var mShakeTimestamp: Long = 0

    fun interface OnShakeListener {
        fun onShake()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // gForce will be close to 1 when there is no movement.
        val gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()
        if (gForce > gravityThreshold) {
            val now = System.currentTimeMillis()
            // ignore shake events too close to each other (500ms)
            if (mShakeTimestamp + timeThreshold > now) {
                return
            }
            mShakeTimestamp = now
            listener.onShake()
        }
    }
}
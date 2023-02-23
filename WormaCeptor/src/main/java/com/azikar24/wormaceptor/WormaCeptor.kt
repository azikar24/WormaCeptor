/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage
import com.azikar24.wormaceptor.internal.support.ShakeDetector
import com.azikar24.wormaceptor.internal.ui.WormaCeptorMainActivity
import java.util.*

object WormaCeptor {

    enum class WormaCeptorType {
        PERSISTENCE,
        IMDB
    }

    var storage: WormaCeptorStorage? = null
    var type: WormaCeptorType? = null

    fun getLaunchIntent(context: Context): Intent? {
        return Intent(context, WormaCeptorMainActivity::class.java)//.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun logUnexpectedCrashes() {
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
            val crashList = paramThrowable.stackTrace.toList()
            AsyncTask.execute {
                storage?.transactionDao?.insertCrash(
                    CrashTransaction.Builder().apply {
                        setThrowable(paramThrowable.toString())
                        setCrashList(crashList.map { it })
                        setCrashDate(Date())
                    }.build()
                )
            }

            if (oldHandler != null)
                oldHandler.uncaughtException(
                    paramThread,
                    paramThrowable
                )
            else
                Runtime.getRuntime().exit(2)
        }

    }

    fun addAppShortcut(context: Context): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val id = "${context.packageName}.wormaceptor_ui"
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            val shortcutInfo = ShortcutInfo.Builder(context, id)
                .setShortLabel(context.getString(R.string.app_name_2))
                .setLongLabel(context.getString(R.string.app_name_2)).apply {
                    getLaunchIntent(context)?.let {
                        setIntent(it.setAction(Intent.ACTION_VIEW))
                    }
                }
                .build()
            shortcutManager.addDynamicShortcuts(listOf(shortcutInfo).toMutableList())
            id
        } else {
            null
        }
    }

    fun startActivityOnShake(appCompatActivity: AppCompatActivity) {

        val mSensorManager = appCompatActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mShakeDetector = ShakeDetector {
            appCompatActivity.startActivity(getLaunchIntent(appCompatActivity))
        }

        appCompatActivity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI)
            }

            override fun onPause(owner: LifecycleOwner) {
                mSensorManager.unregisterListener(mShakeDetector)
                super.onPause(owner)
            }
        })
    }
}
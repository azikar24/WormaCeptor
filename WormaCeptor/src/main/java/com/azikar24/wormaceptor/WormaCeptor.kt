package com.azikar24.wormaceptor

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage
import com.azikar24.wormaceptor.internal.support.ShakeDetector
import com.azikar24.wormaceptor.internal.ui.mainactivity.WormaCeptorMainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import java.util.*

object WormaCeptor {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Storage instance used by the library.
     * Initialized via [init] and provided to internal components via DI.
     */
    internal var storage: WormaCeptorStorage? = null

    fun getLaunchIntent(context: Context): Intent {
        return Intent(
            context,
            WormaCeptorMainActivity::class.java
        )
    }

    /**
     * Initializes WormaCeptor with the provided [storage].
     * This must be called in your [android.app.Application.onCreate].
     */
    fun init(
        context: Context,
        storage: WormaCeptorStorage,
        appShortcut: Boolean = false,
        logCrashes: Boolean = false,
    ) {
        this.storage = storage

        // Register storage in Koin dynamically
        loadKoinModules(module {
            single { storage.transactionDao }
        })

        if (appShortcut) {
            addAppShortcut(context)
        }

        if (logCrashes) {
            logUnexpectedCrashes()
        }
    }

    private fun logUnexpectedCrashes() {
        val oldHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { paramThread, paramThrowable ->
            val crashList = mutableListOf<StackTraceElement>()
            var currentThrowable: Throwable? = paramThrowable

            // Collect stack trace including causes
            while (currentThrowable != null) {
                crashList.addAll(currentThrowable.stackTrace)
                currentThrowable = currentThrowable.cause
            }

            scope.launch {
                storage?.transactionDao?.insertCrash(
                    CrashTransaction(
                        throwable = paramThrowable.toString(),
                        crashList = crashList.toList(),
                        crashDate = Date(),
                    )
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

    private fun addAppShortcut(context: Context): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return null
        val id = "${context.packageName}.wormaceptor_ui"
        val shortcutInfo = ShortcutInfo.Builder(context, id)
            .setShortLabel(context.getString(R.string.app_name_2))
            .setIcon(Icon.createWithResource(context, R.drawable.ic_icon))
            .setLongLabel(context.getString(R.string.app_name_2)).apply {
                setIntent(getLaunchIntent(context).setAction(Intent.ACTION_VIEW))
            }
            .build()
        context.getSystemService(ShortcutManager::class.java)
            .addDynamicShortcuts(listOf(shortcutInfo).toMutableList())
        return id
    }

    fun startActivityOnShake(componentActivity: ComponentActivity) {

        val mSensorManager =
            componentActivity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val mShakeDetector = ShakeDetector {
            componentActivity.startActivity(getLaunchIntent(componentActivity))
        }

        componentActivity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                mSensorManager.registerListener(
                    mShakeDetector,
                    mAccelerometer,
                    SensorManager.SENSOR_DELAY_UI
                )
            }

            override fun onPause(owner: LifecycleOwner) {
                mSensorManager.unregisterListener(mShakeDetector)
                super.onPause(owner)
            }
        })
    }
}

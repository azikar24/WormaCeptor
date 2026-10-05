package com.azikar24.wormaceptor.api.internal

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process

/** True when running in the app's default process; false for `android:process` services or an unknown name. */
internal fun isMainProcess(context: Context): Boolean =
    isMainProcess(currentProcessName(context), context.applicationInfo.processName)

internal fun isMainProcess(
    processName: String?,
    mainProcessName: String,
): Boolean = processName == mainProcessName

private fun currentProcessName(context: Context): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) return Application.getProcessName()
    val pid = Process.myPid()
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    return activityManager?.runningAppProcesses?.firstOrNull { it.pid == pid }?.processName
}

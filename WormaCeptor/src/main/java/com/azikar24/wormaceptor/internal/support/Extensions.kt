/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.azikar24.wormaceptor.WormaCeptor
import java.text.SimpleDateFormat
import java.util.*

fun Context.getColorFromRes(@ColorRes id: Int): Int {
    return ContextCompat.getColor(this, id)
}

fun Context.share(content: String) {
    val sendIntent = Intent()
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(Intent.EXTRA_SUBJECT, content)
    sendIntent.putExtra(Intent.EXTRA_TEXT, content)
    sendIntent.type = "text/plain"
    startActivity(Intent.createChooser(sendIntent, null))
}

fun Date.formatted(): String? {
    return SimpleDateFormat("dd/MMM/yyyy - HH:mm:ss", Locale.US).format(this)
}

fun Activity.getApplicationName(): String {
    val applicationInfo = applicationInfo
    val stringId = applicationInfo?.labelRes
    return if (stringId == 0 || stringId == null) applicationInfo?.nonLocalizedLabel.toString() else getString(stringId)
}

@SuppressLint("HardwareIds")
fun Context.getSystemDetail(): String {
    applicationInfo.packageName
    return """
            Manufacture: ${Build.MANUFACTURER} 
            Brand: ${Build.BRAND} 
            DeviceID: ${Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)}
            Model: ${Build.MODEL}
            ID: ${Build.ID} 
            SDK: ${Build.VERSION.SDK_INT} 
            Board: ${Build.BOARD} 
            Version Code: ${Build.VERSION.RELEASE}
            
            
            
            
            Extras Info: ${WormaCeptor.reportCrashesExtras}
        """.trimIndent()
}
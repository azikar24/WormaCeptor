package com.azikar24.wormaceptor.feature.viewer.ui.util

import android.content.Context
import com.azikar24.wormaceptor.feature.viewer.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

internal fun formatRelativeTime(
    context: Context,
    timestamp: Long,
): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> context.getString(R.string.viewer_crash_list_just_now)
        diff < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
            context.getString(R.string.viewer_crash_list_min_ago, minutes)
        }
        diff < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diff).toInt()
            context.getString(R.string.viewer_crash_list_hr_ago, hours)
        }
        diff < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diff).toInt()
            if (days > 1) {
                context.getString(R.string.viewer_crash_list_days_ago, days)
            } else {
                context.getString(R.string.viewer_crash_list_day_ago, days)
            }
        }
        else -> {
            SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
    }
}

internal fun isSevereException(exceptionType: String): Boolean {
    val severeTypes = listOf(
        "NullPointerException",
        "OutOfMemoryError",
        "StackOverflowError",
        "SecurityException",
        "IllegalStateException",
        "AssertionError",
    )
    return severeTypes.any { exceptionType.contains(it, ignoreCase = true) }
}

package com.azikar24.wormaceptor.feature.viewer

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

internal class FormatCrashRelativeTimeUseCase(private val context: Context) {

    operator fun invoke(
        timestamp: Long,
        now: Long = System.currentTimeMillis(),
    ): String {
        val diff = now - timestamp
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) ->
                context.getString(R.string.viewer_crash_list_just_now)
            diff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diff).toInt()
                context.getString(R.string.viewer_crash_list_min_ago, minutes)
            }
            diff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diff).toInt()
                context.getString(R.string.viewer_crash_list_hr_ago, hours)
            }
            diff < TimeUnit.DAYS.toMillis(DAYS_IN_WEEK) -> {
                val days = TimeUnit.MILLISECONDS.toDays(diff).toInt()
                if (days > 1) {
                    context.getString(R.string.viewer_crash_list_days_ago, days)
                } else {
                    context.getString(R.string.viewer_crash_list_day_ago, days)
                }
            }
            else -> SimpleDateFormat(ABSOLUTE_PATTERN, Locale.getDefault()).format(Date(timestamp))
        }
    }

    companion object {
        private const val DAYS_IN_WEEK = 7L
        private const val ABSOLUTE_PATTERN = "MMM d, HH:mm"
    }
}

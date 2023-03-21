/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.content.Context
import android.content.SharedPreferences
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.WormaCeptorInterceptor
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage
import java.util.*
import java.util.concurrent.TimeUnit

class RetentionManager(val context: Context, period: WormaCeptorInterceptor.Period) {
    private val PREFS_NAME = "wormaceptor_preferences"
    private val KEY_LAST_CLEANUP = "last_cleanup"
    private var mPrefs: SharedPreferences? = null

    private val storage: WormaCeptorStorage? = WormaCeptor.storage
    private val mPeriod = toMillis(period)
    private var mCleanupFrequency: Long = 0

    init {
        mPrefs = context.getSharedPreferences(PREFS_NAME, 0)
        mCleanupFrequency = if (period == WormaCeptorInterceptor.Period.ONE_HOUR) TimeUnit.MINUTES.toMillis(30) else TimeUnit.HOURS.toMillis(2)
    }

    private fun getThreshold(now: Long): Long {
        return if (mPeriod == 0L) now else now - mPeriod
    }

    private fun getLastCleanup(fallback: Long): Long {
        if (LAST_CLEAN_UP == 0L) {
            mPrefs?.getLong(KEY_LAST_CLEANUP, fallback)?.let {
                LAST_CLEAN_UP = it
            }
        }
        return LAST_CLEAN_UP
    }

    private fun updateLastCleanup(time: Long) {
        LAST_CLEAN_UP = time
        mPrefs?.edit()?.putLong(KEY_LAST_CLEANUP, time)?.apply()
    }

    private fun deleteSince(threshold: Long) {
        val rows: Long = storage?.transactionDao?.deleteTransactionsBefore(Date(threshold))?.toLong() ?: -1L
        Logger.i("$rows transactions deleted")
    }

    private fun isCleanupDue(now: Long): Boolean {
        return now - getLastCleanup(now) > mCleanupFrequency
    }

    @Synchronized
    fun doMaintenance() {
        if (mPeriod > 0) {
            val now = Date().time
            if (isCleanupDue(now)) {
                Logger.i("Performing data retention maintenance...")
                deleteSince(getThreshold(now))
                updateLastCleanup(now)
            }
        }
    }

    private fun toMillis(period: WormaCeptorInterceptor.Period): Long {
        return when (period) {
            WormaCeptorInterceptor.Period.ONE_HOUR -> TimeUnit.HOURS.toMillis(1)
            WormaCeptorInterceptor.Period.ONE_DAY -> TimeUnit.DAYS.toMillis(1)
            WormaCeptorInterceptor.Period.ONE_WEEK -> TimeUnit.DAYS.toMillis(7)
            else -> 0
        }
    }

    companion object {
        var LAST_CLEAN_UP: Long = 0L
    }
}
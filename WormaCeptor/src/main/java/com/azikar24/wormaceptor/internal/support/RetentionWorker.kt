package com.azikar24.wormaceptor.internal.support

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.WormaCeptorInterceptor
import java.util.*
import java.util.concurrent.TimeUnit

class RetentionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val storage = WormaCeptor.storage ?: return Result.success()

        val prefs =
            applicationContext.getSharedPreferences("wormaceptor_preferences", Context.MODE_PRIVATE)
        val periodName =
            prefs.getString("retention_period", WormaCeptorInterceptor.Period.ONE_WEEK.name)
        val period = try {
            WormaCeptorInterceptor.Period.valueOf(
                periodName ?: WormaCeptorInterceptor.Period.ONE_WEEK.name
            )
        } catch (_: Exception) {
            WormaCeptorInterceptor.Period.ONE_WEEK
        }

        if (period == WormaCeptorInterceptor.Period.FOREVER) {
            return Result.success()
        }

        val retentionMillis = when (period) {
            WormaCeptorInterceptor.Period.ONE_HOUR -> TimeUnit.HOURS.toMillis(1)
            WormaCeptorInterceptor.Period.ONE_DAY -> TimeUnit.DAYS.toMillis(1)
            WormaCeptorInterceptor.Period.ONE_WEEK -> TimeUnit.DAYS.toMillis(7)
            else -> 0L
        }

        if (retentionMillis > 0) {
            val threshold = System.currentTimeMillis() - retentionMillis
            try {
                val rows = storage.transactionDao?.deleteTransactionsBefore(Date(threshold)) ?: 0
                Logger.i("RetentionWorker: $rows transactions deleted")
            } catch (e: Exception) {
                Logger.e("RetentionWorker: Failed to delete old transactions", e)
                return Result.retry()
            }
        }

        return Result.success()
    }
}

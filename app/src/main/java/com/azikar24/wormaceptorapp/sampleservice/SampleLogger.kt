package com.azikar24.wormaceptorapp.sampleservice

import android.util.Log

object SampleLogger {

    private const val TAG_BOOT = "DemoBoot"
    private const val TAG_NETWORK = "DemoNetwork"
    private const val TAG_DB = "DemoDb"
    private const val TAG_AUTH = "DemoAuth"
    private const val SLOW_QUERY_MS = 248
    private const val LARGE_PAYLOAD_REPEATS = 64

    fun emitSamples() {
        Log.v(TAG_BOOT, "verbose: feature flag cache hit")
        Log.d(TAG_BOOT, "debug: cold-start completed in 412ms")
        Log.i(TAG_NETWORK, "info: enqueued 5 retrofit calls")
        Log.w(TAG_DB, "warn: query took ${SLOW_QUERY_MS}ms — consider an index")
        Log.e(TAG_AUTH, "error: token refresh failed", IllegalStateException("simulated 401 from upstream"))

        Log.i(
            TAG_NETWORK,
            buildString {
                append("info: large payload — ")
                repeat(LARGE_PAYLOAD_REPEATS) { append("payload-chunk-").append(it).append(' ') }
            },
        )
    }
}

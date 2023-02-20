/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.util.Log

object Logger {
    private const val LOG_TAG = "WormaCeptorInterceptor"
    fun i(message: String?) {
        message?.let { Log.i(LOG_TAG, it) }
    }

    fun e(message: String?, e: Exception?) {
        Log.e(LOG_TAG, message, e)
    }
}
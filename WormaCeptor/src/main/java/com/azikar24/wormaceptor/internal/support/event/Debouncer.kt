/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support.event

import android.os.Handler
import android.os.Looper

class Debouncer<V>(private val mInterval: Int, private val mCallback: Callback<V>) {
    private val mHandler: Handler = Handler(Looper.getMainLooper())

    fun consume(event: V) {
        mHandler.removeCallbacksAndMessages(null)
        mHandler.postDelayed(Counter(event, mCallback), mInterval.toLong())
    }

    private class Counter<T> constructor(private val mEvent: T, private val mCallback: Callback<T>) : Runnable {
        override fun run() {
            mCallback.onEmit(mEvent)
        }
    }
}
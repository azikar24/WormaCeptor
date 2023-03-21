/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support.event

import android.os.Handler
import android.os.Looper

class Sampler<V>(private val mInterval: Int, private val mCallback: Callback<V>) {

    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private var currentRunnable: Counter<V>? = null

    fun consume(event: V) {
        if (currentRunnable == null) {
            currentRunnable = Counter(event, mCallback).apply {
                mHandler.postDelayed(this, mInterval.toLong())
            }
        } else {
            if (currentRunnable?.state == Counter.STATE_CREATED || currentRunnable?.state == Counter.STATE_QUEUED) {
                currentRunnable?.updateEvent(event)
            } else if (currentRunnable?.state == Counter.STATE_RUNNING || currentRunnable?.state == Counter.STATE_FINISHED) {
                currentRunnable = Counter(event, mCallback).apply {
                    mHandler.postDelayed(this, mInterval.toLong())
                }
            }
        }
    }

    private class Counter<T> constructor(private var mEvent: T, private val mCallback: Callback<T>) : Runnable {
        var state: Int

        init {
            state = STATE_CREATED
        }

        fun updateEvent(deliverable: T) {
            mEvent = deliverable
        }

        override fun run() {
            state = STATE_RUNNING
            mCallback.onEmit(mEvent)
            state = STATE_FINISHED
        }

        companion object {
            const val STATE_CREATED = 1
            const val STATE_QUEUED = 2
            const val STATE_RUNNING = 3
            const val STATE_FINISHED = 4
        }
    }
}

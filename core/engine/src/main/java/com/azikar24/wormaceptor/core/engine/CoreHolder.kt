package com.azikar24.wormaceptor.core.engine

import java.util.concurrent.atomic.AtomicReference

object CoreHolder {
    private val engines = AtomicReference<Pair<CaptureEngine, QueryEngine>?>(null)

    val captureEngine: CaptureEngine?
        get() = engines.get()?.first

    val queryEngine: QueryEngine?
        get() = engines.get()?.second

    fun initialize(capture: CaptureEngine, query: QueryEngine): Boolean {
        return engines.compareAndSet(null, capture to query)
    }
}

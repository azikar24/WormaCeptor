package com.azikar24.wormaceptor.core.engine

import java.util.concurrent.atomic.AtomicReference

object CoreHolder {
    private val engines = AtomicReference<Triple<CaptureEngine, QueryEngine, ExtensionRegistry?>?>(null)

    val captureEngine: CaptureEngine?
        get() = engines.get()?.first

    val queryEngine: QueryEngine?
        get() = engines.get()?.second

    val extensionRegistry: ExtensionRegistry?
        get() = engines.get()?.third

    fun initialize(capture: CaptureEngine, query: QueryEngine, extensions: ExtensionRegistry? = null): Boolean {
        return engines.compareAndSet(null, Triple(capture, query, extensions))
    }
}

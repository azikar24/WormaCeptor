package com.azikar24.wormaceptor.core.engine

import java.util.concurrent.atomic.AtomicReference

/** Thread-safe singleton that holds references to the core engine instances. */
object CoreHolder {
    private val engines = AtomicReference<Triple<CaptureEngine, QueryEngine, ExtensionRegistry?>?>(null)

    /** The capture engine used to record network transactions. */
    val captureEngine: CaptureEngine?
        get() = engines.get()?.first

    /** The query engine used to retrieve stored transactions and crashes. */
    val queryEngine: QueryEngine?
        get() = engines.get()?.second

    /** The optional extension registry for custom transaction metadata. */
    val extensionRegistry: ExtensionRegistry?
        get() = engines.get()?.third

    /** Atomically initializes the core engines; returns false if already initialized. */
    fun initialize(
        capture: CaptureEngine,
        query: QueryEngine,
        extensions: ExtensionRegistry? = null,
    ): Boolean {
        return engines.compareAndSet(null, Triple(capture, query, extensions))
    }
}

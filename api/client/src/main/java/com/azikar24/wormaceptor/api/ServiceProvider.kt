package com.azikar24.wormaceptor.api

import android.content.Context
import android.content.Intent
import java.io.InputStream
import java.util.UUID

/**
 * Internal interface to be implemented by WormaCeptor implementation modules (persistence, no-op, etc.)
 */
interface ServiceProvider {
    fun init(context: Context, logCrashes: Boolean)

    fun startTransaction(
        url: String,
        method: String,
        headers: Map<String, List<String>>,
        bodyStream: InputStream?,
        bodySize: Long
    ): UUID?

    fun completeTransaction(
        id: UUID,
        code: Int,
        message: String,
        headers: Map<String, List<String>>,
        bodyStream: InputStream?,
        bodySize: Long,
        protocol: String?,
        tlsVersion: String?,
        error: String?
    )

    fun cleanup(threshold: Long)

    fun getLaunchIntent(context: Context): Intent
}

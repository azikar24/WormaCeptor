package com.azikar24.wormaceptor.api

import android.content.Context
import android.content.Intent
import java.io.InputStream
import java.util.UUID

/**
 * DTO for transferring full transaction details including bodies to IDE plugin.
 * Serialized as JSON for content provider openFile() endpoint.
 */
data class TransactionDetailDto(
    val id: String,
    val method: String,
    val url: String,
    val host: String,
    val path: String,
    val code: Int?,
    val duration: Long?,
    val status: String,
    val timestamp: Long,
    val requestHeaders: Map<String, List<String>>,
    val requestBody: String?,
    val requestSize: Long,
    val responseHeaders: Map<String, List<String>>,
    val responseBody: String?,
    val responseSize: Long,
    val responseMessage: String?,
    val protocol: String?,
    val tlsVersion: String?,
    val error: String?,
    val contentType: String?
)

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

    // Query methods for ContentProvider access via reflection
    fun getAllTransactions(): List<Any>
    fun getTransaction(id: String): Any?
    fun getTransactionCount(): Int
    fun clearTransactions()

    /**
     * Get full transaction detail with bodies for IDE plugin.
     * Returns null if transaction not found.
     */
    fun getTransactionDetail(id: String): TransactionDetailDto?
}

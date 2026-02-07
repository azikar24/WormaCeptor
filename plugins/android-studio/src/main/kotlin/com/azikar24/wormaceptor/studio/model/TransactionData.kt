package com.azikar24.wormaceptor.studio.model

/**
 * Represents the status of a network transaction.
 */
enum class TransactionStatus {
    ACTIVE,
    COMPLETED,
    FAILED,
}

/**
 * Summary data for displaying a transaction in the list view.
 * Optimized for compact display in IDE context.
 */
data class TransactionSummary(
    val id: String,
    val method: String,
    val host: String,
    val path: String,
    val code: Int?,
    val tookMs: Long?,
    val hasRequestBody: Boolean,
    val hasResponseBody: Boolean,
    val status: TransactionStatus,
    val timestamp: Long,
    val contentType: String? = null,
    val requestSize: Long = 0,
    val responseSize: Long = 0,
) {
    /**
     * Returns a short display text for the transaction.
     */
    val shortDisplay: String
        get() = "$method ${path.take(50)}${if (path.length > 50) "..." else ""}"

    /**
     * Returns formatted duration string.
     */
    val durationDisplay: String
        get() = tookMs?.let { "${it}ms" } ?: "-"

    /**
     * Returns formatted status code with color hint.
     */
    val statusDisplay: String
        get() = code?.toString() ?: when (status) {
            TransactionStatus.ACTIVE -> "..."
            TransactionStatus.FAILED -> "ERR"
            TransactionStatus.COMPLETED -> "-"
        }

    /**
     * Indicates if this transaction is successful (2xx).
     */
    val isSuccess: Boolean
        get() = code != null && code in 200..299

    /**
     * Indicates if this transaction is a redirect (3xx).
     */
    val isRedirect: Boolean
        get() = code != null && code in 300..399

    /**
     * Indicates if this transaction is a client error (4xx).
     */
    val isClientError: Boolean
        get() = code != null && code in 400..499

    /**
     * Indicates if this transaction is a server error (5xx).
     */
    val isServerError: Boolean
        get() = code != null && code in 500..599

    /**
     * Full URL for display and navigation purposes.
     */
    val fullUrl: String
        get() = "https://$host$path"

    /**
     * URL for display (host + path).
     */
    val url: String
        get() = "$host$path"

    /**
     * Status text for display.
     */
    val statusText: String
        get() = when {
            status == TransactionStatus.ACTIVE -> "Pending"
            status == TransactionStatus.FAILED -> "Failed"
            code == null -> "-"
            code in 200..299 -> "Success ($code)"
            code in 300..399 -> "Redirect ($code)"
            code in 400..499 -> "Client Error ($code)"
            code >= 500 -> "Server Error ($code)"
            else -> "Unknown ($code)"
        }

    /**
     * Duration text for display.
     */
    val durationText: String
        get() = tookMs?.let { "${it}ms" } ?: "-"
}

/**
 * Full transaction details including request and response bodies.
 */
data class TransactionDetail(
    val summary: TransactionSummary,
    val requestHeaders: Map<String, List<String>>,
    val requestBody: String?,
    val responseHeaders: Map<String, List<String>>,
    val responseBody: String?,
    val responseMessage: String?,
    val protocol: String?,
    val tlsVersion: String?,
    val error: String?,
)

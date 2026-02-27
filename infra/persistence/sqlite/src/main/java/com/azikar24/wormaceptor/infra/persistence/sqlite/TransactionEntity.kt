package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import java.util.UUID

/**
 * Database entity for storing network transactions.
 *
 * Indexes are optimized for pagination queries:
 * - timestamp DESC: Main sorting for the transaction list
 * - resCode: Filtering by status code range
 * - reqMethod: Filtering by HTTP method
 * - timestamp + resCode: Compound index for sorted, filtered queries
 *
 * @property id Unique identifier for the transaction.
 * @property timestamp Epoch millis when the request was initiated.
 * @property durationMs Round-trip duration in milliseconds, or null if still in-flight.
 * @property status Current lifecycle status of the transaction.
 * @property reqUrl Full request URL including query parameters.
 * @property reqMethod HTTP method (GET, POST, PUT, etc.).
 * @property reqHeaders Map of request header names to their values.
 * @property reqBodyRef Blob storage reference key for the request body, or null if empty.
 * @property reqBodySize Size of the request body in bytes.
 * @property resCode HTTP response status code, or null if no response received.
 * @property resMessage HTTP response status message, or null if no response received.
 * @property resHeaders Map of response header names to their values, or null if no response.
 * @property resBodyRef Blob storage reference key for the response body, or null if empty.
 * @property resError Error message if the request failed, or null on success.
 * @property resProtocol Protocol used for the response (e.g. "h2", "http/1.1").
 * @property resTlsVersion TLS version negotiated for the connection, or null if plaintext.
 * @property resBodySize Size of the response body in bytes.
 * @property extensions Arbitrary key-value metadata attached to the transaction.
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["resCode"]),
        Index(value = ["reqMethod"]),
        Index(value = ["timestamp", "resCode"]),
    ],
)
data class TransactionEntity(
    @PrimaryKey val id: UUID,
    val timestamp: Long,
    val durationMs: Long?,
    val status: TransactionStatus,

    // Request Fields
    val reqUrl: String,
    val reqMethod: String,
    val reqHeaders: Map<String, List<String>>,
    val reqBodyRef: String?,
    val reqBodySize: Long = 0,

    // Response Fields
    val resCode: Int?,
    val resMessage: String?,
    val resHeaders: Map<String, List<String>>?,
    val resBodyRef: String?,
    val resError: String?,
    val resProtocol: String? = null,
    val resTlsVersion: String? = null,
    val resBodySize: Long = 0,

    val extensions: Map<String, String>,
) {
    /** Converts this entity to a domain [NetworkTransaction] model. */
    fun toDomain(): NetworkTransaction {
        return NetworkTransaction(
            id = id,
            timestamp = timestamp,
            durationMs = durationMs,
            status = status,
            request = Request(
                url = reqUrl,
                method = reqMethod,
                headers = reqHeaders,
                bodyRef = reqBodyRef,
                bodySize = reqBodySize,
            ),
            response = if (resCode != null && resMessage != null && resHeaders != null) {
                Response(
                    code = resCode,
                    message = resMessage,
                    headers = resHeaders,
                    bodyRef = resBodyRef,
                    error = resError,
                    protocol = resProtocol,
                    tlsVersion = resTlsVersion,
                    bodySize = resBodySize,
                )
            } else {
                null
            },
            extensions = extensions,
        )
    }

    /** Domain-entity conversion factory. */
    companion object {
        /** Creates a [TransactionEntity] from a domain [NetworkTransaction] model. */
        fun fromDomain(domain: NetworkTransaction): TransactionEntity {
            return TransactionEntity(
                id = domain.id,
                timestamp = domain.timestamp,
                durationMs = domain.durationMs,
                status = domain.status,
                reqUrl = domain.request.url,
                reqMethod = domain.request.method,
                reqHeaders = domain.request.headers,
                reqBodyRef = domain.request.bodyRef,
                reqBodySize = domain.request.bodySize,
                resCode = domain.response?.code,
                resMessage = domain.response?.message,
                resHeaders = domain.response?.headers,
                resBodyRef = domain.response?.bodyRef,
                resError = domain.response?.error,
                resProtocol = domain.response?.protocol,
                resTlsVersion = domain.response?.tlsVersion,
                resBodySize = domain.response?.bodySize ?: 0,
                extensions = domain.extensions,
            )
        }
    }
}

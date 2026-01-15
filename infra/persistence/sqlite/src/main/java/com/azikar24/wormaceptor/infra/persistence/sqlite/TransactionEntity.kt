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
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["resCode"]),
        Index(value = ["reqMethod"]),
        Index(value = ["timestamp", "resCode"])
    ]
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
    
    val extensions: Map<String, String>
) {
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
                bodySize = reqBodySize
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
                    bodySize = resBodySize
                )
            } else null,
            extensions = extensions
        )
    }

    companion object {
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
                extensions = domain.extensions
            )
        }
    }
}

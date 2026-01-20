package com.azikar24.wormaceptor.api.internal

import android.content.Context
import android.content.Intent
import com.azikar24.wormaceptor.api.ServiceProvider
import com.azikar24.wormaceptor.api.TransactionDetailDto
import com.azikar24.wormaceptor.core.engine.CaptureEngine
import com.azikar24.wormaceptor.core.engine.QueryEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.util.UUID

abstract class BaseServiceProviderImpl : ServiceProvider {

    protected var captureEngine: CaptureEngine? = null
    protected var queryEngine: QueryEngine? = null
    protected var notificationHelper: WormaCeptorNotificationHelper? = null

    override fun startTransaction(
        url: String,
        method: String,
        headers: Map<String, List<String>>,
        bodyStream: InputStream?,
        bodySize: Long,
    ): UUID? = runBlocking {
        captureEngine?.startTransaction(url, method, headers, bodyStream, bodySize)
    }

    override fun completeTransaction(
        id: UUID,
        code: Int,
        message: String,
        headers: Map<String, List<String>>,
        bodyStream: InputStream?,
        bodySize: Long,
        protocol: String?,
        tlsVersion: String?,
        error: String?,
    ) {
        runBlocking {
            captureEngine?.completeTransaction(
                id, code, message, headers, bodyStream, bodySize, protocol, tlsVersion, error,
            )
            val transaction = queryEngine?.getDetails(id)
            if (transaction != null) {
                notificationHelper?.show(transaction)
            }
        }
    }

    override fun cleanup(threshold: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            captureEngine?.cleanup(threshold)
        }
    }

    override fun getLaunchIntent(context: Context): Intent {
        return Intent(context, com.azikar24.wormaceptor.feature.viewer.ViewerActivity::class.java)
    }

    override fun getAllTransactions(): List<Any> = runBlocking {
        queryEngine?.getAllTransactionsForExport() ?: emptyList()
    }

    override fun getTransaction(id: String): Any? = runBlocking {
        try {
            queryEngine?.getDetails(UUID.fromString(id))
        } catch (_: Exception) {
            null
        }
    }

    override fun getTransactionCount(): Int = runBlocking {
        queryEngine?.getTransactionCount() ?: 0
    }

    override fun clearTransactions() {
        runBlocking {
            queryEngine?.clear()
        }
    }

    override fun getTransactionDetail(id: String): TransactionDetailDto? = runBlocking {
        try {
            val transaction = queryEngine?.getDetails(UUID.fromString(id)) ?: return@runBlocking null
            val request = transaction.request
            val response = transaction.response

            val host = try {
                java.net.URI(request.url).host ?: ""
            } catch (_: Exception) {
                ""
            }
            val path = try {
                java.net.URI(request.url).path ?: request.url
            } catch (_: Exception) {
                request.url
            }

            val requestBody = request.bodyRef?.let { queryEngine?.getBody(it) }
            val responseBody = response?.bodyRef?.let { queryEngine?.getBody(it) }

            val contentType = response?.headers?.entries
                ?.find { it.key.equals("content-type", ignoreCase = true) }
                ?.value?.firstOrNull()

            TransactionDetailDto(
                id = transaction.id.toString(),
                method = request.method,
                url = request.url,
                host = host,
                path = path,
                code = response?.code,
                duration = transaction.durationMs,
                status = transaction.status.name,
                timestamp = transaction.timestamp,
                requestHeaders = request.headers,
                requestBody = requestBody,
                requestSize = request.bodySize,
                responseHeaders = response?.headers ?: emptyMap(),
                responseBody = responseBody,
                responseSize = response?.bodySize ?: 0,
                responseMessage = response?.message,
                protocol = response?.protocol,
                tlsVersion = response?.tlsVersion,
                error = response?.error,
                contentType = contentType,
            )
        } catch (_: Exception) {
            null
        }
    }
}

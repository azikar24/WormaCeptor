package com.azikar24.wormaceptor.api.internal

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.room.Room
import com.azikar24.wormaceptor.api.ServiceProvider
import com.azikar24.wormaceptor.api.TransactionDetailDto
import com.azikar24.wormaceptor.core.engine.CaptureEngine
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.CrashReporter
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.infra.persistence.sqlite.FileSystemBlobStorage
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomCrashRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomTransactionRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.WormaCeptorDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.util.UUID

internal class ServiceProviderImpl : ServiceProvider {
    
    private var captureEngine: CaptureEngine? = null
    private var queryEngine: QueryEngine? = null
    private var notificationHelper: com.azikar24.wormaceptor.api.internal.WormaCeptorNotificationHelper? = null

    override fun init(context: Context, logCrashes: Boolean) {
        if (captureEngine != null) return

        val database = Room.databaseBuilder(
            context.applicationContext,
            WormaCeptorDatabase::class.java,
            "wormaceptor-v2.db"
        )
        .fallbackToDestructiveMigration()
        .build()
        
        val repository = RoomTransactionRepository(database.transactionDao())
        val crashRepository = RoomCrashRepository(database.crashDao())
        val blobStorage = FileSystemBlobStorage(context.applicationContext)
        
        captureEngine = CaptureEngine(repository, blobStorage)
        queryEngine = QueryEngine(repository, blobStorage, crashRepository)
        
        if (logCrashes) {
            val crashReporter = CrashReporter(crashRepository)
            crashReporter.init()
        }
        
        notificationHelper = com.azikar24.wormaceptor.api.internal.WormaCeptorNotificationHelper(context)

        // Share with Feature Modules
        CoreHolder.captureEngine = captureEngine
        CoreHolder.queryEngine = queryEngine
    }

    override fun startTransaction(
        url: String,
        method: String,
        headers: Map<String, List<String>>,
        bodyStream: InputStream?,
        bodySize: Long
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
        error: String?
    ) {
        runBlocking {
            captureEngine?.completeTransaction(id, code, message, headers, bodyStream, bodySize, protocol, tlsVersion, error)
            
            // Show notification if successful or even if failed
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
        } catch (e: Exception) {
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

            // Parse URL to extract host and path
            val uri = Uri.parse(request.url)
            val host = uri.host ?: ""
            val path = uri.path ?: "/"

            // Get body content from blob storage
            val requestBody = request.bodyRef?.let { queryEngine?.getBody(it) }
            val responseBody = response?.bodyRef?.let { queryEngine?.getBody(it) }

            // Extract content type from response headers
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
                contentType = contentType
            )
        } catch (e: Exception) {
            null
        }
    }
}

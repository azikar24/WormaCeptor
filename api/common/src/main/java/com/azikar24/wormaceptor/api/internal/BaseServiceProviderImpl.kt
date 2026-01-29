package com.azikar24.wormaceptor.api.internal

import android.content.Context
import android.content.Intent
import android.util.Log
import com.azikar24.wormaceptor.api.ServiceProvider
import com.azikar24.wormaceptor.api.TransactionDetailDto
import com.azikar24.wormaceptor.core.engine.CaptureEngine
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.CrashReporter
import com.azikar24.wormaceptor.core.engine.DefaultExtensionRegistry
import com.azikar24.wormaceptor.core.engine.ExtensionRegistry
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.contracts.CrashRepository
import com.azikar24.wormaceptor.domain.contracts.LeakRepository
import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.get
import java.io.InputStream
import java.util.UUID

abstract class BaseServiceProviderImpl : ServiceProvider {

    private companion object {
        private const val TAG = "BaseServiceProvider"
    }

    protected var captureEngine: CaptureEngine? = null
    protected var queryEngine: QueryEngine? = null
    protected var extensionRegistry: ExtensionRegistry? = null
    protected var notificationHelper: WormaCeptorNotificationHelper? = null

    protected data class StorageDependencies(
        val transactionRepository: TransactionRepository,
        val crashRepository: CrashRepository,
        val blobStorage: BlobStorage,
        val leakRepository: LeakRepository,
    )

    protected abstract fun createDependencies(context: Context): StorageDependencies
    protected abstract fun getNotificationTitle(): String

    override fun init(context: Context, logCrashes: Boolean, leakNotifications: Boolean) {
        if (captureEngine != null) return

        // Initialize Koin for engine dependencies (WebSocketMonitorEngine, etc.)
        WormaCeptorKoin.init(context)

        val deps = createDependencies(context)

        val extensions = DefaultExtensionRegistry()
        val capture = CaptureEngine(deps.transactionRepository, deps.blobStorage, extensions)
        val query = QueryEngine(deps.transactionRepository, deps.blobStorage, deps.crashRepository)

        if (!CoreHolder.initialize(capture, query, extensions)) {
            return // Already initialized
        }

        captureEngine = capture
        queryEngine = query
        extensionRegistry = extensions

        if (logCrashes) {
            val crashReporter = CrashReporter(deps.crashRepository)
            crashReporter.init()
        }

        notificationHelper = WormaCeptorNotificationHelper(context, getNotificationTitle())

        // Configure leak detection engine with persistence and notifications
        configureLeakDetection(context, deps.leakRepository, leakNotifications)

        // Configure thread violation engine with notifications
        configureThreadViolation(context)
    }

    private fun configureLeakDetection(context: Context, leakRepository: LeakRepository, leakNotifications: Boolean) {
        try {
            val leakEngine: LeakDetectionEngine = get(LeakDetectionEngine::class.java)
            val notificationHelper = if (leakNotifications) {
                LeakNotificationHelper(context)
            } else {
                null
            }

            leakEngine.configure(
                repository = leakRepository,
                onLeakCallback = notificationHelper?.let { helper ->
                    { leak -> helper.show(leak) }
                },
            )
        } catch (e: RuntimeException) {
            Log.d(TAG, "LeakDetectionEngine not available in Koin", e)
        }
    }

    private fun configureThreadViolation(context: Context) {
        try {
            val threadViolationEngine: ThreadViolationEngine = get(ThreadViolationEngine::class.java)
            val notificationHelper = ThreadViolationNotificationHelper(context)

            threadViolationEngine.configure(
                hostPackage = context.packageName,
                onViolationCallback = { violation -> notificationHelper.show(violation) },
            )
        } catch (e: RuntimeException) {
            Log.d(TAG, "ThreadViolationEngine not available in Koin", e)
        }
    }

    override fun startTransaction(
        url: String,
        method: String,
        headers: Map<String, List<String>>,
        bodyStream: InputStream?,
        bodySize: Long,
    ): UUID? = runBlocking(Dispatchers.IO) {
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
        runBlocking(Dispatchers.IO) {
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

    override fun getAllTransactions(): List<Any> = runBlocking(Dispatchers.IO) {
        queryEngine?.getAllTransactionsForExport() ?: emptyList()
    }

    override fun getTransaction(id: String): Any? = runBlocking(Dispatchers.IO) {
        try {
            queryEngine?.getDetails(UUID.fromString(id))
        } catch (_: Exception) {
            null
        }
    }

    override fun getTransactionCount(): Int = runBlocking(Dispatchers.IO) {
        queryEngine?.getTransactionCount() ?: 0
    }

    override fun clearTransactions() {
        runBlocking(Dispatchers.IO) {
            queryEngine?.clear()
        }
    }

    override fun getTransactionDetail(id: String): TransactionDetailDto? = runBlocking(Dispatchers.IO) {
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
                extensions = transaction.extensions,
            )
        } catch (_: Exception) {
            null
        }
    }
}

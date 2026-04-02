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
import com.azikar24.wormaceptor.core.engine.HighlighterRegistry
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.core.engine.ParserRegistry
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.contracts.CrashRepository
import com.azikar24.wormaceptor.domain.contracts.FormDataParser
import com.azikar24.wormaceptor.domain.contracts.ImageMetadataExtractor
import com.azikar24.wormaceptor.domain.contracts.LeakRepository
import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.domain.contracts.MultipartParser
import com.azikar24.wormaceptor.domain.contracts.ProtobufDecoder
import com.azikar24.wormaceptor.domain.contracts.PushSimulatorRepository
import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import com.azikar24.wormaceptor.domain.contracts.WebViewMonitorRepository
import com.azikar24.wormaceptor.domain.contracts.XmlFormatter
import com.azikar24.wormaceptor.infra.parser.form.FormBodyParser
import com.azikar24.wormaceptor.infra.parser.html.HtmlBodyParser
import com.azikar24.wormaceptor.infra.parser.image.ImageParser
import com.azikar24.wormaceptor.infra.parser.json.JsonBodyParser
import com.azikar24.wormaceptor.infra.parser.multipart.MultipartBodyParser
import com.azikar24.wormaceptor.infra.parser.pdf.PdfParser
import com.azikar24.wormaceptor.infra.parser.protobuf.ProtobufBodyParser
import com.azikar24.wormaceptor.infra.parser.xml.XmlBodyParser
import com.azikar24.wormaceptor.infra.syntax.json.JsonHighlighter
import com.azikar24.wormaceptor.infra.syntax.xml.XmlHighlighter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.get
import java.io.InputStream
import java.util.UUID

/** Base implementation of [ServiceProvider] that wires up core engines, notifications, and DI. */
abstract class BaseServiceProviderImpl : ServiceProvider {

    protected var captureEngine: CaptureEngine? = null
    protected var queryEngine: QueryEngine? = null
    protected var extensionRegistry: ExtensionRegistry? = null
    protected var notificationHelper: WormaCeptorNotificationHelper? = null

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, throwable ->
            Log.w(TAG, "Background operation failed", throwable)
        },
    )

    protected data class StorageDependencies(
        val transactionRepository: TransactionRepository,
        val crashRepository: CrashRepository,
        val blobStorage: BlobStorage,
        val leakRepository: LeakRepository,
        val locationSimulatorRepository: LocationSimulatorRepository,
        val pushSimulatorRepository: PushSimulatorRepository,
        val webViewMonitorRepository: WebViewMonitorRepository,
    )

    protected abstract fun createDependencies(context: Context): StorageDependencies
    protected abstract fun getNotificationTitle(): String

    override fun init(
        context: Context,
        logCrashes: Boolean,
        leakNotifications: Boolean,
    ) {
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

        // Register storage repositories in Koin for feature access
        val koin = WormaCeptorKoin.getKoin()
        koin.loadModules(
            listOf(
                module {
                    single<LocationSimulatorRepository> { deps.locationSimulatorRepository }
                    single<PushSimulatorRepository> { deps.pushSimulatorRepository }
                },
            ),
        )

        // Configure leak detection engine with persistence and notifications
        configureLeakDetection(context, deps.leakRepository, leakNotifications)

        // Configure thread violation engine with notifications
        configureThreadViolation(context)

        // Configure WebView monitor engine with persistence
        configureWebViewMonitor(deps.webViewMonitorRepository)

        // Register syntax highlighters
        configureHighlighters()

        // Register body parsers
        configureParsers(context)

        // Feature navigation contributors are discovered automatically via ServiceLoader
    }

    private fun configureLeakDetection(
        context: Context,
        leakRepository: LeakRepository,
        leakNotifications: Boolean,
    ) {
        try {
            val leakEngine: LeakDetectionEngine = get(LeakDetectionEngine::class.java)
            val notificationHelper = if (leakNotifications) {
                LeakNotificationHelper(context)
            } else {
                null
            }

            leakEngine.configure(
                repository = leakRepository,
                onLeakCallback = notificationHelper?.let { helper -> helper::show },
            )
        } catch (e: RuntimeException) {
            Log.d(TAG, "LeakDetectionEngine not available in Koin", e)
        }
    }

    private fun configureWebViewMonitor(webViewMonitorRepository: WebViewMonitorRepository) {
        try {
            val webViewMonitorEngine: WebViewMonitorEngine = get(WebViewMonitorEngine::class.java)
            webViewMonitorEngine.configure(repository = webViewMonitorRepository)
        } catch (e: RuntimeException) {
            Log.d(TAG, "WebViewMonitorEngine not available in Koin", e)
        }
    }

    private fun configureHighlighters() {
        try {
            val registry: HighlighterRegistry = get(HighlighterRegistry::class.java)
            registry.register(JsonHighlighter())
            registry.register(XmlHighlighter())
        } catch (e: RuntimeException) {
            Log.d(TAG, "HighlighterRegistry not available", e)
        }
    }

    private fun configureParsers(context: Context) {
        try {
            val registry: ParserRegistry = get(ParserRegistry::class.java)
            val protobufParser = ProtobufBodyParser()
            val multipartParser = MultipartBodyParser()
            val formParser = FormBodyParser()
            val xmlParser = XmlBodyParser()
            val htmlParser = HtmlBodyParser()
            val jsonParser = JsonBodyParser()
            val imageParser = ImageParser()
            val pdfParser = PdfParser(context)

            registry.register(protobufParser)
            registry.register(multipartParser)
            registry.register(formParser)
            registry.register(xmlParser)
            registry.register(htmlParser)
            registry.register(jsonParser)
            registry.register(imageParser)
            registry.register(pdfParser)

            // Register typed interfaces for direct Koin injection
            val koin = WormaCeptorKoin.getKoin()
            koin.loadModules(
                listOf(
                    module {
                        single<ProtobufDecoder> { protobufParser }
                        single<MultipartParser> { multipartParser }
                        single<FormDataParser> { formParser }
                        single<XmlFormatter> { xmlParser }
                        single<ImageMetadataExtractor> { imageParser }
                    },
                ),
            )
        } catch (e: RuntimeException) {
            Log.d(TAG, "ParserRegistry not available", e)
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
        scope.launch {
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
        scope.launch {
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
        scope.launch {
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

    private companion object {
        private const val TAG = "BaseServiceProvider"
    }
}

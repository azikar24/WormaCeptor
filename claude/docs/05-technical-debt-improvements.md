# WormaCeptor V2 - Technical Debt & Improvements

This document identifies technical debt items, architectural weaknesses, and improvement opportunities in WormaCeptor V2. Each item includes current state, problem statement, proposed solution, impact analysis, and estimated effort.

## Overview

**Current Technical Debt Status**:
- Critical: 3 items
- High: 6 items
- Medium: 6 items
- Low: 4 items
- **Total**: 19 items

**Sources**:
- Codebase analysis (64 Kotlin files)
- Existing TECH_DEBT_REGISTER.yaml
- Architecture review
- Code quality analysis

## Critical Priority

These items pose security risks, performance problems, or could cause production issues.

### 1. SEC-001: Plaintext Storage of Sensitive Data

**Current State**:
- Request/response headers and bodies stored unencrypted in SQLite and filesystem
- Sensitive data (Authorization headers, passwords in bodies) persisted in plaintext
- Device compromise or backup extraction exposes all captured traffic

**File References**:
- `infra/persistence/sqlite/src/main/java/com/azikar24/wormaceptor/infra/persistence/sqlite/WormaCeptorDatabase.kt`
- `infra/persistence/sqlite/src/main/java/com/azikar24/wormaceptor/infra/persistence/sqlite/FileSystemBlobStorage.kt`

**Problem Statement**:
Security vulnerability. If a rooted device or unencrypted backup is accessed, all captured network traffic (including tokens, passwords, personal data) is readable in plaintext.

**Proposed Solution**:

**Option A: Encrypt Database and Blobs** (Recommended)
```kotlin
// Use SQLCipher for encrypted SQLite
dependencies {
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
}

// Initialize database with encryption
val passphrase = SecurityUtils.getDatabaseKey(context)  // Keystore-backed
val factory = SupportFactory(passphrase)

Room.databaseBuilder(context, WormaCeptorDatabase::class.java, "wormaceptor-v2.db")
    .openHelperFactory(factory)
    .build()

// Encrypt blobs before writing
class EncryptedBlobStorage(
    private val baseStorage: BlobStorage,
    private val cipher: Cipher
) : BlobStorage {
    override suspend fun saveBlob(stream: InputStream): BlobID {
        val encrypted = cipher.encrypt(stream.readBytes())
        return baseStorage.saveBlob(encrypted.inputStream())
    }

    override suspend fun readBlob(id: BlobID): InputStream? {
        val encrypted = baseStorage.readBlob(id)?.readBytes() ?: return null
        return cipher.decrypt(encrypted).inputStream()
    }
}
```

**Option B: Automatic Wipe on Lock** (Alternative)
```kotlin
// Clear all data when device is locked
class DeviceLockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            WormaCeptorApi.clearAll()
        }
    }
}
```

**Impact Analysis**:
- **Security**: Eliminates plaintext storage vulnerability
- **Performance**: Encryption adds ~10-15% overhead (negligible for debug tool)
- **Complexity**: Requires key management via Android Keystore

**Estimated Effort**: 3-5 days

**Priority Rationale**: Security vulnerability in tool handling sensitive data

---

### 2. PERF-001: Full Body Read into Memory

**Current State**:
- OkHttp interceptor reads entire response body into memory via `buffer.clone().readUtf8()`
- Large responses (e.g., 10MB image) cause memory pressure and GC churn
- Can trigger OutOfMemoryError on low-end devices

**File References**:
- `api/client/src/main/java/com/azikar24/wormaceptor/api/WormaCeptorInterceptor.kt:38`
- `api/client/src/main/java/com/azikar24/wormaceptor/api/WormaCeptorInterceptor.kt:80`

**Problem Statement**:
Performance and stability issue. Reading large bodies into memory doubles memory usage (original + copy) and can crash the app.

**Current Code**:
```kotlin
// PROBLEMATIC: Loads entire body into memory
val bodyString = buffer.clone().readUtf8()
applyRedaction(bodyString)
```

**Proposed Solution**:

**Option A: Stream Bodies to Disk** (Recommended)
```kotlin
// Stream directly to BlobStorage without reading into memory
override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()

    // Stream request body to BlobStorage
    val requestBlobId = request.body?.let { body ->
        val stream = PipedInputStream()
        val sink = stream.sink().buffer()

        // Write body to sink (streams to BlobStorage)
        launch(Dispatchers.IO) {
            body.writeTo(sink)
            sink.close()
        }

        blobStorage.saveBlob(stream)  // Saves while streaming
    }

    // Proceed with request
    val response = chain.proceed(request)

    // Stream response body to BlobStorage
    val responseBlobId = response.body?.let { body ->
        blobStorage.saveBlobFromSource(body.source())  // Direct streaming
    }

    return response
}

// In BlobStorage interface
suspend fun saveBlobFromSource(source: BufferedSource): BlobID
```

**Option B: Configurable Size Threshold**
```kotlin
// Only read small bodies into memory
val body = when {
    contentLength < 1_000_000 -> {  // 1MB threshold
        buffer.clone().readUtf8()
    }
    else -> {
        "[Large body >1MB - not captured]"
    }
}
```

**Impact Analysis**:
- **Memory**: Reduces peak memory usage by 50% for large responses
- **Performance**: Eliminates GC pressure from large allocations
- **Functionality**: Requires refactoring blob storage to support streaming

**Estimated Effort**: 5-8 days

**Priority Rationale**: Can cause crashes, affects all users with large payloads

---

### 3. ARCH-002: Threading Violation in Interceptor

**Current State**:
- `ServiceProviderImpl` uses `runBlocking` in OkHttp interceptor
- Blocks network thread pool, reducing concurrency
- Can cause timeouts if database operations are slow

**File References**:
- `api/impl/persistence/src/main/java/com/azikar24/wormaceptor/api/internal/ServiceProviderImpl.kt:64-65`
- `api/impl/persistence/src/main/java/com/azikar24/wormaceptor/api/internal/ServiceProviderImpl.kt:79-87`

**Current Code**:
```kotlin
// PROBLEMATIC: Blocks OkHttp thread
override fun startTransaction(request: Request): UUID? = runBlocking {
    captureEngine?.startTransaction(request)
}
```

**Problem Statement**:
Architecture violation. OkHttp interceptors should not block, especially on I/O operations. This reduces request throughput and can cause cascading delays.

**Proposed Solution**:

**Option A: Fire-and-Forget with Coroutine** (Recommended)
```kotlin
override fun startTransaction(request: Request): UUID? {
    val uuid = UUID.randomUUID()

    // Launch coroutine without blocking
    captureScope.launch {
        try {
            captureEngine?.startTransaction(request.copy(id = uuid))
        } catch (e: Exception) {
            Log.e("WormaCeptor", "Failed to capture transaction", e)
        }
    }

    return uuid
}
```

**Option B: Background HandlerThread**
```kotlin
class AsyncServiceProvider : ServiceProvider {
    private val handler = HandlerThread("WormaCeptor").apply { start() }.handler

    override fun startTransaction(request: Request): UUID? {
        val uuid = UUID.randomUUID()

        handler.post {
            runBlocking {
                captureEngine?.startTransaction(request)
            }
        }

        return uuid
    }
}
```

**Impact Analysis**:
- **Performance**: Eliminates network thread blocking, improves throughput
- **Correctness**: No behavioral change (already async from app's perspective)
- **Reliability**: Reduces risk of timeouts

**Estimated Effort**: 2-3 days

**Priority Rationale**: Architectural anti-pattern affecting performance

---

## High Priority

These items significantly impact code quality, testability, or maintainability.

### 4. Replace CoreHolder with Proper Dependency Injection

**Current State**:
- `CoreHolder` is a global mutable singleton
- Engines accessed via `CoreHolder.queryEngine!!` (force unwrap)
- No constructor injection, manual wiring

**File References**:
- `core/engine/src/main/java/com/azikar24/wormaceptor/core/engine/CoreHolder.kt`
- `features/viewer/src/main/java/com/azikar24/wormaceptor/feature/viewer/ViewerActivity.kt:48`

**Problem Statement**:
Service Locator anti-pattern. Makes code hard to test, creates hidden dependencies, and risks `NullPointerException` if accessed before initialization.

**Proposed Solution**:

**Introduce Hilt for Dependency Injection**

```kotlin
// In :core:engine module
@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideCaptureEngine(
        repository: TransactionRepository,
        blobStorage: BlobStorage
    ): CaptureEngine {
        return CaptureEngine(repository, blobStorage)
    }

    @Provides
    @Singleton
    fun provideQueryEngine(
        repository: TransactionRepository,
        blobStorage: BlobStorage,
        crashRepository: CrashRepository
    ): QueryEngine {
        return QueryEngine(repository, blobStorage, crashRepository)
    }
}

// In :features:viewer module
@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val queryEngine: QueryEngine  // Injected, no CoreHolder
) : ViewModel() {
    // ...
}

// In ViewerActivity
@AndroidEntryPoint
class ViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // ViewModel automatically injected
            val viewModel: ViewerViewModel = hiltViewModel()
            // ...
        }
    }
}
```

**Impact Analysis**:
- **Testability**: Easy to inject mocks in tests
- **Safety**: No force unwraps, compile-time dependency validation
- **Maintainability**: Clear dependency graph
- **Complexity**: Adds Hilt dependency and annotations

**Estimated Effort**: 5-7 days

**Priority Rationale**: Improves testability and removes crash risk

---

### 5. Testing: Add Unit Tests

**Current State**:
- Only 1 test file out of 64 Kotlin files (1.5% coverage)
- No tests for business logic (CaptureEngine, QueryEngine)
- No tests for ViewModels or repositories

**File References**:
- `test/architecture/src/test/java/com/azikar24/wormaceptor/test/architecture/ArchitectureTest.kt` (only test)

**Problem Statement**:
Quality risk. Without tests, refactoring is dangerous and regressions go undetected. Critical business logic is untested.

**Proposed Solution**:

**Phase 1: Test Business Logic** (Weeks 1-2)
```kotlin
// CaptureEngineTest.kt
class CaptureEngineTest {

    private lateinit var engine: CaptureEngine
    private lateinit var mockRepository: TransactionRepository
    private lateinit var mockBlobStorage: BlobStorage

    @Before
    fun setup() {
        mockRepository = mockk()
        mockBlobStorage = mockk()
        engine = CaptureEngine(mockRepository, mockBlobStorage)
    }

    @Test
    fun `startTransaction saves request to repository`() = runTest {
        // Given
        val request = Request(
            url = "https://api.example.com",
            method = "GET",
            headers = emptyMap(),
            bodyBlobId = null,
            bodySize = 0
        )

        coEvery { mockBlobStorage.saveBlob(any()) } returns "blob-123"
        coEvery { mockRepository.saveTransaction(any()) } returns Unit

        // When
        val id = engine.startTransaction(request)

        // Then
        assertNotNull(id)
        coVerify { mockRepository.saveTransaction(match { it.request == request }) }
    }
}
```

**Phase 2: Test Repositories** (Weeks 3-4)
```kotlin
// RoomTransactionRepositoryTest.kt
@RunWith(AndroidJUnit4::class)
class RoomTransactionRepositoryTest {

    private lateinit var database: WormaCeptorDatabase
    private lateinit var repository: RoomTransactionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WormaCeptorDatabase::class.java)
            .build()
        repository = RoomTransactionRepository(database.transactionDao())
    }

    @Test
    fun `getAllTransactions returns saved transactions`() = runTest {
        // Given
        val transaction = createMockTransaction()
        repository.saveTransaction(transaction)

        // When
        val results = repository.getAllTransactions().first()

        // Then
        assertEquals(1, results.size)
        assertEquals(transaction.id, results[0].id)
    }
}
```

**Phase 3: Test UI** (Weeks 5-6)
```kotlin
// ViewerViewModelTest.kt
@ExperimentalCoroutinesApi
class ViewerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ViewerViewModel
    private lateinit var mockQueryEngine: QueryEngine

    @Before
    fun setup() {
        mockQueryEngine = mockk()
        every { mockQueryEngine.observeTransactions() } returns flowOf(emptyList())
        every { mockQueryEngine.observeCrashes() } returns flowOf(emptyList())

        viewModel = ViewerViewModel(mockQueryEngine)
    }

    @Test
    fun `search updates query state`() {
        // When
        viewModel.search("test")

        // Then
        assertEquals("test", viewModel.searchQuery.value)
    }
}
```

**Impact Analysis**:
- **Quality**: Catch regressions early
- **Confidence**: Safe refactoring
- **Documentation**: Tests document expected behavior

**Estimated Effort**: 3-4 weeks (spread over sprints)

**Priority Rationale**: Essential for long-term maintainability

---

### 6. Testing: Add Integration Tests

**Current State**:
- No tests verifying end-to-end flows
- Room database untested with real data
- Interceptor→Engine→Repository flow untested

**Proposed Solution**:

```kotlin
// InterceptorIntegrationTest.kt
@RunWith(AndroidJUnit4::class)
class InterceptorIntegrationTest {

    @get:Rule
    val mockWebServer = MockWebServer()

    private lateinit var database: WormaCeptorDatabase
    private lateinit var blobStorage: BlobStorage
    private lateinit var interceptor: WormaCeptorInterceptor
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setup() {
        // Real database (in-memory)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WormaCeptorDatabase::class.java)
            .build()

        // Real blob storage (temp directory)
        blobStorage = FileSystemBlobStorage(context.cacheDir)

        // Real components wired together
        val repository = RoomTransactionRepository(database.transactionDao())
        val engine = CaptureEngine(repository, blobStorage)
        // ... initialize interceptor with engine

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Test
    fun `captured transaction appears in database`() = runTest {
        // Given
        mockWebServer.enqueue(MockResponse().setBody("{\"status\":\"ok\"}"))

        // When
        val request = Request.Builder()
            .url(mockWebServer.url("/api/test"))
            .build()
        okHttpClient.newCall(request).execute()

        // Then
        val transactions = database.transactionDao().getAll().first()
        assertEquals(1, transactions.size)
        assertEquals("/api/test", transactions[0].reqPath)
    }
}
```

**Estimated Effort**: 1-2 weeks

---

### 7. Testing: Add Compose UI Tests

**Current State**:
- No UI tests for Compose screens
- User interactions untested
- Accessibility untested

**Proposed Solution**:

```kotlin
// HomeScreenTest.kt
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `clicking filter icon shows filter sheet`() {
        composeTestRule.setContent {
            HomeScreen(viewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithContentDescription("Filter")
            .performClick()

        composeTestRule
            .onNodeWithText("HTTP Method")
            .assertIsDisplayed()
    }

    @Test
    fun `transactions list shows items`() {
        composeTestRule.setContent {
            TransactionListScreen(
                transactions = listOf(mockTransaction),
                onTransactionClick = {}
            )
        }

        composeTestRule
            .onNodeWithText("GET")
            .assertIsDisplayed()
    }
}
```

**Estimated Effort**: 2-3 weeks

---

### 8. Fix CrashReporter Race Condition

**Current State**:
- Uses `Thread.sleep(500)` hack to allow database write before app terminates
- Unreliable: may not be enough time on slow devices
- Not guaranteed to complete

**File References**:
- `core/engine/src/main/java/com/azikar24/wormaceptor/core/engine/CrashReporter.kt:51`

**Current Code**:
```kotlin
// HACK: Sleep to allow DB write
Thread.sleep(500)
originalHandler?.uncaughtException(thread, throwable)
```

**Problem Statement**:
Reliability issue. Crash may not be saved if database write takes >500ms. Arbitrary sleep is a code smell.

**Proposed Solution**:

**Option A: Synchronous Crash Write** (Recommended)
```kotlin
override fun uncaughtException(thread: Thread, throwable: Throwable) {
    try {
        val crash = Crash(
            timestamp = System.currentTimeMillis(),
            exceptionType = throwable.javaClass.simpleName,
            message = throwable.message,
            stackTrace = getStackTraceString(throwable)
        )

        // SYNCHRONOUS: Block until write completes
        runBlocking(Dispatchers.IO) {
            crashRepository.saveCrash(crash)
        }
    } catch (e: Exception) {
        Log.e("CrashReporter", "Failed to save crash", e)
    } finally {
        // Delegate to original handler or exit
        originalHandler?.uncaughtException(thread, throwable)
            ?: System.exit(2)
    }
}
```

**Option B: Write to File, Process Later**
```kotlin
// Write crash to file immediately (fast)
val crashFile = File(context.cacheDir, "pending_crashes/${System.currentTimeMillis()}.json")
crashFile.writeText(Json.encodeToString(crash))

// On next app start, import pending crashes to database
```

**Impact Analysis**:
- **Reliability**: Guaranteed crash capture
- **Performance**: Negligible (app is crashing anyway)
- **Simplicity**: Removes hacky sleep

**Estimated Effort**: 1 day

**Priority Rationale**: Reliability issue for core feature

---

### 9. Implement Proper Logging Framework

**Current State**:
- Uses `printStackTrace()` for error handling
- No structured logging
- No log levels (debug, info, warn, error)

**File References**:
- `api/client/src/main/java/com/azikar24/wormaceptor/api/WormaCeptorInterceptor.kt:54, 110`
- `api/impl/persistence/src/main/java/com/azikar24/wormaceptor/api/internal/ServiceProviderImpl.kt`

**Problem Statement**:
Observability issue. Hard to diagnose issues without structured logging. printStackTrace clutters logcat and provides no filtering.

**Proposed Solution**:

**Integrate Timber for Structured Logging**

```kotlin
// In WormaCeptorApi.init()
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}

// Usage throughout codebase
try {
    captureEngine?.startTransaction(request)
} catch (e: Exception) {
    Timber.e(e, "Failed to start transaction for ${request.url}")
    // Previously: e.printStackTrace()
}

// Structured logging with tags
Timber.tag("WormaCeptor:Interceptor")
    .d("Captured request: ${request.method} ${request.url}")

Timber.tag("WormaCeptor:Database")
    .w("Transaction not found: $id")
```

**Impact Analysis**:
- **Observability**: Easier debugging
- **Performance**: Minimal overhead
- **Flexibility**: Easy to add file logging, crash reporting integration

**Estimated Effort**: 2 days

**Priority Rationale**: Improves developer experience and debugging

---

## Medium Priority

These items improve code quality but are not blocking issues.

### 10. Eliminate Code Duplication: NotificationHelper

**Current State**:
- `WormaCeptorNotificationHelper` duplicated in two modules:
  - `api/impl/persistence`
  - `api/impl/imdb`
- Identical constants and logic

**Problem Statement**:
Maintainability issue. Changes must be made in two places, risking divergence.

**Proposed Solution**:

**Move to Shared Module**

```kotlin
// Create :platform:android:notifications module
class WormaCeptorNotificationHelper(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "wormaceptor_v2_channel"
        const val NOTIFICATION_ID = 4200
        const val BUFFER_SIZE = 10
    }

    fun show(transaction: NetworkTransaction) {
        // Shared implementation
    }
}

// Both impl modules depend on :platform:android:notifications
```

**Estimated Effort**: 1 day

---

### 11. Extract URL Parsing Utilities

**Current State**:
- URL parsing repeated in multiple files:
  - `RoomTransactionRepository.kt:72-86`
  - `TransactionDetailScreen.kt:68-74`
- Duplicated logic for extracting path, host, query

**Problem Statement**:
Code duplication. Bug fixes require changes in multiple places.

**Proposed Solution**:

**Create URLUtils Extension Functions**

```kotlin
// In :domain:entities or :core:utils
object URLUtils {
    fun String.extractHost(): String? {
        return try {
            URI(this).host
        } catch (e: URISyntaxException) {
            null
        }
    }

    fun String.extractPath(): String? {
        return try {
            URI(this).path ?: "/"
        } catch (e: URISyntaxException) {
            null
        }
    }

    fun String.extractQuery(): String? {
        return try {
            URI(this).query
        } catch (e: URISyntaxException) {
            null
        }
    }
}

// Usage
val host = transaction.request.url.extractHost()
val path = transaction.request.url.extractPath()
```

**Estimated Effort**: 1 day

---

### 12. Entity Mapping Library

**Current State**:
- Manual `toDomain()` and `fromDomain()` methods in entities
- Verbose, boilerplate-heavy
- Error-prone for large entities

**File References**:
- `infra/persistence/sqlite/.../TransactionEntity.kt:37-89`
- `infra/persistence/sqlite/.../CrashEntity.kt`

**Problem Statement**:
Boilerplate reduces maintainability. Adding fields requires updating multiple mapping functions.

**Proposed Solution**:

**Option A: Use MapStruct** (Compile-time code generation)
```kotlin
@Mapper
interface TransactionMapper {
    fun toDomain(entity: TransactionEntity): NetworkTransaction
    fun toEntity(domain: NetworkTransaction): TransactionEntity
}
```

**Option B: Keep Manual (Acceptable Given Small Scale)**

Current approach is actually reasonable for a codebase of this size. Refactoring may not be worth the complexity.

**Estimated Effort**: 3-5 days (if pursuing Option A)

**Priority Rationale**: Low ROI given small number of entities

---

### 13. Add Pagination for Transaction Lists

**Current State**:
- Room queries return full lists: `fun getAll(): Flow<List<TransactionEntity>>`
- No `LIMIT` or `OFFSET` clauses
- Can retrieve thousands of records at once

**Problem Statement**:
Performance degradation at scale. Loading 10,000 transactions into memory is slow and memory-intensive.

**Proposed Solution**:

**Integrate Paging 3**

```kotlin
// In TransactionDao
@Query("SELECT * FROM transactions ORDER BY timestamp DESC")
fun getAllPaged(): PagingSource<Int, TransactionEntity>

// In Repository
fun getAllTransactions(): Flow<PagingData<TransactionSummary>> {
    return Pager(
        config = PagingConfig(
            pageSize = 50,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { dao.getAllPaged() }
    ).flow.map { pagingData ->
        pagingData.map { it.toDomain() }
    }
}

// In ViewModel
val transactions: Flow<PagingData<TransactionSummary>> =
    queryEngine.getAllTransactions()
        .cachedIn(viewModelScope)

// In UI
val lazyPagingItems = transactions.collectAsLazyPagingItems()

LazyColumn {
    items(lazyPagingItems.itemCount) { index ->
        lazyPagingItems[index]?.let { transaction ->
            TransactionItem(transaction)
        }
    }
}
```

**Impact Analysis**:
- **Performance**: Loads only visible items, reduces memory
- **Scalability**: Handles tens of thousands of transactions
- **Complexity**: Requires Paging 3 integration

**Estimated Effort**: 3-5 days

---

### 14. Optimize Search Performance

**Current State**:
- Search uses `LIKE '%query%'` in SQL
- Cannot use indexes effectively (leading wildcard)
- Full table scan on every search

**File References**:
- `infra/persistence/sqlite/.../TransactionDao.kt` (search query)

**Problem Statement**:
Performance issue at scale. Searching 10,000 transactions takes seconds.

**Proposed Solution**:

**Add Full-Text Search (FTS)**

```kotlin
// Create FTS table
@Entity(tableName = "transactions_fts")
@Fts4(contentEntity = TransactionEntity::class)
data class TransactionFtsEntity(
    val reqUrl: String,
    val reqMethod: String,
    val statusCode: Int?
)

@Dao
interface TransactionFtsDao {
    @Query("SELECT * FROM transactions_fts WHERE transactions_fts MATCH :query")
    fun search(query: String): Flow<List<TransactionFtsEntity>>
}

// Usage
dao.search("auth*")  // Fast prefix search with FTS index
```

**Impact Analysis**:
- **Performance**: 10-100x faster for large datasets
- **Features**: Enables advanced search (prefix, phrase, boolean)
- **Complexity**: Requires FTS table maintenance

**Estimated Effort**: 2-3 days

---

### 15. Externalize Hard-Coded Values

**Current State**:
- Magic numbers and strings throughout codebase
- Database name, notification IDs, shake thresholds hard-coded

**Problem Statement**:
Configurability issue. Cannot change settings without code changes.

**Proposed Solution**:

**Create WormaCeptorConfig Object**

```kotlin
object WormaCeptorConfig {
    // Database
    const val DATABASE_NAME = "wormaceptor-v2.db"
    const val DATABASE_VERSION = 2

    // Blob Storage
    const val BLOB_DIRECTORY = "wormaceptor_blobs"

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "wormaceptor_v2_channel"
    const val NOTIFICATION_ID = 4200
    const val NOTIFICATION_BUFFER_SIZE = 10

    // Shake Detection
    const val SHAKE_THRESHOLD_GRAVITY = 2.7f
    const val SHAKE_SLOP_TIME_MS = 3000
    const val SHAKE_COUNT_RESET_TIME_MS = 500

    // Interceptor
    const val DEFAULT_MAX_CONTENT_LENGTH = 250_000L
    const val DEFAULT_RETENTION_MS = 7 * 24 * 60 * 60 * 1000L  // 1 week

    // UI
    const val LARGE_BODY_THRESHOLD = 500_000L
    const val TRUNCATED_BODY_SIZE = 100_000L
}
```

**Estimated Effort**: 1 day

---

## Low Priority

These items are minor improvements or code cleanup.

### 16. Extract UI Strings to strings.xml

**Current State**:
- Hard-coded strings in Composables
- No internationalization support
- Inconsistent wording

**Example**:
```kotlin
Text("No transactions match filters")  // Hard-coded
```

**Proposed Solution**:

```xml
<!-- strings.xml -->
<resources>
    <string name="transactions_empty_filtered">No transactions match filters</string>
    <string name="transactions_empty">No transactions captured</string>
    <string name="crashes_empty">No crashes captured</string>
    <string name="clear_filters">Clear Filters</string>
</resources>

// Usage
Text(stringResource(R.string.transactions_empty_filtered))
```

**Estimated Effort**: 2-3 days

---

### 17. Replace Magic Numbers with Named Constants

**Current State**:
- Time periods calculated inline
- Arbitrary thresholds

**Example**:
```kotlin
ONE_WEEK = 7 * 24 * 60 * 60 * 1000L  // Magic calculation
```

**Proposed Solution**:

```kotlin
object TimeConstants {
    const val MILLIS_PER_SECOND = 1000L
    const val SECONDS_PER_MINUTE = 60L
    const val MINUTES_PER_HOUR = 60L
    const val HOURS_PER_DAY = 24L
    const val DAYS_PER_WEEK = 7L

    const val ONE_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE
    const val ONE_HOUR = ONE_MINUTE * MINUTES_PER_HOUR
    const val ONE_DAY = ONE_HOUR * HOURS_PER_DAY
    const val ONE_WEEK = ONE_DAY * DAYS_PER_WEEK
}
```

**Estimated Effort**: 1 day

---

### 18. Reduce lateinit vars

**Current State**:
- Several `lateinit var` in UI components
- Risk of `UninitializedPropertyAccessException`

**Proposed Solution**:

**Option A: Use nullable with safe calls**
```kotlin
private var sensorManager: SensorManager? = null  // Instead of lateinit
```

**Option B: Initialize in constructor/init**
```kotlin
class ShakeDetector(context: Context) {
    private val sensorManager = context.getSystemService<SensorManager>()!!
}
```

**Estimated Effort**: 1 day

---

### 19. Thread Safety for RedactionConfig

**Current State**:
- `RedactionConfig` uses mutable sets
- Accessed from multiple threads (OkHttp interceptor runs on thread pool)
- No synchronization

**File References**:
- `api/client/src/main/java/com/azikar24/wormaceptor/api/RedactionConfig.kt:8-9`

**Problem Statement**:
Potential concurrency bug. Modifying redaction config while interceptor reads it can cause `ConcurrentModificationException`.

**Proposed Solution**:

```kotlin
object RedactionConfig {
    private val _headersToRedact = ConcurrentHashMap.newKeySet<String>()
    private val _bodyPatternsToRedact = ConcurrentHashMap.newKeySet<String>()

    val headersToRedact: Set<String> get() = _headersToRedact.toSet()
    val bodyPatternsToRedact: Set<String> get() = _bodyPatternsToRedact.toSet()

    fun redactHeader(vararg names: String): RedactionConfig {
        _headersToRedact.addAll(names)
        return this
    }

    // Or use immutable data class with copy-on-write
}
```

**Estimated Effort**: 1 day

---

## Summary and Recommendations

### Immediate Actions (Next 2 Weeks)
1. **SEC-001**: Implement database encryption (security)
2. **ARCH-002**: Remove runBlocking from interceptor (performance)
3. **Fix CrashReporter**: Replace Thread.sleep with synchronous write (reliability)

### Short-Term (1-2 Months)
4. **PERF-001**: Implement streaming blob storage (memory)
5. **Replace CoreHolder**: Integrate Hilt (testability)
6. **Add Unit Tests**: Start with critical business logic (quality)

### Medium-Term (3-6 Months)
7. **Add Integration Tests**: End-to-end flows (quality)
8. **Add UI Tests**: Compose screen testing (quality)
9. **Pagination**: Paging 3 integration (scalability)
10. **FTS Search**: Full-text search optimization (performance)

### Long-Term (Ongoing)
11. **Code Cleanup**: Extract utilities, reduce duplication (maintainability)
12. **Externalize Config**: Move hard-coded values to config (flexibility)

### Metrics to Track

**Code Quality Metrics**:
- Test coverage (target: >70%)
- Code duplication (target: <5%)
- Cyclomatic complexity (target: <10 per method)

**Performance Metrics**:
- Memory usage during capture (target: <50MB)
- Intercept latency (target: <5ms added)
- Database query time (target: <100ms for 10k records)

**Security Metrics**:
- Security vulnerabilities (target: 0 critical)
- Sensitive data exposure (target: 0 plaintext)

## Conclusion

WormaCeptor V2 has a solid architectural foundation but carries technical debt in three critical areas:
1. **Security**: Plaintext storage needs encryption
2. **Performance**: Threading violations and memory issues
3. **Testing**: Insufficient test coverage

Addressing the Critical and High priority items will significantly improve the codebase quality, security, and maintainability. The recommended approach is to tackle these systematically over the next 3-6 months while continuing feature development.

The project already demonstrates awareness of its technical debt (TECH_DEBT_REGISTER.yaml), which is a positive sign. Prioritizing these improvements will position WormaCeptor as a production-grade, enterprise-ready debugging tool.

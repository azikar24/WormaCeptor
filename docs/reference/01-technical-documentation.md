# WormaCeptor V2 - Technical Documentation

## System Overview

### Purpose

WormaCeptor V2 is a production-safe, modular network inspection library for Android applications. It captures HTTP traffic and application crashes during development, providing developers with a rich debugging interface while maintaining zero overhead in production builds.

### Design Philosophy

1. **Strict Separation of Concerns**: Clear boundaries between API (what you call) and Implementation (how it works)
2. **Zero Production Impact**: Reflection-based runtime discovery ensures no debug code exists in release builds
3. **Crash-Safe Operation**: Network interception never disrupts application flow, even when errors occur
4. **Build-Variant Awareness**: Gradle dependency management automatically excludes implementation from release builds
5. **Clean Architecture**: Framework-agnostic domain layer with dependency inversion throughout

### Application Type

Android Library (AAR) designed for integration into Android applications as a development dependency.

## Architecture Overview

WormaCeptor V2 follows Clean Architecture principles with strict layer separation:

```
+---------------------------------------------------------------------+
|                         HOST APPLICATION                            |
|  (Your Android App - integrates via Gradle dependency)              |
+-----------------------------+---------------------------------------+
                              |
                 +------------v------------+
                 |   API LAYER             |
                 |   :api:client           |<---- Always included
                 |   Public interfaces     |
                 +------------+------------+
                              | Reflection Discovery
                 +------------v----------------------+
                 |   IMPLEMENTATION LAYER            |
                 |   :api:impl:persistence (SQLite)  |<- debugImplementation
                 |   :api:impl:imdb (In-Memory)      |<- optional
                 |   :api:impl:no-op (No-Op)         |<- fallback
                 +------------+---------------------+
                              |
                 +------------v------------+
                 |   CORE LAYER            |
                 |   :core:engine          |<---- Business Logic
                 |   Orchestration         |
                 +-----+----------+--------+
                       |          |
         +-------------v--+    +--v---------------------+
         |  DOMAIN LAYER  |    |  INFRASTRUCTURE       |
         |  :domain:*     |    |  :infra:*             |
         |  Pure Kotlin   |    |  Android/Room/OkHttp  |
         +----------------+    +-----+------------------+
                                     |
                          +----------v------------+
                          |  FEATURE LAYER        |
                          |  :features:viewer     |
                          |  Jetpack Compose UI   |
                          +-----------------------+
```

### Layer Responsibilities

**API Layer** (`api/*`)
- Defines public interfaces that host applications interact with
- Minimal dependencies (only domain entities and framework libraries)
- Reflection-based service discovery for implementation modules

**Domain Layer** (`domain/*`)
- Pure Kotlin data models with no Android dependencies
- Repository contracts (interfaces) defining data access patterns
- No business logic - only data structures and contracts

**Core Layer** (`core/engine`)
- Business logic orchestration (CaptureEngine, QueryEngine, CrashReporter)
- Depends on domain contracts, not concrete implementations
- Framework-agnostic algorithms and workflows

**Infrastructure Layer** (`infra/*`)
- Concrete implementations of domain contracts
- Room database, filesystem storage, OkHttp adapters
- Platform-specific code isolated here

**Feature Layer** (`features/*`)
- User-facing UI components built with Jetpack Compose
- ViewModels for state management
- Navigation and presentation logic

**Platform Layer** (`platform/android`)
- Android-specific utilities (ShakeDetector, lifecycle management)
- Sensor access and system integration

## Module Structure and Responsibilities

### API Modules

#### :api:client
**Purpose**: Public-facing API for host applications

**Key Components**:
- `WormaCeptorApi` - Singleton entry point for initialization and viewer access
- `WormaCeptorInterceptor` - OkHttp interceptor for capturing HTTP traffic
- `ServiceProvider` - Interface for implementation discovery
- `RedactionConfig` - Configuration for sensitive data masking

**Dependencies**:
- `:domain:entities` - Core data models
- `okhttp3` - OkHttp interceptor API

**File**: `api/client/src/main/java/com/azikar24/wormaceptor/api/WormaCeptorApi.kt`

#### :api:impl:persistence
**Purpose**: SQLite-backed implementation with persistent storage

**Key Components**:
- `ServiceProviderImpl` - Discovered via reflection at runtime
- Coordinates Room database, blob storage, and notification system
- Used in `debugImplementation` dependency configuration

**Dependencies**:
- `:api:client` - Service provider interface
- `:core:engine` - Business logic
- `:infra:persistence:sqlite` - Database implementation
- `:features:viewer` - UI components

**Reflection Name**: `com.azikar24.wormaceptor.api.internal.ServiceProviderImpl`

#### :api:impl:imdb
**Purpose**: In-memory database alternative for faster, non-persistent debugging

**Use Case**: QA environments or temporary debugging sessions where persistence is unnecessary

**Dependencies**:
- `:api:client`
- `:core:engine`

#### :api:impl:no-op
**Purpose**: Empty implementation for release builds

**Characteristics**:
- Zero logic, zero overhead
- Fallback when no implementation is discovered
- Ensures production builds have no debug code

**Dependencies**: `:api:client` only

### Domain Modules

#### :domain:entities
**Purpose**: Core data models with no framework dependencies

**Key Models**:
```kotlin
data class NetworkTransaction(
    val id: UUID,
    val request: Request,
    val response: Response?,
    val timestamp: EpochMillis,
    val duration: Long?,
    val status: TransactionStatus
)

data class Request(
    val url: String,
    val method: String,
    val headers: Map<String, String>,
    val bodyBlobId: BlobID?,
    val bodySize: Long
)

data class Response(
    val code: Int,
    val message: String,
    val headers: Map<String, String>,
    val bodyBlobId: BlobID?,
    val bodySize: Long,
    val protocol: String?,
    val tlsVersion: String?
)

data class Crash(
    val timestamp: EpochMillis,
    val exceptionType: String,
    val message: String?,
    val stackTrace: String
)
```

**Type Aliases**:
- `BlobID = String` - Identifier for stored request/response bodies
- `EpochMillis = Long` - Unix timestamp in milliseconds

**Enums**:
- `TransactionStatus` - ACTIVE, COMPLETED, FAILED

**Dependencies**: None (pure Kotlin)

#### :domain:contracts
**Purpose**: Repository interfaces defining data access patterns

**Key Interfaces**:
```kotlin
interface TransactionRepository {
    fun getAllTransactions(): Flow<List<TransactionSummary>>
    suspend fun getTransactionById(id: UUID): NetworkTransaction?
    suspend fun saveTransaction(transaction: NetworkTransaction)
    suspend fun searchTransactions(query: String): Flow<List<TransactionSummary>>
    suspend fun deleteAllTransactions()
}

interface CrashRepository {
    fun getAllCrashes(): Flow<List<Crash>>
    suspend fun saveCrash(crash: Crash)
    suspend fun deleteAllCrashes()
}

interface BlobStorage {
    suspend fun saveBlob(stream: InputStream): BlobID
    suspend fun readBlob(id: BlobID): InputStream?
    suspend fun deleteBlob(id: BlobID)
}
```

**Dependencies**: `:domain:entities`

### Core Modules

#### :core:engine
**Purpose**: Business logic and orchestration

**Key Components**:

**CaptureEngine** - Handles transaction capture
```kotlin
class CaptureEngine(
    private val repository: TransactionRepository,
    private val blobStorage: BlobStorage
) {
    suspend fun startTransaction(request: Request): UUID
    suspend fun completeTransaction(id: UUID, response: Response)
}
```

**QueryEngine** - Data retrieval and search
```kotlin
class QueryEngine(
    private val repository: TransactionRepository,
    private val blobStorage: BlobStorage,
    private val crashRepository: CrashRepository
) {
    fun observeTransactions(): Flow<List<TransactionSummary>>
    fun observeCrashes(): Flow<List<Crash>>
    suspend fun getTransactionById(id: UUID): NetworkTransaction?
    suspend fun getBodyContent(blobId: BlobID): String?
    suspend fun search(query: String): Flow<List<TransactionSummary>>
}
```

**CrashReporter** - Uncaught exception handler
```kotlin
class CrashReporter(
    private val crashRepository: CrashRepository,
    private val originalHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable)
}
```

**CoreHolder** - Global singleton for engine access
```kotlin
object CoreHolder {
    @Volatile var captureEngine: CaptureEngine? = null
    @Volatile var queryEngine: QueryEngine? = null
}
```

**Dependencies**: `:domain:entities`, `:domain:contracts`

### Infrastructure Modules

#### :infra:persistence:sqlite
**Purpose**: Room database and filesystem storage implementations

**Key Components**:

**WormaCeptorDatabase** - Room database
```kotlin
@Database(
    entities = [TransactionEntity::class, CrashEntity::class],
    version = 2
)
abstract class WormaCeptorDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun crashDao(): CrashDao
}
```

**RoomTransactionRepository** - TransactionRepository implementation
- Converts between Room entities and domain models
- Provides Flow-based reactive queries
- Implements search using SQL LIKE

**FileSystemBlobStorage** - BlobStorage implementation
- Stores request/response bodies as individual files
- Uses Android internal storage
- Directory: `<internal_storage>/wormaceptor_blobs/`

**InMemoryTransactionRepository** - Alternative for IMDB mode
**InMemoryBlobStorage** - RAM-based blob storage

**Dependencies**:
- `:domain:entities`, `:domain:contracts`
- `androidx.room` (2.6.1)

**File**: `infra/persistence/sqlite/src/main/java/com/azikar24/wormaceptor/infra/persistence/sqlite/WormaCeptorDatabase.kt`

#### :infra:networking:okhttp
**Purpose**: OkHttp-specific adapters and utilities

**Current State**: Minimal implementation, placeholder for future extensions

#### :infra:parser:json
**Purpose**: JSON parsing and formatting utilities

**Future**: GraphQL support planned

#### :infra:parser:protobuf
**Purpose**: Protocol buffer parsing for gRPC support

**Current State**: Placeholder module

### Feature Modules

#### :features:viewer
**Purpose**: Jetpack Compose UI for viewing transactions and crashes

**Key Screens**:
- `HomeScreen` - Tab container (Transactions/Crashes)
- `TransactionListScreen` - List of captured HTTP requests
- `TransactionDetailScreen` - Detailed view with Overview/Request/Response tabs
- `CrashListScreen` - List of captured crashes
- `CrashDetailScreen` - Full crash details with stacktrace

**ViewerViewModel** - State management
```kotlin
class ViewerViewModel(
    private val queryEngine: QueryEngine
) : ViewModel() {
    val transactions: StateFlow<List<TransactionSummary>>
    val crashes: StateFlow<List<Crash>>
    val searchQuery: StateFlow<String>
    val filterMethod: StateFlow<String?>
    val filterStatusRange: StateFlow<String?>

    fun search(query: String)
    fun filterByMethod(method: String?)
    fun filterByStatusRange(range: String?)
    fun clearFilters()
}
```

**ExportManager** - JSON export and share functionality

**Dependencies**:
- `:core:engine` (QueryEngine access)
- `:domain:entities` (data models)
- Jetpack Compose (UI framework)
- Material 3 (design system)
- Navigation Compose (screen navigation)

**File**: `features/viewer/src/main/java/com/azikar24/wormaceptor/feature/viewer/ViewerActivity.kt`

#### :features:settings
**Current State**: Placeholder (build.gradle.kts exists, no implementation)

**Planned Use**: Configuration UI for retention policies, redaction rules, etc.

#### :features:sharing
**Current State**: Placeholder

**Planned Use**: Advanced export formats (HAR, Postman, etc.)

### Platform Modules

#### :platform:android
**Purpose**: Android-specific utilities

**ShakeDetector** - Accelerometer-based gesture detection
```kotlin
class ShakeDetector(
    private val context: Context,
    private val onShake: () -> Unit
) {
    fun start()
    fun stop()
}
```
- Threshold: 2.7g
- Cooldown: 500ms between shakes

**Dependencies**: AndroidX Core

## Data Flow Patterns

### Write Path (Capture Flow)

```
1. OkHttp Request Created
   |
   v
2. WormaCeptorInterceptor.intercept() called
   |
   +- Read request body from okio.Buffer
   +- Apply redaction (headers + body regex)
   +- Call provider.startTransaction()
   |  |
   |  v
   |  CaptureEngine.startTransaction()
   |  +- Save body to BlobStorage -> returns BlobID
   |  +- Create NetworkTransaction with Request
   |  +- Save to TransactionRepository (Room)
   |
   v
3. Execute network call: chain.proceed(request)
   |
   v
4. OkHttp Response Received
   |
   v
5. Interceptor captures response
   |
   +- Peek response body (maxContentLength)
   +- Apply redaction
   +- Extract protocol and TLS version
   +- Call provider.completeTransaction()
   |  |
   |  v
   |  CaptureEngine.completeTransaction()
   |  +- Save body to BlobStorage -> returns BlobID
   |  +- Update transaction with Response + duration
   |  +- Save to TransactionRepository
   |  +- Show notification (if enabled)
   |
   v
6. Response returned to application
```

**Storage Separation**:
- **Metadata** (URL, headers, status) -> SQLite database
- **Bodies** (request/response payloads) -> Filesystem blobs

### Read Path (Query Flow)

```
1. User opens ViewerActivity
   |
   v
2. ViewerViewModel created
   |
   +- Access CoreHolder.queryEngine
   |
   v
3. QueryEngine.observeTransactions() called
   |
   v
4. TransactionRepository.getAllTransactions() returns Flow
   |
   v
5. Room DAO emits List<TransactionEntity>
   |
   +- Mapped to domain models
   |
   v
6. Flow emitted to ViewModel
   |
   v
7. StateFlow updated
   |
   v
8. Compose UI recomposes with new data
   |
   v
9. User taps transaction
   |
   v
10. Navigate to TransactionDetailScreen
    |
    +- QueryEngine.getTransactionById(id)
    |
    v
11. User views request/response body
    |
    +- QueryEngine.getBodyContent(blobId)
    +- BlobStorage.readBlob(blobId) reads file
    |
    v
12. Body displayed with JSON formatting
```

### Search/Filter Flow

```
1. User enters search query
   |
   v
2. ViewerViewModel.search(query) called
   |
   +- Update searchQuery StateFlow
   |
   v
3. Combined Flow (transactions + query + filters)
   |
   +- QueryEngine.search(query) for DB search
   +- In-memory filtering for method/status
   |
   v
4. Filtered results emitted
   |
   v
5. UI updates with filtered list
```

### Crash Reporting Flow

```
1. Uncaught exception thrown
   |
   v
2. CrashReporter.uncaughtException() called
   |
   +- Extract stack trace via StringWriter
   +- Create Crash entity
   +- Launch coroutine: CrashRepository.saveCrash()
   +- Thread.sleep(500) to allow DB write
   |
   v
3. Delegate to original exception handler
   |
   +- OR System.exit(2) if no original handler
```

## Key Components and Interactions

### Initialization Sequence

```kotlin
// In MyApplication.onCreate()
WormaCeptorApi.init(this, logCrashes = true)

// Internal flow:
1. Try reflection: Class.forName("ServiceProviderImpl")
2. If found:
   a. ServiceProviderImpl.init(context, logCrashes)
   b. Create Room database (wormaceptor-v2.db)
   c. Initialize repositories (Room + Blob)
   d. Create CaptureEngine(repository, blobStorage)
   e. Create QueryEngine(repository, blobStorage, crashRepo)
   f. Store in CoreHolder
   g. If logCrashes: Register CrashReporter
3. If not found:
   a. Log warning
   b. Use NoOpProvider (no functionality)
```

### OkHttp Interception

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(
        WormaCeptorInterceptor()
            .showNotification(true)
            .maxContentLength(250_000L)
            .retainDataFor(Period.ONE_WEEK)
            .redactHeader("Authorization")
            .redactBody("\"password\":\\s*\"[^\"]*\"")
    )
    .build()

// Internal flow per request:
1. Read request (body, headers, URL)
2. Apply redaction patterns
3. Start transaction (save to DB)
4. Proceed with network call
5. Capture response
6. Apply redaction
7. Complete transaction (update DB)
8. Show notification (optional)
9. Return response to app
```

### UI Navigation

```kotlin
// ViewerActivity navigation graph:
NavHost {
    composable("home") { HomeScreen() }
    composable("detail/{id}") { TransactionDetailScreen(id) }
    composable("crash/{timestamp}") { CrashDetailScreen(timestamp) }
}

// HomeScreen tabs:
- Transactions tab -> TransactionListScreen
- Crashes tab -> CrashListScreen

// Tap transaction -> navigate("detail/$id")
// Tap crash -> navigate("crash/$timestamp")
```

## Technology Stack

### Language and Build System

| Component | Version | Purpose |
|-----------|---------|---------|
| Kotlin | 2.0.21 | Primary language |
| JVM Target | 17 | Compilation target |
| Gradle | 8.5.2 | Build system |
| Gradle Plugin | 8.6.1 | Android build tools |

### Android Framework

| Component | Version | Purpose |
|-----------|---------|---------|
| Min SDK | 23 | Minimum Android 6.0 |
| Target SDK | 36 | Latest Android |
| Compile SDK | 34 | Compile target |
| AndroidX Core | 1.13.1 | Core libraries |
| Lifecycle | 2.8.7 | ViewModel, LiveData |
| Activity Compose | 1.9.3 | Compose integration |

### UI Framework

| Component | Version | Purpose |
|-----------|---------|---------|
| Compose BOM | 2024.10.01 | Compose version management |
| Material 3 | Latest | Design system |
| Navigation Compose | 2.8.3 | Screen navigation |
| Accompanist | 0.36.0 | System UI controller |

### Persistence

| Component | Version | Purpose |
|-----------|---------|---------|
| Room | 2.6.1 | SQLite ORM |
| Room KSP | 2.6.1 | Annotation processor |
| Kotlinx Serialization | 1.6.3 | JSON serialization |

### Networking

| Component | Version | Purpose |
|-----------|---------|---------|
| OkHttp | 4.12.0 | HTTP client + interceptor |
| Retrofit | 2.9.0 | REST client (demo app) |
| Okio | 3.x | Efficient I/O |

### Concurrency

| Component | Version | Purpose |
|-----------|---------|---------|
| Coroutines | 1.8.1 | Async programming |
| Coroutines Android | 1.8.1 | Android integration |
| Flow | (included) | Reactive streams |

### Testing

| Component | Version | Purpose |
|-----------|---------|---------|
| JUnit | 4.13.2 | Unit testing |
| Mockk | 1.13.12 | Mocking |
| Coroutines Test | 1.8.1 | Coroutine testing |
| ArchUnit | 1.3.0 | Architecture validation |

## Configuration Guide

### Gradle Setup

**1. Add Library as Module**
```kotlin
// settings.gradle.kts
include(":wormaceptor")
project(":wormaceptor").projectDir = file("../WormaCeptor")
```

**2. Add Dependencies**
```kotlin
// app/build.gradle.kts
dependencies {
    // API always included (lightweight interface)
    implementation(project(":wormaceptor:api:client"))

    // Implementation only in debug builds
    debugImplementation(project(":wormaceptor:api:impl:persistence"))

    // Alternative: In-memory mode
    // debugImplementation(project(":wormaceptor:api:impl:imdb"))
}
```

**3. ProGuard/R8 Rules** (if using code shrinking in debug)
```proguard
# Keep ServiceProviderImpl for reflection
-keep class com.azikar24.wormaceptor.api.internal.ServiceProviderImpl { *; }

# Keep Room entities
-keep class com.azikar24.wormaceptor.infra.persistence.sqlite.** { *; }
```

### Application Initialization

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize WormaCeptor
        WormaCeptorApi.init(
            context = this,
            logCrashes = true  // Enable crash logging
        )

        // Optional: Configure redaction globally
        WormaCeptorApi.redactionConfig
            .redactHeader("Authorization", "X-Api-Key", "Cookie")
            .redactBody("\"(password|token|ssn)\":\\s*\"[^\"]*\"")
            .replacement("[REDACTED]")
    }
}
```

### OkHttp Integration

```kotlin
class NetworkModule {
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(
                WormaCeptorInterceptor()
                    // Show persistent notification while capturing
                    .showNotification(true)

                    // Limit body capture to 250KB
                    .maxContentLength(250_000L)

                    // Auto-delete old transactions
                    .retainDataFor(Period.ONE_WEEK)

                    // Redact sensitive headers
                    .redactHeader("Authorization")
                    .redactHeader("X-Api-Key")
                    .redactHeader("Cookie")

                    // Redact sensitive body content (regex)
                    .redactBody("\"password\":\\s*\"[^\"]*\"")
                    .redactBody("\"creditCard\":\\s*\"[^\"]*\"")
                    .redactBody("\"ssn\":\\s*\"\\d{3}-\\d{2}-\\d{4}\"")
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
```

### Retention Policies

```kotlin
enum class Period {
    ONE_HOUR,      // 3600000 ms
    ONE_DAY,       // 86400000 ms
    ONE_WEEK,      // 604800000 ms
    ONE_MONTH,     // 2592000000 ms
    FOREVER        // No auto-deletion
}
```

### Opening the Viewer

**Option 1: Shake Gesture** (auto-registered)
```kotlin
// Shake device while app is running
// Automatically opens viewer if initialized
```

**Option 2: Programmatic**
```kotlin
// From Activity or Fragment
WormaCeptorApi.openViewer(context)
```

**Option 3: Debug Menu**
```kotlin
// Add button in debug drawer
if (WormaCeptorApi.isInitialized()) {
    Button(onClick = { WormaCeptorApi.openViewer(context) }) {
        Text("Open Network Inspector")
    }
}
```

## Summary

WormaCeptor V2 is a well-architected Android debugging library that successfully balances functionality, performance, and production safety. The modular structure, clean architecture, and reflection-based discovery enable powerful debugging capabilities without compromising release builds.

Key strengths:
- Strict architectural boundaries
- Zero production overhead
- Extensible design
- Modern Android tech stack
- Reactive data flow

For detailed feature information, see [Feature Inventory](02-feature-inventory.md).

For improvement opportunities, see [Technical Debt & Improvements](05-technical-debt-improvements.md).

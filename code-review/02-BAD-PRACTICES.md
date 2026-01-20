# Bad Practices Analysis

This document lists bad practices and anti-patterns found in the WormaCeptor codebase.

---

## Critical Issues

### 1. Extensive Use of `runBlocking` in Production Code

**Severity:** Critical - Can cause ANR (Application Not Responding)

**Files:**
- `api/impl/imdb/.../ServiceProviderImpl.kt:49,54,73,77,85,90,95`
- `api/impl/persistence/.../ServiceProviderImpl.kt:65,80,101,105,113,118,123`

**Problem:** Using `runBlocking` blocks the calling thread, which is an anti-pattern for coroutines.

**Bad Code:**
```kotlin
override fun startTransaction(...): UUID? = runBlocking {
    captureEngine?.startTransaction(url, method, headers, bodyStream, bodySize)
}

override fun completeTransaction(...) {
    runBlocking {
        captureEngine?.completeTransaction(...)
        val transaction = queryEngine?.getDetails(id)
        if (transaction != null) {
            notificationHelper?.show(transaction)
        }
    }
}
```

**Fix:**
```kotlin
// Make the interface suspend functions
interface ServiceProvider {
    suspend fun startTransaction(...): UUID?
    suspend fun completeTransaction(...)
}

// Implementation
override suspend fun startTransaction(...): UUID? {
    return captureEngine?.startTransaction(url, method, headers, bodyStream, bodySize)
}

override suspend fun completeTransaction(...) {
    captureEngine?.completeTransaction(...)
    val transaction = queryEngine?.getDetails(id)
    if (transaction != null) {
        withContext(Dispatchers.Main) {
            notificationHelper?.show(transaction)
        }
    }
}
```

---

### 2. God Object Pattern - CoreHolder as Global Mutable State

**Severity:** Critical - Violates SOLID, hard to test

**File:** `core/engine/src/main/java/com/azikar24/wormaceptor/core/engine/CoreHolder.kt`

**Problem:** Using a global mutable singleton violates dependency injection principles, makes testing difficult, and creates tight coupling.

**Bad Code:**
```kotlin
object CoreHolder {
    @Volatile
    var captureEngine: CaptureEngine? = null

    @Volatile
    var queryEngine: QueryEngine? = null
}
```

**Fix:** Use proper dependency injection with Hilt or Koin:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    @Singleton
    fun provideCaptureEngine(
        repository: TransactionRepository,
        blobStorage: BlobStorage
    ): CaptureEngine = CaptureEngine(repository, blobStorage)

    @Provides
    @Singleton
    fun provideQueryEngine(
        repository: TransactionRepository,
        blobStorage: BlobStorage,
        crashRepository: CrashRepository
    ): QueryEngine = QueryEngine(repository, blobStorage, crashRepository)
}

// Inject where needed
@AndroidEntryPoint
class ViewerActivity : ComponentActivity() {
    @Inject lateinit var queryEngine: QueryEngine
}
```

---

### 3. Unsafe Null Assertion (!!) Chaining

**Severity:** Critical - Can cause NullPointerException

**Files:** 4 files with 941 occurrences detected

**Examples:**
- `features/viewer/.../ViewerActivity.kt:50` - `CoreHolder.queryEngine!!`
- `features/viewer/.../ui/HomeScreen.kt:120,121,149` - `CoreHolder.queryEngine!!`

**Bad Code:**
```kotlin
return ViewerViewModel(CoreHolder.queryEngine!!) as T
val allTransactionsForExport = CoreHolder.queryEngine!!.getAllTransactionsForExport()
```

**Fix:**
```kotlin
// Option 1: Proper null handling
return CoreHolder.queryEngine?.let { engine ->
    ViewerViewModel(engine) as T
} ?: throw IllegalStateException("QueryEngine not initialized")

// Option 2: Use requireNotNull with message
val queryEngine = requireNotNull(CoreHolder.queryEngine) {
    "QueryEngine must be initialized before creating ViewModel"
}
return ViewerViewModel(queryEngine) as T
```

---

### 4. Magic Numbers Everywhere

**Severity:** Critical - Hard to maintain, no clear meaning

**Files:** 28 files detected with hardcoded values

**Examples:**

| File | Line | Magic Number |
|------|------|--------------|
| `WormaCeptorInterceptor.kt` | 14 | `250_000L` |
| `TransactionDetailScreen.kt` | 1795-1796 | `500_000`, `100_000` |
| `GlitchMeltdownEffect.kt` | 224-230 | Time calculations |
| `LongPressModifier.kt` | various | `500L`, `0.96f`, `0.94f` |

**Bad Code:**
```kotlin
private var maxContentLength = 250_000L

if (json.length > 500_000) {
    return json.take(100_000) + "\n\n... (Rest truncated) ..."
}

Period.ONE_HOUR -> 60 * 60 * 1000L
Period.ONE_DAY -> 24 * 60 * 60 * 1000L
```

**Fix:**
```kotlin
object WormaCeptorConstants {
    object Network {
        const val MAX_CONTENT_LENGTH = 250_000L
        const val JSON_FORMAT_SIZE_THRESHOLD = 500_000
        const val JSON_PREVIEW_SIZE = 100_000
    }

    object Time {
        const val MILLIS_PER_HOUR = 60 * 60 * 1000L
        const val MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR
        const val MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY
    }

    object UI {
        const val LONG_PRESS_THRESHOLD_MS = 500L
        const val PRESS_SCALE = 0.96f
        const val LONG_PRESS_SCALE = 0.94f
    }
}
```

---

## Important Issues

### 5. Hardcoded Magic Bytes for Binary Detection

**Severity:** Important - Hard to maintain

**File:** `api/client/.../WormaCeptorInterceptor.kt:49-87`

**Bad Code:**
```kotlin
private fun isBinaryByMagicBytes(data: ByteArray): Boolean {
    if (data.size < 8) return false
    return when {
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        data[0] == 0x89.toByte() && data[1] == 0x50.toByte() &&
                data[2] == 0x4E.toByte() && data[3] == 0x47.toByte() -> true
        // JPEG: FF D8 FF
        data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() &&
                data[2] == 0xFF.toByte() -> true
        ...
    }
}
```

**Fix:**
```kotlin
sealed class MagicBytes(val signature: ByteArray, val name: String) {
    object PNG : MagicBytes(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A), "PNG")
    object JPEG : MagicBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()), "JPEG")
    object GIF : MagicBytes(byteArrayOf(0x47, 0x49, 0x46, 0x38), "GIF")
    object PDF : MagicBytes(byteArrayOf(0x25, 0x50, 0x44, 0x46), "PDF")

    companion object {
        val ALL = listOf(PNG, JPEG, GIF, PDF)
    }
}

private fun isBinaryByMagicBytes(data: ByteArray): Boolean {
    if (data.size < 8) return false
    return MagicBytes.ALL.any { magic ->
        data.size >= magic.signature.size &&
            magic.signature.indices.all { i -> data[i] == magic.signature[i] }
    }
}
```

---

### 6. Poor Error Handling - Silent Failures

**Severity:** Important - Hard to debug in production

**File:** `api/client/.../WormaCeptorInterceptor.kt:126-128,204-206`

**Bad Code:**
```kotlin
} catch (e: Exception) {
    e.printStackTrace()
}
```

**Fix:**
```kotlin
private val logger = Logger.getLogger(WormaCeptorInterceptor::class.java.name)

} catch (e: Exception) {
    logger.log(Level.SEVERE, "Failed to capture request", e)
    // Or use Android Log
    android.util.Log.e("WormaCeptor", "Failed to capture request", e)
}
```

---

### 7. Thread Safety Violation - TOCTOU Race Condition

**Severity:** Important - Potential race condition

**File:** `core/engine/.../CoreHolder.kt:3-9` and both `ServiceProviderImpl.kt`

**Problem:** While fields are `@Volatile`, there's no synchronization during initialization. Multiple threads can check for null and attempt initialization simultaneously.

**Bad Code:**
```kotlin
if (captureEngine != null) return  // Check

// ... initialization ...

CoreHolder.captureEngine = captureEngine  // Set
CoreHolder.queryEngine = queryEngine
```

**Fix:**
```kotlin
object CoreHolder {
    private val _captureEngine = AtomicReference<CaptureEngine?>()
    private val _queryEngine = AtomicReference<QueryEngine?>()

    fun initializeOnce(
        captureEngineProvider: () -> CaptureEngine,
        queryEngineProvider: () -> QueryEngine
    ) {
        _captureEngine.compareAndSet(null, captureEngineProvider())
        _queryEngine.compareAndSet(null, queryEngineProvider())
    }

    val captureEngine: CaptureEngine? get() = _captureEngine.get()
    val queryEngine: QueryEngine? get() = _queryEngine.get()
}
```

---

### 8. Hardcoded Strings in UI

**Severity:** Important - Breaks i18n/l10n

**File:** `features/viewer/.../ui/HomeScreen.kt`

**Bad Code:**
```kotlin
Text("WormaCeptor V2")
Text("Export Transactions")
Text("Clear All Transactions")
```

**Fix:**
```xml
<!-- res/values/strings.xml -->
<resources>
    <string name="app_name">WormaCeptor V2</string>
    <string name="action_export_transactions">Export Transactions</string>
    <string name="action_clear_all_transactions">Clear All Transactions</string>
</resources>
```

```kotlin
Text(stringResource(R.string.app_name))
Text(stringResource(R.string.action_export_transactions))
```

---

### 9. Non-Idiomatic Kotlin - Using Java APIs

**Severity:** Minor - Not idiomatic

**File:** `platform/android/.../ShakeDetector.kt:40`

**Bad Code:**
```kotlin
val gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()
```

**Fix:**
```kotlin
import kotlin.math.sqrt

val gForce = sqrt(gX * gX + gY * gY + gZ * gZ)
```

---

### 10. Unsafe Force Unwrap in Shake Detector

**Severity:** Important - Potential NPE

**File:** `platform/android/.../ShakeDetector.kt:54`

**Bad Code:**
```kotlin
override fun onSensorChanged(event: SensorEvent) {
    if (mListener != null) {
        ...
        mListener!!.onShake(mShakeCount)  // Already checked but unsafe
    }
}
```

**Fix:**
```kotlin
override fun onSensorChanged(event: SensorEvent) {
    val listener = mListener ?: return

    val x = event.values[0]
    val y = event.values[1]
    val z = event.values[2]
    // ... calculations ...

    listener.onShake(mShakeCount)
}
```

---

### 11. Overly Complex Function - High Cyclomatic Complexity

**Severity:** Important - Hard to test/maintain

**File:** `features/viewer/.../ui/TransactionDetailScreen.kt`

**Problem:** The `ResponseTab` function (lines 1132-1507) is 375 lines long.

**Fix:** Break into smaller composables:
```kotlin
@Composable
private fun ResponseTab(...) {
    Box(modifier = modifier.fillMaxSize()) {
        ResponseContent(state = responseState, ...)
        ResponseFloatingActions(isVisible = ..., onCopyAll = ...)
        ResponseViewers(state = responseState, onDismiss = ...)
    }
}

@Composable
private fun ResponseContent(...) { ... }

@Composable
private fun ResponseFloatingActions(...) { ... }

@Composable
private fun ResponseViewers(...) { ... }
```

---

### 12. Unused Variable Causes Recomposition

**Severity:** Minor - Performance impact

**File:** `features/viewer/.../ui/components/TextWithStartEllipsis.kt:37`

**Bad Code:**
```kotlin
val textMeasurer = rememberTextMeasurer()
LocalDensity.current  // Unused - causes subscription to density changes
```

**Fix:**
```kotlin
val textMeasurer = rememberTextMeasurer()
// Remove: LocalDensity.current
```

---

### 13. Resource Leak - InputStream Not Closed

**Severity:** Important - Potential resource leak

**File:** `api/client/.../WormaCeptorInterceptor.kt:111-117`

**Bad Code:**
```kotlin
val bodyStream = bodyText.byteInputStream()  // Never closed
provider.startTransaction(..., bodyStream = bodyStream, ...)
```

**Fix:**
```kotlin
bodyText.byteInputStream().use { stream ->
    transactionId = provider.startTransaction(
        url = request.url.toString(),
        method = request.method,
        headers = cleanHeaders,
        bodyStream = stream,
        bodySize = bodySize
    )
}
```

---

## Summary

| Severity | Count | Categories |
|----------|-------|------------|
| Critical | 4 | runBlocking, CoreHolder, null assertions, magic numbers |
| Important | 9 | Error handling, thread safety, hardcoded strings, complexity |
| Minor | 2 | Non-idiomatic Kotlin, unused variables |
| **Total** | **15** | |

## Priority Recommendations

1. **Immediate:** Replace `runBlocking` with proper coroutine patterns
2. **High:** Implement dependency injection to replace CoreHolder
3. **High:** Replace `!!` operators with safe null handling
4. **Medium:** Extract magic numbers to constants
5. **Medium:** Improve error handling with proper logging
6. **Low:** Refactor large functions for testability

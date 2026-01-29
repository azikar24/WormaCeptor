---
name: perf-analyzer
description: Analyze and optimize WormaCeptor's own performance - startup time, memory footprint, UI jank, and impact on host apps. Use when investigating performance issues, optimizing critical paths, or measuring overhead.
---

# Performance Analyzer

Analyze and optimize WormaCeptor's performance and impact on host apps.

## When to Use

- Investigating performance issues
- Optimizing critical code paths
- Measuring WormaCeptor's overhead on host apps
- Profiling startup time or memory usage
- Fixing UI jank in the inspector

## Performance Goals

| Metric | Target | Critical Threshold |
|--------|--------|-------------------|
| Initialization time | < 50ms | < 100ms |
| Memory overhead (idle) | < 5MB | < 10MB |
| Per-request overhead | < 1ms | < 5ms |
| UI frame time | < 16ms (60fps) | < 32ms (30fps) |
| Database query | < 10ms | < 50ms |

## Analysis Tools

### Android Studio Profiler

```bash
# Launch with profiler
./gradlew :app:installDebug
# Then: Run > Profile 'app'
```

**CPU Profiler:**
- Sample Java/Kotlin methods
- Trace system calls
- Record call stacks

**Memory Profiler:**
- Track allocations
- Detect leaks
- Analyze heap dumps

**Energy Profiler:**
- CPU wake locks
- Network activity
- Location requests

### Jetpack Macrobenchmark

For startup and runtime metrics:

```kotlin
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupWithWormaCeptor() = benchmarkRule.measureRepeated(
        packageName = "com.example.app",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()
    }
}
```

### Baseline Profiles

Generate baseline profiles for faster startup:

```kotlin
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = rule.collect(
        packageName = "com.azikar24.wormaceptor"
    ) {
        // Critical user journeys
        startActivityAndWait()
        // Navigate to transactions
        // Open transaction detail
        // Search
    }
}
```

### Custom Timing

```kotlin
// Inline timing for quick checks
inline fun <T> measureTimeMillisWithResult(block: () -> T): Pair<T, Long> {
    val start = System.nanoTime()
    val result = block()
    val duration = (System.nanoTime() - start) / 1_000_000
    return result to duration
}

// Usage
val (transactions, queryTime) = measureTimeMillisWithResult {
    repository.getTransactions()
}
Log.d("Perf", "Query took ${queryTime}ms")
```

## Critical Path Analysis

### 1. Initialization Path

```
WormaCeptorApi.init()
├── Service discovery (reflection)      [Target: <10ms]
├── Koin module loading                  [Target: <20ms]
├── Database initialization              [Target: <15ms]
└── Engine startup                       [Target: <5ms]
```

**Optimization strategies:**
- Lazy initialization for non-critical components
- Background thread for database setup
- Avoid class loading in hot path

```kotlin
// BEFORE: Eager initialization
class WormaCeptorApi {
    init {
        database = Room.databaseBuilder(...).build()  // Blocks main thread
    }
}

// AFTER: Lazy initialization
class WormaCeptorApi {
    private val database by lazy {
        Room.databaseBuilder(...).build()
    }
}
```

### 2. Interception Path

```
WormaCeptorInterceptor.intercept()
├── Create transaction entity            [Target: <0.1ms]
├── Clone request/response               [Target: <0.5ms]
├── Parse body (if enabled)              [Target: <0.5ms]
└── Persist to database                  [Target: <1ms async]
```

**Optimization strategies:**
- Never block network thread
- Async persistence with bounded queue
- Skip parsing for large bodies
- Reuse buffers

```kotlin
// BEFORE: Blocking persistence
override fun intercept(chain: Chain): Response {
    val response = chain.proceed(request)
    database.insert(transaction)  // Blocks network thread!
    return response
}

// AFTER: Async persistence
override fun intercept(chain: Chain): Response {
    val response = chain.proceed(request)
    scope.launch(Dispatchers.IO) {
        database.insert(transaction)
    }
    return response
}
```

### 3. UI Rendering Path

```
TransactionListScreen
├── Collect state from ViewModel         [Target: <1ms]
├── Diff list items                       [Target: <5ms]
├── Compose recomposition                 [Target: <8ms]
└── Draw frame                            [Target: <2ms]
```

**Optimization strategies:**
- Use `ImmutableList` to enable smart diffing
- Use `key` parameter in `LazyColumn`
- Avoid allocations in composition
- Use `derivedStateOf` for computed values

```kotlin
// BEFORE: Recomputes every frame
@Composable
fun TransactionList(transactions: List<Transaction>) {
    val filtered = transactions.filter { it.isError }  // Allocates every recomposition
    LazyColumn {
        items(filtered) { item ->
            TransactionRow(item)
        }
    }
}

// AFTER: Stable references, keyed items
@Composable
fun TransactionList(transactions: ImmutableList<Transaction>) {
    val filtered by remember(transactions) {
        derivedStateOf { transactions.filter { it.isError }.toImmutableList() }
    }
    LazyColumn {
        items(filtered, key = { it.id }) { item ->
            TransactionRow(item)
        }
    }
}
```

## Memory Optimization

### Identify Leaks

```kotlin
// Use LeakCanary (already in WormaCeptor)
// Check features/leak-canary module

// Common leak sources:
// 1. Context references in singletons
// 2. Unregistered callbacks/listeners
// 3. Coroutine scope not cancelled
// 4. View references in ViewModel
```

### Reduce Allocations

```kotlin
// BEFORE: Allocates on every call
fun formatSize(bytes: Long): String {
    return String.format("%.2f MB", bytes / 1_000_000.0)
}

// AFTER: Reuse formatter
private val sizeFormatter = DecimalFormat("0.00")
fun formatSize(bytes: Long): String {
    return "${sizeFormatter.format(bytes / 1_000_000.0)} MB"
}
```

### Large Object Handling

```kotlin
// For large response bodies
class PaginatedBodyView {
    // Load chunks on demand, not entire body
    fun loadPage(pageIndex: Int, pageSize: Int): String {
        return body.substring(
            pageIndex * pageSize,
            minOf((pageIndex + 1) * pageSize, body.length)
        )
    }
}
```

## Database Optimization

### Query Analysis

```kotlin
// Enable query logging
Room.databaseBuilder(...)
    .setQueryCallback({ sql, args ->
        Log.d("RoomQuery", "SQL: $sql, Args: $args")
    }, Executors.newSingleThreadExecutor())
    .build()
```

### Index Strategy

```kotlin
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["timestamp"]),           // Sort by time
        Index(value = ["response_code"]),       // Filter by status
        Index(value = ["host", "path"]),        // Search by URL
        Index(value = ["is_favorite"])          // Filter favorites
    ]
)
data class TransactionEntity(...)
```

### Pagination

```kotlin
// BEFORE: Load all transactions
@Query("SELECT * FROM transactions ORDER BY timestamp DESC")
fun getAllTransactions(): Flow<List<TransactionEntity>>

// AFTER: Paged loading
@Query("SELECT * FROM transactions ORDER BY timestamp DESC")
fun getPagedTransactions(): PagingSource<Int, TransactionEntity>
```

## Compose Performance

### Stability

```kotlin
// Mark classes as stable for Compose
@Immutable
data class TransactionState(
    val items: ImmutableList<Transaction>,
    val isLoading: Boolean,
    val error: String?
)

// Or use @Stable for classes with stable public API
@Stable
class TransactionController {
    // ...
}
```

### Skip Recomposition

```kotlin
// Use remember to cache expensive operations
@Composable
fun TransactionDetail(transaction: Transaction) {
    val formattedBody = remember(transaction.body) {
        jsonFormatter.format(transaction.body)  // Expensive
    }

    Text(formattedBody)
}
```

### Layout Inspector

In Android Studio:
1. Run app
2. Tools > Layout Inspector
3. Check recomposition counts
4. Yellow/red highlights indicate frequent recomposition

## Benchmarking Checklist

Before claiming performance improvement:

- [ ] Measure baseline (before change)
- [ ] Measure multiple times (5+ iterations)
- [ ] Test on release build (not debug)
- [ ] Test on low-end device
- [ ] Test with realistic data volume
- [ ] Document methodology
- [ ] Compare percentiles (p50, p90, p99)

## Performance Report Template

```markdown
## Performance Analysis: {Component/Feature}

### Environment
- Device: Pixel 6, Android 14
- Build: Release with R8
- Data: 1000 transactions

### Baseline Metrics
| Metric | P50 | P90 | P99 |
|--------|-----|-----|-----|
| Startup | 85ms | 120ms | 150ms |
| List scroll FPS | 58 | 52 | 45 |
| Memory (idle) | 8MB | 9MB | 12MB |

### After Optimization
| Metric | P50 | P90 | P99 | Change |
|--------|-----|-----|-----|--------|
| Startup | 45ms | 65ms | 80ms | -47% |
| List scroll FPS | 60 | 59 | 55 | +10% |
| Memory (idle) | 5MB | 6MB | 7MB | -38% |

### Changes Made
1. Lazy database initialization
2. ImmutableList for transaction state
3. Added key parameter to LazyColumn items

### Methodology
- 10 cold starts, averaged
- Scroll test: 500 items, fast fling
- Memory: After GC, 30s idle
```

## Quick Wins Checklist

Common optimizations that usually help:

- [ ] Use `ImmutableList`/`ImmutableSet` in Compose state
- [ ] Add `key` parameter to `LazyColumn`/`LazyRow` items
- [ ] Use `remember`/`derivedStateOf` for computed values
- [ ] Lazy initialize heavy dependencies
- [ ] Use `Dispatchers.IO` for database operations
- [ ] Add database indices for common queries
- [ ] Implement pagination for large lists
- [ ] Avoid allocations in hot paths
- [ ] Use baseline profiles for startup
- [ ] Enable R8 full mode for release builds

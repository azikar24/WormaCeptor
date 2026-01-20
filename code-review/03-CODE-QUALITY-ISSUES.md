# Code Quality Issues

This document lists code quality issues including performance problems, security concerns, and architectural issues.

---

## Performance Issues

### 1. Inefficient Search Highlighting - O(n^2) Complexity

**Severity:** High - UI lag on large responses

**File:** `features/viewer/.../ui/TransactionDetailScreen.kt:1698-1727`

**Problem:** The search highlighting algorithm repeatedly scans the entire text. For large response bodies (up to 500KB), this causes UI lag.

**Bad Code:**
```kotlin
private fun enhancedHighlightMatches(...): AnnotatedString {
    return buildAnnotatedString {
        var start = 0
        while (start < text.length) {
            val index = text.indexOf(query, start, ignoreCase = true)  // O(n) each iteration
            // ... process match ...
            start = index + query.length
        }
    }
}
```

**Fix:**
```kotlin
private fun enhancedHighlightMatches(
    text: String,
    query: String,
    currentMatchGlobalPos: Int?
): AnnotatedString {
    if (query.isEmpty()) return AnnotatedString(text)

    return buildAnnotatedString {
        val pattern = Regex.escape(query).toRegex(RegexOption.IGNORE_CASE)
        val matches = pattern.findAll(text).toList()

        var lastIndex = 0
        matches.forEach { match ->
            append(text.substring(lastIndex, match.range.first))

            val isCurrent = match.range.first == currentMatchGlobalPos
            withStyle(
                style = SpanStyle(
                    background = if (isCurrent) Color.Cyan.copy(alpha = 0.6f)
                                 else Color.Yellow.copy(alpha = 0.4f),
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            ) {
                append(match.value)
            }
            lastIndex = match.range.last + 1
        }
        append(text.substring(lastIndex))
    }
}
```

---

### 2. Unnecessary Recomposition from Unused CompositionLocal

**Severity:** Medium - Performance waste

**Files:**
- `features/viewer/.../ui/components/TextWithStartEllipsis.kt:37`
- `features/viewer/.../ui/TransactionDetailScreen.kt:247,1152`

**Problem:** Accessing `LocalDensity.current` or `rememberCoroutineScope()` without using the result still subscribes to changes, causing unnecessary recomposition.

**Bad Code:**
```kotlin
val textMeasurer = rememberTextMeasurer()
LocalDensity.current  // Unused but causes subscription

LocalHapticFeedback.current  // Unused
rememberCoroutineScope()  // Unused
```

**Fix:** Remove all unused CompositionLocal accesses and remember calls.

---

### 3. Blocking Main Thread Risk

**Severity:** High - ANR risk

**Files:** Both `ServiceProviderImpl.kt` files

**Problem:** `runBlocking` calls from UI thread (ViewerActivity) can cause ANR.

**Evidence:**
```kotlin
// ViewerActivity.kt lines 120-121
val allTransactionsForExport = CoreHolder.queryEngine!!.getAllTransactionsForExport()
```

This calls through to `runBlocking` in ServiceProviderImpl.

**Fix:** Make all data operations suspend functions and call from coroutines.

---

## Dead Code

### 1. Unused Variables in MultipartBodyParser

**File:** `infra/parser/multipart/.../MultipartBodyParser.kt:155,197`

**Bad Code:**
```kotlin
private fun parseParts(content: String, boundary: String): List<MultipartPart> {
    val delimiter = "--$boundary"
    "--$boundary--"  // Line 155 - evaluated and discarded
    ...
}

private fun findHeaderBodySeparator(content: String): Int {
    var crlfIdx = content.indexOf("\r\n\r\n")  // var should be val
    if (crlfIdx != -1) return crlfIdx + 4

    var lfIdx = content.indexOf("\n\n")  // var should be val
    ...
}
```

**Fix:**
```kotlin
private fun parseParts(content: String, boundary: String): List<MultipartPart> {
    val delimiter = "--$boundary"
    val endDelimiter = "--$boundary--"  // Use or remove
    ...
}

private fun findHeaderBodySeparator(content: String): Int {
    val crlfIdx = content.indexOf("\r\n\r\n")  // Changed to val
    if (crlfIdx != -1) return crlfIdx + 4

    val lfIdx = content.indexOf("\n\n")  // Changed to val
    ...
}
```

---

### 2. Unused Composables in SwipeRefreshWrapper

**File:** `features/viewer/.../ui/components/gesture/SwipeRefreshWrapper.kt`

| Function | Line | Status |
|----------|------|--------|
| `CustomSwipeRefreshWrapper()` | 103 | Never called |
| `SimpleSwipeRefresh()` | 179 | Never called |
| `rememberCoroutineScope()` | 111 | Created but unused |

---

### 3. Unused Functions in GestureNavigationComponents

**File:** `features/viewer/.../ui/components/gesture/GestureNavigationComponents.kt`

| Function | Line | Status |
|----------|------|--------|
| `CompactPositionIndicator()` | 352 | Never called |
| `SwipeBackEdgeShadow()` | 1002 | Never called |
| `Float.roundTo()` | 1047 | Never called |

---

## Architectural Issues

### 1. Tight Coupling via CoreHolder Singleton

**Problem:** Multiple layers directly access `CoreHolder`, creating tight coupling:

```
ViewerActivity -> CoreHolder -> QueryEngine
HomeScreen -> CoreHolder -> QueryEngine
ViewerViewModel -> CoreHolder -> QueryEngine
```

**Impact:**
- Cannot easily swap implementations
- Unit testing requires mocking static object
- Initialization order dependencies

**Fix:** Use dependency injection to provide engines where needed.

---

### 2. Mixed Responsibilities in ServiceProviderImpl

**Problem:** `ServiceProviderImpl` handles:
- Initialization
- Transaction capture
- Transaction querying
- Notification display
- DTO conversion

**Impact:** Violates Single Responsibility Principle

**Fix:** Split into separate classes:
```kotlin
class TransactionCaptureService(private val captureEngine: CaptureEngine)
class TransactionQueryService(private val queryEngine: QueryEngine)
class TransactionNotificationService(private val notificationHelper: NotificationHelper)
class TransactionDtoMapper
```

---

### 3. Improper Layer Separation

**Problem:** `ServiceProviderImpl.getTransactionDetail()` contains URL parsing logic that should be in domain layer.

**Bad Code:**
```kotlin
override fun getTransactionDetail(id: String): TransactionDetailDto? = runBlocking {
    // ... URL parsing in API layer
    val host = try { java.net.URI(request.url).host ?: "" } catch (e: Exception) { "" }
    val path = try { java.net.URI(request.url).path ?: request.url } catch (e: Exception) { request.url }
    // ... DTO construction
}
```

**Fix:** URL parsing should be in domain entity or a dedicated utility class.

---

## Security Considerations

### 1. No Input Validation on Body Size

**File:** `api/client/.../WormaCeptorInterceptor.kt`

**Problem:** While there's a `maxContentLength` setting, it's a private mutable var with no validation.

**Current:**
```kotlin
private var maxContentLength = 250_000L
```

**Recommendation:**
```kotlin
companion object {
    const val MIN_CONTENT_LENGTH = 1_000L
    const val MAX_CONTENT_LENGTH = 10_000_000L
    const val DEFAULT_CONTENT_LENGTH = 250_000L
}

var maxContentLength: Long = DEFAULT_CONTENT_LENGTH
    set(value) {
        field = value.coerceIn(MIN_CONTENT_LENGTH, MAX_CONTENT_LENGTH)
    }
```

---

### 2. Exception Details Exposed in UI

**File:** Multiple export functions

**Problem:** Full exception messages and stack traces are included in exports, potentially exposing internal details.

**Recommendation:** Sanitize error messages before export in production builds.

---

## Summary

| Category | Count | Severity |
|----------|-------|----------|
| Performance Issues | 3 | High/Medium |
| Dead Code | 8+ functions | Low |
| Architectural Issues | 3 | Medium |
| Security Considerations | 2 | Low |

## Recommended Actions

1. **Performance:** Fix O(n^2) search highlighting algorithm
2. **Performance:** Remove unused CompositionLocal subscriptions
3. **Architecture:** Implement dependency injection
4. **Architecture:** Split ServiceProviderImpl responsibilities
5. **Cleanup:** Remove dead code (unused composables and functions)
6. **Security:** Add input validation on configurable limits

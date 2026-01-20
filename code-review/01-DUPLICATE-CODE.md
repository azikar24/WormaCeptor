# Duplicate Code Analysis

This document lists duplicate code patterns found in the WormaCeptor codebase.

---

## Critical Duplicates
### 2. ServiceProviderImpl - 95% Duplicate

**Impact:** ~110 lines of duplicate code

| Location | File |
|----------|------|
| 1 | `api/impl/imdb/src/main/java/com/azikar24/wormaceptor/api/internal/ServiceProviderImpl.kt:21-140` |
| 2 | `api/impl/persistence/src/main/java/com/azikar24/wormaceptor/api/internal/ServiceProviderImpl.kt:23-168` |

**Identical Methods:**
- `startTransaction()`
- `completeTransaction()`
- `cleanup()`
- `getLaunchIntent()`
- `getAllTransactions()`
- `getTransaction()`
- `getTransactionCount()`
- `clearTransactions()`
- `getTransactionDetail()` - 45+ lines, 98% identical

**Only Difference:** The `init()` method (InMemory vs Room implementations)

**Fix:** Create abstract base class:

```kotlin
// In api/client or api/common
abstract class BaseServiceProvider : ServiceProvider {
    protected var captureEngine: CaptureEngine? = null
    protected var queryEngine: QueryEngine? = null
    protected var notificationHelper: WormaCeptorNotificationHelper? = null

    abstract override fun init(context: Context, logCrashes: Boolean)

    // All other methods implemented here (shared logic)
    override fun startTransaction(...) = runBlocking {
        captureEngine?.startTransaction(...)
    }

    override fun getTransactionDetail(id: String): TransactionDetailDto? = runBlocking {
        // Shared URL parsing, DTO construction logic
    }
}
```

---

### 3. WormaCeptorDesignSystem - 100% Duplicate

**Impact:** 121 lines of identical code

| Location | File |
|----------|------|
| 1 | `app/src/main/java/com/azikar24/wormaceptorapp/wormaceptorui/theme/DesignSystem.kt:1-121` |
| 2 | `features/viewer/src/main/java/com/azikar24/wormaceptor/feature/viewer/ui/theme/DesignSystem.kt:1-121` |

**Fix:** Move to shared theme module or keep only in `features/viewer` and have `app` depend on it.

---

## Medium Priority Duplicates

### 4. formatBytes() - 3 Variations

| Location | File | Line |
|----------|------|------|
| 1 | `features/viewer/.../ui/TransactionDetailScreen.kt` | 861-866 |
| 2 | `features/viewer/.../ui/components/LoadingStates.kt` | 904-913 |
| 3 | Android Studio Plugin | similar |

**Code Variation 1:**
```kotlin
private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    val digitGroup = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroup.toDouble()), units[digitGroup])
}
```

**Code Variation 2:**
```kotlin
fun formatBytes(bytes: Long): String {
    if (bytes < 0) return "0 B"
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
```

**Fix:** Create unified utility:

```kotlin
// In features/viewer/.../util/FormatUtils.kt
object FormatUtils {
    fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "0 B"
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
```

---

### 5. copyToClipboard() - 4+ Occurrences

| Location | File | Line |
|----------|------|------|
| 1 | `features/viewer/.../ui/TransactionDetailScreen.kt` | 1757-1762 |
| 2 | `features/viewer/.../ui/CrashDetailScreen.kt` | 448-453 |
| 3 | `features/viewer/.../ViewerActivity.kt` | 225-230 |
| 4 | `features/viewer/.../ViewerActivity.kt` | 270-274 |

**Fix:**
```kotlin
// In features/viewer/.../util/ClipboardUtils.kt
object ClipboardUtils {
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
```

---

### 6. shareText() - 4+ Occurrences

| Location | File | Line |
|----------|------|------|
| 1 | `features/viewer/.../export/ExportManager.kt` | 57-69 |
| 2 | `features/viewer/.../export/CrashExport.kt` | 40-52 |
| 3 | `features/viewer/.../ViewerActivity.kt` | 232-244 |
| 4 | `features/viewer/.../ViewerActivity.kt` | 246-261 |

**Fix:**
```kotlin
// In features/viewer/.../util/ShareUtils.kt
object ShareUtils {
    fun shareText(context: Context, content: String, subject: String, chooserTitle: String = "Share") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            context.startActivity(Intent.createChooser(intent, chooserTitle))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
```

---

### 7. URL Parsing Pattern - 6+ Occurrences

| Location | File | Line |
|----------|------|------|
| 1 | `api/impl/imdb/.../ServiceProviderImpl.kt` | 102-103 |
| 2 | `api/impl/persistence/.../ServiceProviderImpl.kt` | 130-131 |
| 3 | `api/impl/imdb/.../WormaCeptorNotificationHelper.kt` | 80 |
| 4 | `api/impl/persistence/.../WormaCeptorNotificationHelper.kt` | 81 |
| 5 | `infra/persistence/sqlite/.../InMemoryTransactionRepository.kt` | 103-104 |
| 6 | `features/viewer/.../TransactionDetailScreen.kt` | 280-284 |

**Duplicate Pattern:**
```kotlin
val host = try { java.net.URI(request.url).host ?: "" } catch (e: Exception) { "" }
val path = try { java.net.URI(request.url).path ?: request.url } catch (e: Exception) { request.url }
```

**Fix:**
```kotlin
// In domain/entities or shared util
object UrlParser {
    fun extractHost(url: String): String =
        try { java.net.URI(url).host ?: "" } catch (e: Exception) { "" }

    fun extractPath(url: String): String =
        try { java.net.URI(url).path ?: url } catch (e: Exception) { url }
}
```

---

### 8. Parser Empty Body Handling - 7+ Occurrences

Found across ALL parsers in `infra/parser/*/`:

| File | Pattern |
|------|---------|
| `FormBodyParser.kt` | Lines 41-47 |
| `ProtobufBodyParser.kt` | Lines 51-57 |
| `PdfParser.kt` | Lines 54-61 |
| `HtmlBodyParser.kt` | Similar |
| `JsonBodyParser.kt` | Similar |
| `XmlBodyParser.kt` | Similar |
| `MultipartBodyParser.kt` | Similar |

**Duplicate Pattern:**
```kotlin
override fun parse(body: ByteArray): ParsedBody {
    if (body.isEmpty()) {
        return ParsedBody(
            formatted = "",
            contentType = ContentType.FORM_DATA, // varies
            isValid = true
        )
    }
    // ... parsing logic
}
```

**Fix:** Create abstract base class:
```kotlin
abstract class BaseBodyParser : BodyParser {
    abstract val supportedContentType: ContentType

    override fun parse(body: ByteArray): ParsedBody {
        if (body.isEmpty()) return emptyParsedBody()
        return parseNonEmpty(body)
    }

    protected fun emptyParsedBody() = ParsedBody(
        formatted = "",
        contentType = supportedContentType,
        isValid = true
    )

    protected abstract fun parseNonEmpty(body: ByteArray): ParsedBody
}
```

---

### 9. Status Color Logic - 3+ Occurrences

| Location | File | Line |
|----------|------|------|
| 1 | `features/viewer/.../PagedTransactionListScreen.kt` | 207-218 |
| 2 | Android Studio plugin `TransactionListCellRenderer.kt` | 100-110 |
| 3 | Android Studio plugin `TransactionDetailPanel.kt` | 274-284 |

**Fix:** Add extension function:
```kotlin
fun TransactionSummary.getStatusColor(): Color = when (status) {
    TransactionStatus.COMPLETED -> when {
        code == null -> WormaCeptorColors.StatusAmber
        code in 200..299 -> WormaCeptorColors.StatusGreen
        code in 300..399 -> WormaCeptorColors.StatusBlue
        code in 400..499 -> WormaCeptorColors.StatusAmber
        code in 500..599 -> WormaCeptorColors.StatusRed
        else -> WormaCeptorColors.StatusGrey
    }
    TransactionStatus.FAILED -> WormaCeptorColors.StatusRed
    TransactionStatus.ACTIVE -> WormaCeptorColors.StatusGrey
}
```

---

## Summary

| Priority | Pattern | Lines Saved |
|----------|---------|-------------|
| High | WormaCeptorDesignSystem | 121 |
| High | BaseServiceProvider | 110 |
| High | WormaCeptorNotificationHelper | 82 |
| Medium | BaseBodyParser | 50+ |
| Medium | ClipboardUtils + ShareUtils | 30+ |
| Medium | FormatUtils | 20+ |
| Medium | UrlParser | 15+ |
| Low | Status color extension | 20+ |

**Total Estimated Duplicate Code:** 400-500 lines

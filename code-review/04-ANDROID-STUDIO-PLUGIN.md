# Android Studio Plugin Issues

This document lists code quality issues found in the Android Studio plugin module.

**Location:** `plugins/android-studio/src/main/kotlin/`

---

## Critical Issues

### 1. Memory Leak - StateListener Not Removed

**File:** `WormaCeptorToolWindowPanel.kt:280-294`

**Problem:** Anonymous `StateListener` is added but never removed. The `dispose()` method only stops the timer but doesn't remove this listener.

**Bad Code:**
```kotlin
service.addStateListener(object : WormaCeptorService.StateListener {
    override fun onDeviceChanged(serial: String?) {
        refreshTransactions()
    }
    // ... other callbacks
})
```

**Fix:**
```kotlin
private val stateListener = object : WormaCeptorService.StateListener {
    override fun onDeviceChanged(serial: String?) {
        refreshTransactions()
    }
    // ... other methods
}

private fun setupListeners() {
    service.addStateListener(stateListener)
}

fun dispose() {
    refreshTimer.stop()
    service.removeStateListener(stateListener)  // ADD THIS
}
```

---

### 2. EDT Blocking - Synchronous ADB Calls in update()

**Files:**
- `ClearTransactionsAction.kt:46-50`
- `OpenViewerAction.kt:22-26`

**Problem:** The `update()` method runs on EDT. Calling `isDeviceConnected()` executes ADB commands synchronously, freezing the UI.

**Bad Code:**
```kotlin
override fun update(e: AnActionEvent) {
    val project = e.project
    e.presentation.isEnabled = project != null &&
            project.getService(WormaCeptorService::class.java).isDeviceConnected()
}
```

**Fix:** Cache device connection status and update asynchronously:
```kotlin
override fun update(e: AnActionEvent) {
    val project = e.project
    val service = project?.getService(WormaCeptorService::class.java)
    e.presentation.isEnabled = service?.cachedDeviceConnected ?: false
}
```

---

### 3. Resource Leak - Process Timeout Ineffective

**File:** `WormaCeptorServiceImpl.kt:333-355`

**Problem:** Code reads entire process output BEFORE checking timeout. If ADB hangs, the read loop blocks forever.

**Bad Code:**
```kotlin
private fun executeAdbCommand(vararg args: String): String {
    val process = ProcessBuilder(command).start()

    // This blocks forever if process hangs!
    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
        reader.lineSequence().forEach { line ->
            output.appendLine(line)
        }
    }

    // Timeout check happens AFTER blocking read
    val completed = process.waitFor(10, TimeUnit.SECONDS)
    if (!completed) {
        process.destroyForcibly()
    }
    return output.toString()
}
```

**Fix:**
```kotlin
private fun executeAdbCommand(vararg args: String): String {
    val process = ProcessBuilder(command).start()

    try {
        // Check timeout FIRST
        val completed = process.waitFor(10, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            throw RuntimeException("ADB command timed out")
        }

        // Then read output (process already completed)
        return process.inputStream.bufferedReader().readText()
    } catch (e: Exception) {
        process.destroyForcibly()
        throw e
    }
}
```

---

### 4. Missing Dispose Hook for Timer

**File:** `WormaCeptorToolWindowPanel.kt:62-84`

**Problem:** The `dispose()` method is defined but never called. IntelliJ doesn't automatically call `dispose()` on `SimpleToolWindowPanel`. Timer runs forever.

**Fix:** Implement `Disposable` and register properly:
```kotlin
class WormaCeptorToolWindowPanel(private val project: Project) :
    SimpleToolWindowPanel(true, true), Disposable {

    override fun dispose() {
        refreshTimer.stop()
        service.removeStateListener(stateListener)
    }
}

// In WormaCeptorToolWindowFactory.kt
override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val panel = WormaCeptorToolWindowPanel(project)
    val content = ContentFactory.getInstance().createContent(panel, "Transactions", false)
    content.setDisposer(panel)  // Register disposer
    toolWindow.contentManager.addContent(content)
}
```

---

### 5. Shared Mutable State Without Synchronization

**File:** `WormaCeptorServiceImpl.kt:28-36`

**Problem:** Mutable fields accessed from multiple threads (EDT and background) without synchronization.

**Bad Code:**
```kotlin
private var selectedDeviceSerial: String? = null
private var cachedTransactions = listOf<TransactionSummary>()
private var captureActive = false
private var transactionCount = 0
private var targetPackage: String? = null
```

**Fix:** Use `@Volatile` for thread-safe visibility:
```kotlin
@Volatile private var selectedDeviceSerial: String? = null
@Volatile private var cachedTransactions = listOf<TransactionSummary>()
@Volatile private var captureActive = false
@Volatile private var transactionCount = 0
@Volatile private var targetPackage: String? = null
```

---

### 6. Hardcoded Strings - Missing Message Bundle

**Files:** All action files

**Problem:** User-facing strings are hardcoded instead of using message bundles.

**Bad Code:**
```kotlin
class ClearTransactionsAction : AnAction(
    "Clear Transactions",
    "Clear all captured transactions",
    AllIcons.Actions.GC
)
```

**Fix:** Create message bundle:
```kotlin
// WormaCeptorBundle.kt
object WormaCeptorBundle {
    private const val BUNDLE = "messages.WormaCeptorBundle"

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
        return AbstractBundle.message(ResourceBundle.getBundle(BUNDLE), key, *params)
    }
}

// messages/WormaCeptorBundle.properties
action.clear.transactions.text=Clear Transactions
action.clear.transactions.description=Clear all captured transactions

// Usage
class ClearTransactionsAction : AnAction(
    WormaCeptorBundle.message("action.clear.transactions.text"),
    WormaCeptorBundle.message("action.clear.transactions.description"),
    AllIcons.Actions.GC
)
```

---

### 7. Listener Notifications Not on EDT

**File:** `WormaCeptorServiceImpl.kt:454-464`

**Problem:** Notifications called from background threads, but listener callbacks might do UI work.

**Bad Code:**
```kotlin
private fun notifyDeviceChanged(serial: String?) {
    listeners.forEach { it.onDeviceChanged(serial) }
}
```

**Fix:**
```kotlin
private fun notifyDeviceChanged(serial: String?) {
    ApplicationManager.getApplication().invokeLater {
        listeners.forEach { it.onDeviceChanged(serial) }
    }
}
```

---

## Summary

| Issue | Severity | Impact |
|-------|----------|--------|
| Memory Leak - StateListener | Critical | Plugin memory grows over time |
| EDT Blocking | Critical | UI freezes during ADB calls |
| Process Timeout Ineffective | Critical | Plugin hangs on ADB issues |
| Missing Dispose Hook | Critical | Timer runs after window closed |
| Thread Safety | Important | Race conditions, stale UI |
| Missing i18n | Important | No internationalization |
| EDT Notifications | Important | Potential UI exceptions |

**Total Issues:** 7

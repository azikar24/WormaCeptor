# WormaCeptor V2 - Setup Guide

Comprehensive integration reference for WormaCeptor V2.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Initialization](#initialization)
- [OkHttp Interceptor](#okhttp-interceptor)
- [Ktor Client Plugin](#ktor-client-plugin)
- [WebSocket Monitoring](#websocket-monitoring)
- [WebView Monitoring](#webview-monitoring)
- [Launching the UI](#launching-the-ui)
- [Performance Overlay](#performance-overlay)
- [Feature Toggles](#feature-toggles)
- [Data Redaction](#data-redaction)
- [Extension Providers](#extension-providers)
- [Deep Links](#deep-links)
- [No-Op Behavior](#no-op-behavior)
- [Permissions](#permissions)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)

---

## Prerequisites

| Requirement | Minimum |
|-------------|---------|
| Android SDK | API 23 (Android 6.0) |
| Kotlin | 2.0+ |
| OkHttp | 4.x (for OkHttp interceptor) |
| Ktor | 3.x (for Ktor plugin) |
| Gradle | 8.x |

---

## Installation

Add JitPack to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add dependencies to your module's `build.gradle.kts`:

```kotlin
dependencies {
    // Required: API client (lightweight, safe for all build types)
    implementation("com.github.azikar24.WormaCeptor:api-client:2.2.0")

    // Debug: Choose one storage backend
    debugImplementation("com.github.azikar24.WormaCeptor:api-impl-persistence:2.2.0")
}
```

### Storage Backend Comparison

| Module | Storage | Survives App Restart | Size Impact | Best For |
|--------|---------|---------------------|-------------|----------|
| `api-impl-persistence` | Room (SQLite) | Yes | ~2 MB | Most apps: persistent inspection across sessions |
| `api-impl-imdb` | In-memory | No | ~0.5 MB | CI/testing: lightweight, no disk I/O |
| `api-impl-no-op` | None | N/A | ~5 KB | Explicit no-op (rarely needed) |

The `api-client` module automatically falls back to a no-op implementation when no backend is found, so `api-impl-no-op` is rarely needed.

---

## Initialization

Call `WormaCeptorApi.init()` as early as possible in your `Application.onCreate()`:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        WormaCeptorApi.init(
            context = this,
            logCrashes = true,
            features = Feature.ALL,
            leakNotifications = true,
        )
    }
}
```

### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `context` | `Context` | required | Application context |
| `logCrashes` | `Boolean` | `true` | Capture uncaught exceptions and display them in the Crashes tab |
| `features` | `Set<Feature>` | `Feature.DEFAULT` | Which features to enable (see [Feature Toggles](#feature-toggles)) |
| `leakNotifications` | `Boolean` | `true` | Show system notifications when memory leaks are detected |

### Selective Feature Initialization

```kotlin
// Enable only performance monitoring and network inspection
WormaCeptorApi.init(
    context = this,
    features = setOf(
        Feature.MEMORY_MONITOR,
        Feature.FPS_MONITOR,
        Feature.CPU_MONITOR,
        Feature.WEBSOCKET_MONITOR,
    ),
)
```

---

## OkHttp Interceptor

Add `WormaCeptorInterceptor` to your OkHttp client to capture HTTP/HTTPS traffic:

```kotlin
val interceptor = WormaCeptorInterceptor()
    .showNotification(true)
    .maxContentLength(500_000L)
    .retainDataFor(WormaCeptorInterceptor.Period.ONE_WEEK)
    .redactHeader("Authorization")
    .redactHeader("Cookie")
    .redactJsonValue("password")
    .redactXmlValue("apiKey")

val client = OkHttpClient.Builder()
    .addInterceptor(interceptor)
    .build()
```

### Builder Methods

All methods return `WormaCeptorInterceptor` for chaining.

| Method | Description |
|--------|-------------|
| `showNotification(show: Boolean)` | Enable/disable system notification on capture (default: `true`) |
| `maxContentLength(length: Long)` | Max body size to capture in bytes (default: 250,000) |
| `retainDataFor(period: Period)` | Auto-delete transactions older than the specified period |
| `redactHeader(name: String)` | Redact a header value (case-insensitive) |
| `redactBody(pattern: String)` | Redact body content matching a regex pattern |
| `redactJsonValue(key: String)` | Redact JSON values by key name |
| `redactXmlValue(tag: String)` | Redact XML element content by tag name |

### Period Enum

| Value | Duration |
|-------|----------|
| `ONE_HOUR` | 1 hour |
| `ONE_DAY` | 24 hours |
| `ONE_WEEK` | 7 days |
| `ONE_MONTH` | 30 days |
| `FOREVER` | No cleanup |

---

## Ktor Client Plugin

WormaCeptor provides a native Ktor client plugin for capturing HTTP traffic without OkHttp:

```kotlin
val client = HttpClient(CIO) {
    install(WormaCeptorKtorPlugin) {
        maxContentLength = 500_000L
        retainDataFor = WormaCeptorKtorConfig.RetentionPeriod.ONE_WEEK
        redactHeader("Authorization")
        redactJsonValue("password")
        redactXmlValue("apiKey")
    }
}
```

### Configuration Properties

| Property/Method | Type | Default | Description |
|-----------------|------|---------|-------------|
| `maxContentLength` | `Long` | 250,000 | Max body size to capture in bytes |
| `retainDataFor` | `RetentionPeriod?` | `null` | Data retention period (cleanup on plugin install) |
| `redactHeader(name)` | Method | - | Delegates to global `WormaCeptorApi.redactionConfig` |
| `redactBody(pattern)` | Method | - | Delegates to global `WormaCeptorApi.redactionConfig` |
| `redactJsonValue(key)` | Method | - | Delegates to global `WormaCeptorApi.redactionConfig` |
| `redactXmlValue(tag)` | Method | - | Delegates to global `WormaCeptorApi.redactionConfig` |

### RetentionPeriod Enum

`WormaCeptorKtorConfig.RetentionPeriod` has the same values as the OkHttp `Period` enum: `ONE_HOUR`, `ONE_DAY`, `ONE_WEEK`, `ONE_MONTH`, `FOREVER`.

### Dependency Note

The Ktor plugin classes live in the `api-client` module. Use `compileOnly` for the Ktor dependency so it doesn't add Ktor to apps that only use OkHttp:

```kotlin
compileOnly("io.ktor:ktor-client-core:<ktor-version>")
```

### Rate Limiting

Both the OkHttp interceptor and Ktor plugin automatically integrate with the Rate Limiter tool. When rate limiting is enabled in the WormaCeptor UI, artificial latency is applied to network calls.

---

## WebSocket Monitoring

OkHttp interceptors don't capture WebSocket traffic. Use `WormaCeptorWebSocket` to monitor WebSocket connections:

```kotlin
val listener = object : WebSocketListener() {
    override fun onMessage(webSocket: WebSocket, text: String) {
        // Handle received message
    }
}

// Wrap your listener for monitoring
val monitor = WormaCeptorWebSocket.wrap(listener, "wss://example.com/ws")

// Use the wrapped listener with OkHttp
val request = Request.Builder().url("wss://example.com/ws").build()
val webSocket = client.newWebSocket(request, monitor.listener)

// Record sent messages (OkHttp doesn't notify listeners of outgoing messages)
webSocket.send(message)
monitor.recordSentMessage(message)
```

### Factory Methods

| Method | Description |
|--------|-------------|
| `wrap(delegate: WebSocketListener, url: String)` | Wrap an existing listener for monitoring |
| `wrap(url: String)` | Create a monitoring-only listener (no delegate) |

### Instance Methods

| Method | Description |
|--------|-------------|
| `recordSentMessage(text: String)` | Record an outgoing text message |
| `recordSentMessage(bytes: ByteString)` | Record an outgoing binary message |
| `recordPing(payload: ByteString)` | Record a ping frame |
| `recordPong(payload: ByteString)` | Record a pong frame |
| `getConnectionId()` | Get the unique connection ID (`-1` if unavailable) |

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `listener` | `WebSocketListener` | The wrapped listener to pass to OkHttp |

---

## WebView Monitoring

Monitor network requests made by `WebView` instances:

```kotlin
val webView: WebView = findViewById(R.id.webview)

webView.webViewClient = WormaCeptorWebView.createMonitoringClient(
    webViewId = "my_webview",
    delegate = myWebViewClient,  // Optional: your existing WebViewClient
)

webView.loadUrl("https://example.com")
```

### Factory Methods

| Method | Description |
|--------|-------------|
| `createMonitoringClient(webViewId: String, delegate: WebViewClient?)` | Create a monitoring WebViewClient |

### Release Build Behavior

When the WormaCeptor implementation is not available (e.g., release builds):
- If a `delegate` is provided, it is returned as-is
- If no `delegate` is provided, a no-op `WebViewClient` is returned

This means you can safely call `createMonitoringClient()` in all build types.

---

## Launching the UI

### Via Intent

Launch WormaCeptor manually from a button, menu item, or any trigger:

```kotlin
startActivity(WormaCeptorApi.getLaunchIntent(context))
```

### Via Shake Gesture

Register a lifecycle-aware shake detector that opens WormaCeptor when the user shakes the device:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ...
        WormaCeptorApi.startActivityOnShake(this)
    }
}
```

The shake detector automatically stops when the activity is destroyed.

### Via Floating Button

Show a draggable floating button overlay that opens WormaCeptor when tapped:

```kotlin
// Check permission first
if (WormaCeptorApi.canShowFloatingButton(context)) {
    WormaCeptorApi.showFloatingButton(context)
} else {
    // Request overlay permission
    val intent = WormaCeptorApi.getOverlayPermissionIntent(context)
    if (intent != null) {
        startActivity(intent)
    }
}

// Hide when no longer needed
WormaCeptorApi.hideFloatingButton(context)
```

| Method | Return | Description |
|--------|--------|-------------|
| `canShowFloatingButton(context)` | `Boolean` | Check if overlay permission is granted (always `true` on API < 23) |
| `showFloatingButton(context)` | `Boolean` | Show overlay. Returns `false` if permission not granted |
| `hideFloatingButton(context)` | `Unit` | Hide the overlay |
| `getOverlayPermissionIntent(context)` | `Intent?` | Intent to open system overlay settings. `null` on API < 23 |

---

## Performance Overlay

Show a draggable overlay displaying real-time FPS, Memory, and CPU metrics:

```kotlin
// Show the overlay (requires SYSTEM_ALERT_WINDOW permission)
val shown = WormaCeptorApi.showPerformanceOverlay(activity)

// Check visibility
val isVisible = WormaCeptorApi.isPerformanceOverlayVisible()

// Hide the overlay
WormaCeptorApi.hidePerformanceOverlay()
```

| Method | Return | Description |
|--------|--------|-------------|
| `showPerformanceOverlay(activity)` | `Boolean` | Show overlay. Returns `false` if permission not granted |
| `hidePerformanceOverlay()` | `Unit` | Hide the overlay |
| `isPerformanceOverlayVisible()` | `Boolean` | Check if the overlay is currently visible |

Use `canShowFloatingButton(context)` to check the `SYSTEM_ALERT_WINDOW` permission before showing.

---

## Feature Toggles

The `Feature` enum controls which tools are available in WormaCeptor. Pass a set of features to `init()` to selectively enable them.

### All Feature Values

#### Inspection Features

| Feature | Description |
|---------|-------------|
| `SHARED_PREFERENCES` | Browse and edit SharedPreferences files |
| `DATABASE_BROWSER` | Browse SQLite databases and execute queries |
| `FILE_BROWSER` | Browse application files and directories |
| `LOADED_LIBRARIES` | View loaded native libraries (.so files) |
| `DEPENDENCIES_INSPECTOR` | Inspect Gradle dependencies and versions |
| `SECURE_STORAGE` | Browse encrypted SharedPreferences (EncryptedSharedPreferences) |
| `WEBVIEW_MONITOR` | Monitor WebView loading and JavaScript execution |

#### Performance Features

| Feature | Description |
|---------|-------------|
| `MEMORY_MONITOR` | Memory usage monitoring with real-time charts |
| `FPS_MONITOR` | Frame rate monitoring and jank detection |
| `CPU_MONITOR` | CPU usage monitoring per core |
| `LEAK_DETECTION` | Memory leak detection using LeakCanary integration |
| `THREAD_VIOLATIONS` | StrictMode violation detection for disk/network on main thread |

#### Network Features

| Feature | Description |
|---------|-------------|
| `WEBSOCKET_MONITOR` | WebSocket connection and message monitoring |
| `RATE_LIMITER` | Network rate limiting with presets (2G, 3G, 4G, WiFi) |

#### Simulation Features

| Feature | Description |
|---------|-------------|
| `LOCATION_SIMULATOR` | Mock GPS location for testing |
| `PUSH_SIMULATOR` | Send simulated push notifications |
| `PUSH_TOKEN_MANAGER` | View and copy FCM/push notification tokens |
| `CRYPTO_TOOL` | Cryptographic tools for hashing, encoding, encryption |

#### Core Features

| Feature | Description |
|---------|-------------|
| `CONSOLE_LOGS` | Application log viewer (Logcat) |
| `DEVICE_INFO` | Device and application information |

### Predefined Feature Sets

| Set | Contents |
|-----|----------|
| `Feature.ALL` | All 20 features |
| `Feature.DEFAULT` | Same as `ALL` |
| `Feature.CORE` | `CONSOLE_LOGS` + `DEVICE_INFO` |

### Runtime Queries

```kotlin
// Check if a specific feature is enabled
val leakEnabled: Boolean = WormaCeptorApi.isFeatureEnabled(Feature.LEAK_DETECTION)

// Get all enabled features
val enabled: Set<Feature> = WormaCeptorApi.getEnabledFeatures()
```

---

## Data Redaction

WormaCeptor provides two levels of redaction configuration.

### Global Redaction

Configure via `WormaCeptorApi.redactionConfig`. Applies to all captured traffic (OkHttp, Ktor, and future HTTP clients):

```kotlin
WormaCeptorApi.redactionConfig
    .redactHeader("Authorization")
    .redactHeader("Cookie")
    .redactHeader("X-Api-Key")
    .redactJsonValue("password")
    .redactJsonValue("token")
    .redactXmlValue("apiKey")
    .redactBody("api_key=\\w+")       // Regex pattern
    .replacement("[REDACTED]")         // Custom replacement text
```

| Method | Description |
|--------|-------------|
| `redactHeader(name: String)` | Redact header values by name (case-insensitive) |
| `redactBody(pattern: String)` | Redact body content matching a regex |
| `redactJsonValue(key: String)` | Redact JSON values by key |
| `redactXmlValue(tag: String)` | Redact XML element content by tag |
| `replacement(text: String)` | Set custom replacement text (default: `"********"`) |

### Per-Interceptor Redaction (OkHttp)

The OkHttp interceptor's `redactHeader()`, `redactBody()`, `redactJsonValue()`, and `redactXmlValue()` methods delegate to the global `WormaCeptorApi.redactionConfig`. This means:

- Redaction rules set on the interceptor also affect Ktor traffic
- For OkHttp-only redaction, configure the global config after Ktor plugin installation

### Per-Plugin Redaction (Ktor)

The Ktor plugin config's redaction methods also delegate to `WormaCeptorApi.redactionConfig`, so they behave identically.

### How Redaction Works

- **Headers**: The header value is replaced entirely with the replacement text
- **JSON values**: `"password":"secret"` becomes `"password":"********"`
- **XML values**: `<apiKey>secret</apiKey>` becomes `<apiKey>********</apiKey>`
- **Body patterns**: The entire regex match is replaced with the replacement text

---

## Extension Providers

Register custom metadata extractors that enrich network transactions with app-specific data:

```kotlin
WormaCeptorApi.registerExtensionProvider(
    object : ExtensionProvider {
        override val name = "AuthExtension"
        override fun extractExtensions(context: ExtensionContext): Map<String, String> {
            return mapOf(
                "user_id" to getCurrentUserId(),
                "session_type" to getSessionType(),
            )
        }
    },
)
```

The extracted key-value pairs are stored with each transaction and displayed in the WormaCeptor UI.

### Methods

| Method | Return | Description |
|--------|--------|-------------|
| `registerExtensionProvider(provider: ExtensionProvider)` | `Unit` | Register a custom provider |
| `unregisterExtensionProvider(name: String)` | `Boolean` | Unregister by name. Returns `true` if removed |
| `getRegisteredExtensionProviders()` | `List<String>` | List registered provider names |

### ExtensionProvider Interface

```kotlin
interface ExtensionProvider {
    val name: String
    fun extractExtensions(context: ExtensionContext): Map<String, String>
}
```

`ExtensionContext` provides access to the request URL, method, headers, and response data for each transaction.

---

## Deep Links

All deep links use the `wormaceptor://` scheme.

### Tab Links

| Deep Link | Destination |
|-----------|-------------|
| `wormaceptor://transactions` | Transactions tab |
| `wormaceptor://crashes` | Crashes tab |
| `wormaceptor://tools` | Tools tab |

### Tool Links

| Deep Link | Aliases | Destination |
|-----------|---------|-------------|
| `wormaceptor://tools/memory` | - | Memory Monitor |
| `wormaceptor://tools/fps` | - | FPS Monitor |
| `wormaceptor://tools/cpu` | - | CPU Monitor |
| `wormaceptor://tools/preferences` | `sharedpreferences` | SharedPreferences Inspector |
| `wormaceptor://tools/database` | - | SQLite Browser |
| `wormaceptor://tools/filebrowser` | `files` | File Browser |
| `wormaceptor://tools/websocket` | - | WebSocket Monitor |
| `wormaceptor://tools/touchviz` | `touch` | Touch Visualizer |
| `wormaceptor://tools/viewborders` | `borders` | View Borders |
| `wormaceptor://tools/location` | - | Location Simulator |
| `wormaceptor://tools/pushsimulator` | `push` | Push Notification Simulator |
| `wormaceptor://tools/leakdetection` | `leaks` | Leak Detection |
| `wormaceptor://tools/threadviolation` | `threads` | Thread Violation Detection |
| `wormaceptor://tools/webviewmonitor` | `webview` | WebView Monitor |
| `wormaceptor://tools/crypto` | - | Crypto Tool |
| `wormaceptor://tools/gridoverlay` | `grid` | Grid Overlay |
| `wormaceptor://tools/measurement` | `measure` | Measurement Tool |
| `wormaceptor://tools/securestorage` | `secure` | Secure Storage Inspector |
| `wormaceptor://tools/ratelimit` | `rate` | Rate Limiter |
| `wormaceptor://tools/pushtoken` | `token` | Push Token Manager |
| `wormaceptor://tools/loadedlibraries` | `libraries` | Loaded Libraries |
| `wormaceptor://tools/dependencies` | `deps` | Dependencies Inspector |
| `wormaceptor://tools/logs` | `console` | Console Logs |
| `wormaceptor://tools/deviceinfo` | `device` | Device Info |

### Programmatic Deep Links

```kotlin
// Open a specific tool
val intent = DeepLinkHandler.createIntent(context, "wormaceptor://tools/memory")
startActivity(intent)

// Or use helper methods
val uri = DeepLinkHandler.createToolDeepLink("memory")      // "wormaceptor://tools/memory"
val tabUri = DeepLinkHandler.createToolsTabDeepLink()        // "wormaceptor://tools"
val txUri = DeepLinkHandler.createTransactionsTabDeepLink()  // "wormaceptor://transactions"
```

Unrecognized tool paths fall back to the Tools tab.

---

## No-Op Behavior

WormaCeptor is designed to have zero impact on release builds.

### How It Works

1. The `api-client` module contains only lightweight API facades and a `NoOpProvider`
2. At `init()` time, it uses reflection to look for `ServiceProviderImpl` in the classpath
3. If found (debug build with `api-impl-persistence` or `api-impl-imdb`), it delegates to the real implementation
4. If not found (release build), all API calls silently do nothing

### What Happens in Release Builds

| API Call | Release Behavior |
|----------|-----------------|
| `WormaCeptorApi.init()` | Initializes `NoOpProvider`, returns immediately |
| `WormaCeptorInterceptor.intercept()` | Passes request through unchanged |
| `WormaCeptorKtorPlugin` | Passes request through unchanged |
| `WormaCeptorWebView.createMonitoringClient()` | Returns the delegate (or a plain `WebViewClient`) |
| `WormaCeptorWebSocket.wrap()` | Returns a pass-through wrapper |
| `getLaunchIntent()` | Returns an empty `Intent` |
| `startActivityOnShake()` | No-op (ShakeDetector not in classpath) |
| `showFloatingButton()` | Returns `false` |
| `showPerformanceOverlay()` | Returns `false` |

---

## Permissions

| Permission | Required By | Notes |
|------------|------------|-------|
| `SYSTEM_ALERT_WINDOW` | Performance Overlay, Floating Button | Request via `getOverlayPermissionIntent()` |
| `READ_EXTERNAL_STORAGE` | File Browser | Only needed on API < 30 |
| `QUERY_ALL_PACKAGES` | Loaded Libraries | Required to list all installed packages |
| `ACCESS_FINE_LOCATION` | Location Simulator | Required for mock location provider |
| `POST_NOTIFICATIONS` | Push Simulator | Required on API 33+ |
| `INTERNET` | Network inspection | Usually already present in apps using HTTP |

---

## Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| No network logs | Missing implementation module | Add `debugImplementation` for `api-impl-persistence` or `api-impl-imdb` |
| Release build crash | Implementation in `implementation` | Change to `debugImplementation` |
| Shake gesture not working | No implementation module | Shake requires `api-impl-persistence` or `api-impl-imdb` |
| Overlay not appearing | Missing permission | Grant `SYSTEM_ALERT_WINDOW` via system settings |
| Ktor requests missing | Init order | Call `WormaCeptorApi.init()` before creating the `HttpClient` |
| WebView traffic not captured | Wrong WebViewClient | Use `WormaCeptorWebView.createMonitoringClient()` |
| WebSocket messages missing outgoing | Manual recording required | Call `monitor.recordSentMessage()` after each `webSocket.send()` |
| Features disabled | Init config | Check the `features` parameter in `init()` |
| Redaction not working for Ktor | Shared config | Ktor redaction methods delegate to `WormaCeptorApi.redactionConfig` |
| Large responses truncated | Content length limit | Increase `maxContentLength` (default: 250KB) |

---

## Best Practices

1. **Initialize early** - Call `WormaCeptorApi.init()` at the start of `Application.onCreate()` before creating HTTP clients
2. **Always use `debugImplementation`** - Never use `implementation` for persistence/imdb modules
3. **Redact sensitive data** - Configure `redactHeader()` and `redactJsonValue()` for auth tokens, passwords, and PII before any network calls
4. **Set content limits** - Keep `maxContentLength` reasonable (250KB-500KB) to avoid excessive memory/storage use
5. **Use selective features** - Disable features you don't need to reduce initialization overhead
6. **Record WebSocket sends** - OkHttp doesn't notify listeners of outgoing messages; always call `recordSentMessage()`
7. **Handle overlay permissions** - Check `canShowFloatingButton()` before showing overlays and guide users to settings if needed
8. **Use deep links for testing** - Deep links let you navigate directly to any tool programmatically
9. **Register extension providers early** - Register before network traffic starts to capture metadata from the first request
10. **Leverage no-op safety** - All WormaCeptor API calls are safe in release builds; no conditional wrapping needed

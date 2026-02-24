# WormaCeptor API Client - Public API Reference

This document describes the stable public API surface of the `api-client` module.
Classes and methods documented here are considered stable and follow semantic versioning.

## Overview

The api-client module provides the public interface for integrating WormaCeptor into Android applications. It is designed as a lightweight facade that discovers and delegates to implementation modules at runtime.

## Core Classes

### WormaCeptorApi

**Package:** `com.azikar24.wormaceptor.api`

Main entry point for WormaCeptor. This singleton object handles initialization and provides access to all features.

#### Initialization

```kotlin
WormaCeptorApi.init(
    context: Context,
    logCrashes: Boolean = true,
    features: Set<Feature> = Feature.DEFAULT,
    leakNotifications: Boolean = true
)
```

Initializes WormaCeptor with the specified configuration. Call once during application startup.

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| context | Context | required | Application context |
| logCrashes | Boolean | true | Enable uncaught exception logging |
| features | Set\<Feature\> | Feature.DEFAULT | Features to enable |
| leakNotifications | Boolean | true | Show notifications for memory leaks |

#### Feature Control

| Method | Return Type | Description |
|--------|-------------|-------------|
| `isFeatureEnabled(feature: Feature)` | Boolean | Check if a specific feature is enabled |
| `getEnabledFeatures()` | Set\<Feature\> | Get all enabled features |

#### UI Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getLaunchIntent(context: Context)` | Intent | Get intent to launch WormaCeptor UI |
| `startActivityOnShake(activity: ComponentActivity)` | Unit | Enable shake gesture to open WormaCeptor |
| `canShowFloatingButton(context: Context)` | Boolean | Check if overlay permission is granted |
| `showFloatingButton(context: Context)` | Boolean | Show floating button overlay |
| `hideFloatingButton(context: Context)` | Unit | Hide floating button overlay |
| `getOverlayPermissionIntent(context: Context)` | Intent? | Get intent to request overlay permission |

#### Performance Overlay

| Method | Return Type | Description |
|--------|-------------|-------------|
| `showPerformanceOverlay(activity: ComponentActivity)` | Boolean | Show real-time performance metrics overlay |
| `hidePerformanceOverlay()` | Unit | Hide performance overlay |
| `isPerformanceOverlayVisible()` | Boolean | Check if overlay is visible |

#### Extension System

| Method | Return Type | Description |
|--------|-------------|-------------|
| `registerExtensionProvider(provider: ExtensionProvider)` | Unit | Register custom metadata extractor |
| `unregisterExtensionProvider(name: String)` | Boolean | Unregister provider by name |
| `getRegisteredExtensionProviders()` | List\<String\> | List registered provider names |

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| `redactionConfig` | RedactionConfig | Configuration for data redaction |

---

### WormaCeptorInterceptor

**Package:** `com.azikar24.wormaceptor.api`

OkHttp interceptor for capturing HTTP/HTTPS network traffic.

#### Usage

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(WormaCeptorInterceptor())
    .build()
```

#### Builder Methods

All methods return `WormaCeptorInterceptor` for method chaining.

| Method | Description |
|--------|-------------|
| `showNotification(show: Boolean)` | Enable/disable capture notifications |
| `maxContentLength(length: Long)` | Set max body size to capture (default: 250KB) |
| `retainDataFor(period: Period)` | Set data retention period and clean old data |
| `redactHeader(name: String)` | Add header to redact |
| `redactBody(pattern: String)` | Add regex pattern to redact in bodies |
| `redactJsonValue(key: String)` | Redact JSON values by key |
| `redactXmlValue(tag: String)` | Redact XML values by tag |

#### Period Enum

Data retention periods for `retainDataFor()`:

| Value | Description |
|-------|-------------|
| `ONE_HOUR` | Retain for 1 hour |
| `ONE_DAY` | Retain for 24 hours |
| `ONE_WEEK` | Retain for 7 days |
| `ONE_MONTH` | Retain for 30 days |
| `FOREVER` | Retain indefinitely |

---

### WormaCeptorWebSocket

**Package:** `com.azikar24.wormaceptor.api`

Wrapper for monitoring WebSocket connections.

#### Usage

```kotlin
val monitor = WormaCeptorWebSocket.wrap(myListener, "wss://example.com/ws")
val webSocket = client.newWebSocket(request, monitor.listener)

// Record outgoing messages manually
webSocket.send(message)
monitor.recordSentMessage(message)
```

#### Factory Methods

| Method | Description |
|--------|-------------|
| `wrap(delegate: WebSocketListener, url: String)` | Wrap existing listener |
| `wrap(url: String)` | Create monitoring-only listener |

#### Instance Methods

| Method | Description |
|--------|-------------|
| `recordSentMessage(text: String)` | Record sent text message |
| `recordSentMessage(bytes: ByteString)` | Record sent binary message |
| `recordPing(payload: ByteString)` | Record ping frame |
| `recordPong(payload: ByteString)` | Record pong frame |
| `getConnectionId()` | Get unique connection ID (-1 if unavailable) |

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| `listener` | WebSocketListener | Wrapped listener for OkHttp |

---

### WormaCeptorWebView

**Package:** `com.azikar24.wormaceptor.api`

Wrapper for monitoring WebView network requests.

#### Usage

```kotlin
val webView: WebView = ...
webView.webViewClient = WormaCeptorWebView.createMonitoringClient(
    webViewId = "my_webview",
    delegate = myWebViewClient,
)
```

#### Factory Methods

| Method | Description |
|--------|-------------|
| `createMonitoringClient(webViewId: String, delegate: WebViewClient?)` | Create a monitoring WebViewClient |

When the WormaCeptor implementation is not available (e.g. release builds), the delegate is returned as-is (or a no-op `WebViewClient` if no delegate is provided).

---

### RedactionConfig

**Package:** `com.azikar24.wormaceptor.api`

Configuration for redacting sensitive data from captured traffic.

#### Usage

```kotlin
WormaCeptorApi.redactionConfig
    .redactHeader("Authorization")
    .redactHeader("Cookie")
    .redactJsonValue("password")
    .redactXmlValue("apiKey")
    .replacement("[REDACTED]")
```

#### Methods

All methods return `RedactionConfig` for method chaining.

| Method | Description |
|--------|-------------|
| `redactHeader(name: String)` | Redact header by name (case-insensitive) |
| `redactBody(pattern: String)` | Redact matching regex patterns |
| `redactJsonValue(key: String)` | Redact JSON values by key |
| `redactXmlValue(tag: String)` | Redact XML element content by tag |
| `replacement(text: String)` | Set replacement text (default: "********") |

---

### Feature

**Package:** `com.azikar24.wormaceptor.api`

Enum of all available WormaCeptor features for selective enabling.

#### Inspection Features

| Value | Description |
|-------|-------------|
| `SHARED_PREFERENCES` | Browse and edit SharedPreferences |
| `DATABASE_BROWSER` | Browse SQLite databases |
| `FILE_BROWSER` | Browse app files |
| `LOADED_LIBRARIES` | View loaded native libraries |
| `DEPENDENCIES_INSPECTOR` | Inspect Gradle dependencies |
| `SECURE_STORAGE` | Browse encrypted preferences |
| `WEBVIEW_MONITOR` | Monitor WebView activity |

#### Performance Features

| Value | Description |
|-------|-------------|
| `MEMORY_MONITOR` | Memory usage monitoring |
| `FPS_MONITOR` | Frame rate monitoring |
| `CPU_MONITOR` | CPU usage monitoring |
| `LEAK_DETECTION` | Memory leak detection |
| `THREAD_VIOLATIONS` | StrictMode violation detection |

#### Network Features

| Value | Description |
|-------|-------------|
| `WEBSOCKET_MONITOR` | WebSocket monitoring |
| `RATE_LIMITER` | Network rate limiting |

#### Simulation Features

| Value | Description |
|-------|-------------|
| `LOCATION_SIMULATOR` | Mock GPS location |
| `PUSH_SIMULATOR` | Simulate push notifications |
| `PUSH_TOKEN_MANAGER` | View push tokens |
| `CRYPTO_TOOL` | Cryptographic utilities |

#### Core Features

| Value | Description |
|-------|-------------|
| `CONSOLE_LOGS` | Application logs |
| `DEVICE_INFO` | Device information |

#### Companion Object Properties

| Property | Type | Description |
|----------|------|-------------|
| `ALL` | Set\<Feature\> | All available features |
| `DEFAULT` | Set\<Feature\> | Default enabled features (all) |
| `CORE` | Set\<Feature\> | Core features (logs, device info) |

---

## Type Aliases

### ExtensionContext

**Package:** `com.azikar24.wormaceptor.api`

```kotlin
typealias ExtensionContext = com.azikar24.wormaceptor.domain.contracts.ExtensionContext
```

Context provided to extension providers for extracting custom metadata from transactions.

### ExtensionProvider

**Package:** `com.azikar24.wormaceptor.api`

```kotlin
typealias ExtensionProvider = com.azikar24.wormaceptor.domain.contracts.ExtensionProvider
```

Interface for custom extension providers that extract metadata from network transactions.

---

## Data Classes

### TransactionDetailDto

**Package:** `com.azikar24.wormaceptor.api`

DTO for transferring complete transaction details to IDE plugins via content provider.

| Property | Type | Description |
|----------|------|-------------|
| `id` | String | Unique transaction ID (UUID) |
| `method` | String | HTTP method |
| `url` | String | Complete request URL |
| `host` | String | URL host |
| `path` | String | URL path |
| `code` | Int? | Response status code |
| `duration` | Long? | Request duration (ms) |
| `status` | String | Transaction status |
| `timestamp` | Long | Request timestamp |
| `requestHeaders` | Map\<String, List\<String\>\> | Request headers |
| `requestBody` | String? | Request body content |
| `requestSize` | Long | Request body size |
| `responseHeaders` | Map\<String, List\<String\>\> | Response headers |
| `responseBody` | String? | Response body content |
| `responseSize` | Long | Response body size |
| `responseMessage` | String? | HTTP status message |
| `protocol` | String? | HTTP protocol version |
| `tlsVersion` | String? | TLS version (HTTPS only) |
| `error` | String? | Error message (if failed) |
| `contentType` | String? | Response Content-Type |
| `extensions` | Map\<String, String\> | Custom extension data |

---

## Implementation-Only Classes

The following classes are public for technical reasons but are not part of the stable API.
They should only be used by WormaCeptor implementation modules, not by host applications:

| Class | Purpose |
|-------|---------|
| `ServiceProvider` | Interface implemented by storage backends (persistence, in-memory) |
| `WormaCeptorContentProvider` | ContentProvider for IDE plugin communication |

**Warning:** These classes may change without notice between minor versions.

---

## Version History

| Version | Changes |
|---------|---------|
| 2.2.0 | Ktor HTTP client support, MVI pattern adoption (BaseViewModel), shared WormaCeptorTheme, decoupled syntax highlighting |
| 2.0.0 | Initial modular architecture |

# WormaCeptor V2

### The Clean-Architecture, Production-Safe Network Inspector and Debugging Toolkit for Android

**WormaCeptor V2** is a complete architectural rewrite of the classic network interceptor, now expanded into a comprehensive debugging toolkit. Designed for modularity, safety, and zero-impact production builds, it decouples inspection logic from interception points.

---

## Quick Start

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.github.azikar24.WormaCeptor:api-client:2.1.0")
    debugImplementation("com.github.azikar24.WormaCeptor:api-impl-persistence:2.1.0")
}
```

```kotlin
// Application.kt
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WormaCeptorApi.init(this)
    }
}

// OkHttp setup
val client = OkHttpClient.Builder()
    .addInterceptor(WormaCeptorInterceptor())
    .build()
```

For detailed integration, see [Quick Reference](docs/reference/00-quick-reference.md).

---

## Why WormaCeptor V2?

| Problem | Solution |
|---------|----------|
| Traditional inspectors leak into release builds | Physical dependency separation via `debugImplementation` |
| Monolithic architecture couples UI + Logic | Modular design with 50+ independent modules |
| Debug tools can crash production apps | Reflection-based discovery with graceful No-Op fallback |
| Limited to network inspection only | Comprehensive debugging toolkit with 30+ features |

---

## Feature Overview

### Network Inspection
- HTTP/HTTPS traffic capture with headers, body, timing, TLS info
- Request/response body parsing (JSON, XML, HTML, multipart, images, PDFs)
- Syntax highlighting and tree views
- Search, filter, and favorites
- cURL and JSON export

### Performance Monitoring
- **FPS Monitor**: Real-time frame rate tracking with history
- **Memory Monitor**: Heap usage with threshold alerts
- **CPU Monitor**: Per-core and overall usage tracking
- **Performance Overlay**: Draggable floating badge showing live metrics with mini sparklines

### System Inspection
- **SQLite Browser**: Browse tables and execute custom queries
- **SharedPreferences Inspector**: View and edit preferences
- **Secure Storage Inspector**: View encrypted preferences
- **File Browser**: Navigate app file system
- **Device Info**: Comprehensive device details
- **Cookies Manager**: View HTTP cookies
- **Loaded Libraries**: List native .so files

### Advanced Debugging
- **Leak Detection**: Automatic memory leak detection for Activities/Fragments
- **Thread Violation Detection**: ANR warnings and main thread violations
- **Crash Reporting**: Integrated exception capture with stack traces

### Network Simulation
- **Rate Limiter**: Throttle network with presets (2G, 3G, 4G, WiFi) or custom speeds
- **WebSocket Monitor**: Real-time WebSocket frame inspection
- **WebView Monitor**: Track WebView activity and JS bridges

### Testing Tools
- **Push Notification Simulator**: Send test notifications
- **Location Simulator**: Mock GPS location
- **Crypto Tool**: Encrypt/decrypt with AES, RSA

---

## Architecture

```
[ Your App ]
     |
     v
[ OkHttp Client ]
     |
     +--> [ WormaCeptor Interceptor (API Client) ]
                  |
                  v
          ( ServiceProvider Contract )
                  |
        +---------+---------+
        | (Dynamic Binding) |
        v                   v
 [ No-Op Impl ]      [ Persistence / IMDB ]
    (Release)             (Debug)
                             |
                   +---------+---------+
                   |         |         |
               [ Core ]  [ Infra ]  [ UI ]
                  |          |         |
               Engine   SQLite/Mem  Features
```

### Module Structure

| Layer | Modules | Purpose |
|-------|---------|---------|
| **API** | client, common, impl-persistence, impl-imdb, impl-no-op | Public interface and implementations |
| **Core** | engine | Business logic, monitoring engines, capture/query |
| **Domain** | entities, contracts | Framework-agnostic data models and interfaces |
| **Features** | 31 modules | UI screens for each debugging feature |
| **Infra** | persistence, networking, parsers, syntax | Concrete implementations |
| **Platform** | android | Android-specific utilities (notifications, shake) |

See [Repository Structure](docs/architecture/REPO_STRUCTURE.md) for full details.

---

## Installation

### JitPack

Add JitPack repository:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add dependencies:

```kotlin
dependencies {
    // Required: Lightweight API client
    implementation("com.github.azikar24.WormaCeptor:api-client:2.1.0")

    // Debug: Choose one implementation
    debugImplementation("com.github.azikar24.WormaCeptor:api-impl-persistence:2.1.0")
    // OR for in-memory (clears on app kill):
    // debugImplementation("com.github.azikar24.WormaCeptor:api-impl-imdb:2.1.0")

    // Optional: Explicit no-op for release (usually not needed)
    // releaseImplementation("com.github.azikar24.WormaCeptor:api-impl-no-op:2.1.0")
}
```

---

## Configuration

### Interceptor Options

```kotlin
WormaCeptorInterceptor()
    .showNotification(true)           // System notification for new transactions
    .maxContentLength(500_000L)       // Max body capture size in bytes
    .retainDataFor(Period.ONE_WEEK)   // Auto-cleanup policy
    .redactHeader("Authorization")    // Mask sensitive headers
    .redactHeader("Cookie")
    .redactBody("password\":\".*?\"") // Mask JSON fields via regex
```

### WebSocket Monitoring

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
val webSocket = client.newWebSocket(request, monitor.listener)

// Record sent messages (OkHttp doesn't notify listeners of outgoing messages)
webSocket.send(message)
monitor.recordSentMessage(message)
```

**Available methods**:
- `recordSentMessage(text: String)` - Record outgoing text messages
- `recordSentMessage(bytes: ByteString)` - Record outgoing binary messages
- `recordPing(payload: ByteString)` - Record ping frames
- `recordPong(payload: ByteString)` - Record pong frames

### Launch UI Manually

```kotlin
// If shake-to-open is disabled
startActivity(WormaCeptorApi.getLaunchIntent(context))
```

### Feature Toggles

Enable/disable features programmatically or via Settings > Feature Toggles:
- Network Tab
- Crashes Tab
- SharedPreferences tool
- Console Logs
- Device Info
- SQLite Browser
- File Browser

---

## Performance Overlay

The draggable overlay shows real-time FPS, Memory, and CPU metrics. Enable via Tools > Performance Overlay.

**Features**:
- Collapsed: Compact badge with three metrics
- Expanded: Mini sparkline charts for each metric
- Tap metrics to deep-link to detail screens
- Long-press to drag anywhere on screen

**Deep Links**:
- `wormaceptor://tools/fps` - FPS detail
- `wormaceptor://tools/memory` - Memory detail
- `wormaceptor://tools/cpu` - CPU detail
- `wormaceptor://tools` - Tools tab

---

## Permissions

Some features require additional permissions:

| Permission | Features |
|------------|----------|
| `SYSTEM_ALERT_WINDOW` | Performance Overlay |
| `READ_EXTERNAL_STORAGE` | File Browser |
| `QUERY_ALL_PACKAGES` | Loaded Libraries |

---

## Best Practices

1. **Initialize Early**: Call `WormaCeptorApi.init()` first in `Application.onCreate()`
2. **Redact Sensitive Data**: Use `redactHeader()` and `redactBody()` for tokens, passwords, PII
3. **Limit Content Size**: Default is 250KB - avoid increasing for large binary downloads
4. **Debug Only**: Always use `debugImplementation` for persistence modules
5. **Performance Overlay**: Disable in performance-critical scenarios (adds ~1% overhead)

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| No logs appearing | Verify `debugImplementation` for `api-impl-persistence` |
| Release build crashes | Ensure persistence module is NOT in `implementation` or `releaseImplementation` |
| Shake not working | Requires `api-impl-persistence` module (disabled in No-Op) |
| Overlay not showing | Grant `SYSTEM_ALERT_WINDOW` permission |
| Feature toggles not persisting | Check DataStore initialization |

---

## Technology Stack

| Component | Version |
|-----------|---------|
| Kotlin | 2.0.21 |
| Min SDK | 23 |
| Target SDK | 36 |
| Jetpack Compose | BOM 2024.10.01 |
| Room | 2.6.1 |
| OkHttp | 4.12.0 |
| Koin | 4.0.0 |
| Coroutines | 1.8.1 |

---

## Documentation

| Document | Description |
|----------|-------------|
| [Quick Reference](docs/reference/00-quick-reference.md) | 5-minute integration guide |
| [Technical Docs](docs/reference/01-technical-documentation.md) | Comprehensive architecture |
| [Feature Inventory](docs/reference/02-feature-inventory.md) | Complete feature list |
| [Repository Structure](docs/architecture/REPO_STRUCTURE.md) | Module layout |
| [Design System](docs/architecture/DESIGN_SYSTEM.md) | UI guidelines |
| [Product Boundaries](docs/architecture/PRODUCT_BOUNDARIES.md) | Scope definition |

---

## Comparison

| Feature | Traditional Tools | WormaCeptor V2 |
|---------|-------------------|----------------|
| Architecture | Monolithic | Modular (50+ modules) |
| Release Safety | ProGuard rules | Physical dependency separation |
| Crash Reporting | Separate library | Integrated and correlated |
| Performance Monitoring | Not included | FPS, Memory, CPU with overlay |
| System Inspection | Limited | SQLite, SharedPrefs, Files, Cookies |

---

## Roadmap

### Implemented
- Network interception with full HTTP inspection
- Performance monitoring (FPS, Memory, CPU)
- Performance overlay with deep linking
- System inspection (SQLite, SharedPrefs, Files)
- Feature toggle system
- Leak detection and thread violation detection
- Rate limiting and network simulation

### Planned
- GraphQL query/mutation parsing
- gRPC/Protobuf native support
- Custom formatter plugins
- CI/CD headless mode for UI tests
- Remote inspection via ADB

---

## Contributing

We welcome PRs. Please respect Clean Architecture boundaries:

| Type | Location |
|------|----------|
| Business logic | `core/engine` |
| Data models | `domain/entities` |
| Interfaces | `domain/contracts` |
| UI screens | `features/*` |
| Storage/Network | `infra/*` |

If your feature adds a heavy library, create a new module.

### Development

```bash
# Build
./gradlew build

# Run demo app
./gradlew :app:installDebug

# Format code
./gradlew spotlessApply

# Static analysis
./gradlew detekt

# Architecture tests
./gradlew :test:architecture:test
```

---

## License

MIT License - Free for personal and commercial use.

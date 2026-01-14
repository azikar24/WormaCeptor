# WormaCeptor V2 🪱

### The Clean-Architecture, Production-Safe Network Inspector for Android.

**WormaCeptor V2** is a complete architectural rewrite of the classic network interceptor, designed for modularity, safety, and zero-impact production builds. It decouples the *inspection logic* from the *interception point*, ensuring your debug tools never crash your production app.

---

## Executive Summary

**What it solves:**  
traditional network inspectors are often monolithic, heavy, and tightly coupled to their UI. They can accidentally leak into release builds, increase APK size, or cause runtime crashes if not carefully managed.

**Why V2 exists:**  
WormaCeptor V2 was born from the need to have a powerful debug tool that adheres to **Clean Architecture** principles. It separates the "API" (what you call) from the "Implementation" (how it stores/shows data).

**Use it when:**
- You need deep insight into HTTP traffic (Headers, TLS, Sizes, Timing).
- You want a debug tool that *guarantees* no-op in release builds without complex ProGuard rules.
- You care about modularity and don't want to coupled your app to a specific database or UI library.
- You need "Shake-to-Report" functionality for QA teams.

**Do NOT use it when:**
- You are looking for a simple `Log.d` printer (use `HttpLoggingInterceptor`).
- You need a tool to *modify* or *mock* responses explicitly (WormaCeptor is a passive *observer*).

---

## Key Principles & Design Philosophy

1.  **Strict Separation of Concerns**: The `api-client` module knows *nothing* about the database, the UI, or the notification system. It only knows `Contracts`.
2.  **Reflection-Based Discovery**: You simply add the `implementation` module (e.g., `api-impl-persistence`) to your debug configuration. The core API discovers it at runtime. If it's missing (release builds), it gracefully falls back to a No-Op implementation.
3.  **Crash Safety**: Interceptors are wrapped in safety blocks. A bug in your logging tool should **never** crash your user's purchase flow.
4.  **Non-Invasive**: The interceptor does not modify the request/response stream unless redaction is explicitly requested.
5.  **Debug vs. Release**:
    - **Debug**: Full inspection (Persistence or In-Memory), notifications, UI.
    - **Release**: Zero logic, zero DB, zero UI dependencies.

---

## High-Level Architecture

```text
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
               Engine   SQLite/Mem  Viewer
```

---

## Core Features

*   **Network Interception**: Captures method, headers, body, latency, protocol, and TLS version.
*   **Safety First**: Handles `IOException` and memory pressure gracefully without disrupting the app flow.
*   **Redaction Systems**: Configurable regex-based masking for sensitive headers (Auth tokens) and body content (PII).
*   **Crash Recording**: Automatic capture of uncaught exceptions and HTTP failures.
*   **Smart Retention**: Auto-cleanup policies (`ONE_HOUR`, `ONE_DAY`, `FOREVER`) to prevent database bloat.
*   **Environment Awareness**:
    *   `debugImplementation` = Full Features.
    *   `releaseImplementation` = auto-no-op (or simply omit the impl dependency).
*   **Shake-to-Open**: Integrated shake detection to launch the dashboard.
*   **Export**: Share transactions and crash logs via JSON/Text to Slack/Jira.
*   **Search & Filter**: Powerful on-device search by URL, method, status code, or time range.

---

## Installation

Add the core API and the implementation modules to your `build.gradle.kts`.

```kotlin
dependencies {
    // 1. Always include the API client (lightweight, safe)
    implementation("com.azikar24.wormaceptor:api-client:2.0.0")

    // 2. Choose your implementation for debug/QA builds:
    
    // Option A: SQLite Persistence (Keeps logs across app restarts)
    debugImplementation("com.azikar24.wormaceptor:api-impl-persistence:2.0.0")
    
    // OR Option B: In-Memory DB (Faster, clears on app kill)
    // debugImplementation("com.azikar24.wormaceptor:api-impl-imdb:2.0.0")

    // 3. (Optional) Explicit No-Op for release, though usually not needed if you use debugImplementation above
    // releaseImplementation("com.azikar24.wormaceptor:api-impl-no-op:2.0.0")
}
```

---

## Basic Usage

### 1. Initialize

In your `Application.onCreate()`:

```kotlin
import com.azikar24.wormaceptor.api.WormaCeptorApi

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Auto-detects if a real provider is available.
        // If 'api-impl-persistence' is missing, this does nothing safely.
        WormaCeptorApi.init(this)
    }
}
```

### 2. Add Validator

In your OkHttp setup (e.g., Dagger/Hilt module):

```kotlin
import com.azikar24.wormaceptor.api.WormaCeptorInterceptor

val client = OkHttpClient.Builder()
    .addInterceptor(WormaCeptorInterceptor()) // Add this!
    .build()
```

---

## Advanced Usage

### Custom Configuration

You can chain configuration methods on the interceptor:

```kotlin
WormaCeptorInterceptor()
    .showNotification(true)           // Toggle system notifications
    .maxContentLength(500_000L)       // Cap body capture size (bytes)
    .retainDataFor(Period.ONE_WEEK)   // Auto-delete old data
    .redactHeader("Authorization")    // Mask sensitive headers
    .redactBody("password\":\".*?\"") // Mask JSON fields via Regex
```

### Launching the UI Manually

If you don't like "Shake-to-Open", you can trigger it programmatically:

```kotlin
// Launch the dashboard
startActivity(WormaCeptorApi.getLaunchIntent(context))
```

---

## Best Practices

*   **Initialize Early**: Call `init()` as the first thing in `Application.onCreate` to ensure crash reporting catches early startup issues.
*   **Do NOT Log Everything**: Use the `redactHeader` and `redactBody` generously. Logging user passwords or credit card info to a local DB is a security risk, even in debug builds.
*   **Performance**: The default `maxContentLength` is 250KB. Avoid increasing this for large binary downloads (images/videos) as it consumes memory and DB space.
*   **Production Safety**: strictly use `debugImplementation` for the `api-impl-persistence` artifact. This guarantees that your release APK contains **zero** footprint of the database, UI, and logic code.

---

## Common Pitfalls

*   **"I see no logs!"**: Double-check you added `debugImplementation("...:api-impl-persistence")`. Without it, the API defaults to No-Op.
*   **"My release build crashes"**: Ensure you did NOT include the persistence module in `implementation` or `releaseImplementation`.
*   **"Shake doesn't work"**: Shake detection requires the `api-impl-persistence` module. It is disabled in No-Op.

---

## Benefits Over Traditional Interceptors

| Feature             | Traditional Tools (Chucker/Chuck) | WormaCeptor V2                             |
| :------------------ | :-------------------------------- | :----------------------------------------- |
| **Architecture**    | Monolithic (UI + Logic)           | Modular (API decoupled from Impl)          |
| **Release Safety**  | ProGuard rules / No-Op Stubs      | Physical dependency separation             |
| **Crash Reporting** | Usually separate library          | Integrated & correlated with network calls |
| **Design**          | Basic List                        | Modern, Search-First, Filter-Rich UI       |

---

## Roadmap / Extensibility

WormaCeptor V2 is designed to be **pluggable**.
*   **Core**: The engine handles data ingestion.
*   **Plugins**: New "Providers" can be written.
    *   *Planned*: **GraphQL Support** - Specialized viewer for GraphQL query/mutation parsing.
    *   *Planned*: **gRPC Support** - Native Protobuf decoding and viewer.
    *   *Planned*: **Custom Formatters** - Plug in your own JSON/XML pretty printers.
    *   *Planned*: **CI/CD Integration** - Headless mode to dump network logs to a file during UI tests.

To extend, simply implement the `ServiceProvider` interface and register it via the internal discovery mechanism.

---

## License & Contribution

**License**: MIT. Free for personal and commercial use.

**Contribution**: We welcome PRs! Please stick to the Clean Architecture boundaries.
- Logic goes in `core:engine`
- Entity definitions go in `domain:entities`
- UI goes in `features:viewer`

Strive for **modularity**. If your feature adds a heavy library, it belongs in a new module.

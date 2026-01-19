# WormaCeptor V2 - Quick Reference Guide

## Documentation Index

This documentation suite provides comprehensive information about WormaCeptor V2, a production-grade Android network inspection library.

### Available Documents

| Document | Description | Audience |
|----------|-------------|----------|
| **[01-technical-documentation.md](01-technical-documentation.md)** | Complete architecture overview, module structure, data flows, and extension points | Engineers, Architects |
| **[02-feature-inventory.md](02-feature-inventory.md)** | Comprehensive feature list organized by domain with implementation status | Product Managers, Engineers |
| **[03-feature-expansion-roadmap.md](03-feature-expansion-roadmap.md)** | Future feature ideas categorized by timeline (short/mid/long-term) | Product Managers, Stakeholders |
| **[04-ui-ux-enhancement-plan.md](04-ui-ux-enhancement-plan.md)** | UI/UX improvements, accessibility enhancements, and design system recommendations | Designers, Engineers |
| **[05-technical-debt-improvements.md](05-technical-debt-improvements.md)** | Prioritized technical debt items with solutions and impact analysis | Engineers, Tech Leads |

## What is WormaCeptor V2?

WormaCeptor V2 is an Android library that captures and visualizes HTTP network traffic and application crashes for debugging purposes. It provides:

- **Network Interception**: Captures all HTTP requests/responses via OkHttp interceptor
- **Crash Logging**: Automatically logs uncaught exceptions with full stack traces
- **Rich UI**: Jetpack Compose interface for viewing and searching captured data
- **Developer-Friendly**: Shake-to-open, notifications, export to JSON/cURL
- **Production-Safe**: Zero overhead in release builds through reflection-based discovery

## 5-Minute Integration Guide

### Step 1: Add Dependencies

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("path/to/wormaceptor") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation(project(":api:client"))
    debugImplementation(project(":api:impl:persistence"))
}
```

### Step 2: Initialize in Application Class

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        WormaCeptorApi.init(this, logCrashes = true)
    }
}
```

### Step 3: Add Interceptor to OkHttp

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
```

### Step 4: Open Viewer

Shake your device or call:
```kotlin
WormaCeptorApi.openViewer(context)
```

## Common Use Cases

### Debugging Network Issues
1. Enable shake-to-open gesture
2. Trigger the problematic request
3. Shake device to open viewer
4. Search for the transaction by URL
5. Inspect request/response details

### Exporting Transactions
1. Open transaction detail screen
2. Tap overflow menu (3 dots)
3. Select "Share as JSON" or "Copy as cURL"
4. Share via email, Slack, etc.

### Redacting Sensitive Data
```kotlin
WormaCeptorInterceptor()
    .redactHeader("Authorization", "X-Api-Key", "Cookie")
    .redactBody("\"(password|token|ssn)\":\\s*\"[^\"]*\"")
```

### Viewing Crashes
1. Open viewer
2. Navigate to "Crashes" tab
3. Tap crash to see full stacktrace
4. Use copy/share to send to team

## Troubleshooting Quick Fixes

### No transactions appearing
- Ensure `debugImplementation` is used (not `implementation`)
- Check `WormaCeptorApi.init()` is called in Application.onCreate
- Verify interceptor is added to OkHttpClient
- Confirm requests are using the configured OkHttpClient

### Viewer not opening
- Check `WormaCeptorApi.isInitialized()` returns true
- Verify reflection discovery succeeded (check logcat)
- Ensure Activity context is used (not Application context)

### App crashes on startup
- Ensure reflection is not obfuscated (ProGuard/R8 rules needed)
- Check for conflicting Room versions
- Verify min SDK is 23+

### Missing request/response bodies
- Check `maxContentLength` is not too restrictive
- Verify Content-Type is supported (JSON, text)
- Confirm bodies aren't consumed before interceptor runs

## API at a Glance

### WormaCeptorApi (Entry Point)

```kotlin
// Initialization
WormaCeptorApi.init(context: Context, logCrashes: Boolean = true)

// Check status
WormaCeptorApi.isInitialized(): Boolean

// Open viewer UI
WormaCeptorApi.openViewer(context: Context)

// Configuration
WormaCeptorApi.redactionConfig: RedactionConfig
```

### WormaCeptorInterceptor (OkHttp)

```kotlin
WormaCeptorInterceptor()
    .showNotification(true)               // Show ongoing notification
    .maxContentLength(250_000L)           // Max body size to capture (bytes)
    .retainDataFor(Period.ONE_WEEK)       // Auto-cleanup after period
    .redactHeader(vararg names: String)   // Redact headers by name
    .redactBody(vararg patterns: String)  // Redact body via regex
```

### RedactionConfig (Global)

```kotlin
WormaCeptorApi.redactionConfig
    .redactHeader("Authorization")
    .redactBody("\"password\":\\s*\"[^\"]*\"")
    .replacement("********")  // Default redaction text
```

## Key File Locations

### API Layer
- `api/client/src/.../WormaCeptorApi.kt` - Public API entry point
- `api/client/src/.../WormaCeptorInterceptor.kt` - OkHttp interceptor
- `api/client/src/.../RedactionConfig.kt` - Redaction configuration

### Core Business Logic
- `core/engine/src/.../CaptureEngine.kt` - Transaction capture orchestration
- `core/engine/src/.../QueryEngine.kt` - Data retrieval and search
- `core/engine/src/.../CrashReporter.kt` - Crash handling

### Domain Models
- `domain/entities/src/.../NetworkTransaction.kt` - Core data model
- `domain/contracts/src/.../TransactionRepository.kt` - Repository interface

### Infrastructure
- `infra/persistence/sqlite/src/.../WormaCeptorDatabase.kt` - Room database
- `infra/persistence/sqlite/src/.../FileSystemBlobStorage.kt` - Body storage

### UI (Viewer)
- `features/viewer/src/.../ViewerActivity.kt` - Main UI entry point
- `features/viewer/src/.../HomeScreen.kt` - Transaction/crash list
- `features/viewer/src/.../TransactionDetailScreen.kt` - Detail view
- `features/viewer/src/.../ViewerViewModel.kt` - UI state management

### Platform Utilities
- `platform/android/src/.../ShakeDetector.kt` - Gesture detection

## Architecture Quick Summary

```
+-------------------------------------------------------------+
|  Host App (MainActivity)                                    |
|  - Initializes WormaCeptorApi                               |
|  - Adds WormaCeptorInterceptor to OkHttp                    |
+---------------------------+---------------------------------+
                            |
+---------------------------v---------------------------------+
|  API Layer (:api:client)                                    |
|  - WormaCeptorApi (init, openViewer)                        |
|  - WormaCeptorInterceptor (captures HTTP traffic)           |
|  - ServiceProvider interface                                |
+---------------------------+---------------------------------+
                            | (Reflection Discovery)
+---------------------------v---------------------------------+
|  Implementation (:api:impl:persistence)                     |
|  - ServiceProviderImpl (coordinates components)             |
|  - NotificationHelper (ongoing notifications)               |
+---------------------------+---------------------------------+
                            |
+---------------------------v---------------------------------+
|  Core Layer (:core:engine)                                  |
|  - CaptureEngine (business logic for capture)               |
|  - QueryEngine (business logic for queries)                 |
|  - CrashReporter (exception handler)                        |
+---------+----------------------+----------------------------+
          |                      |
+---------v----------+  +--------v------------------------+
|  Domain Contracts  |  |  Infrastructure                 |
|  - Repository IF   |  |  - RoomTransactionRepository    |
|  - BlobStorage IF  |  |  - RoomCrashRepository          |
+--------------------+  |  - FileSystemBlobStorage        |
                        |  - Room Database (SQLite)       |
                        +----------+----------------------+
                                   |
                        +----------v----------------------+
                        |  Features (:features:viewer)    |
                        |  - ViewerActivity (UI)          |
                        |  - ViewerViewModel (state)      |
                        |  - Compose screens              |
                        +---------------------------------+
```

## Technology Stack Summary

| Category | Technology | Version |
|----------|------------|---------|
| Language | Kotlin | 2.0.21 |
| UI Framework | Jetpack Compose | BOM 2024.10.01 |
| Design System | Material 3 | Latest |
| Database | Room | 2.6.1 |
| Networking | OkHttp | 4.12.0 |
| Async | Coroutines + Flow | 1.8.1 |
| Architecture | Clean Architecture | - |
| Min SDK | Android 23 (Marshmallow) | - |
| Target SDK | Android 34 | - |

## Next Steps

- **New to WormaCeptor?** Start with [Technical Documentation](01-technical-documentation.md)
- **Want to understand features?** See [Feature Inventory](02-feature-inventory.md)
- **Planning enhancements?** Check [Feature Expansion Roadmap](03-feature-expansion-roadmap.md)
- **Improving UI/UX?** Read [UI/UX Enhancement Plan](04-ui-ux-enhancement-plan.md)
- **Addressing technical debt?** Review [Technical Debt & Improvements](05-technical-debt-improvements.md)

## Support

For issues, feature requests, or contributions, see the main README.md in the project root.

# 🐛 WormaCeptor

WormaCeptor is a powerful, production-grade network and crash interceptor for Android, designed to give engineers real-time visibility into their application's telemetry.

Built with **Jetpack Compose**, **Kotlin Coroutines**, and a modular **State-Driven Architecture**, it allows you to inspect raw network traffic, debug crashes on the fly, and share diagnostic data with ease.

---

## 🚀 Getting Started

### 1. Integration

Add WormaCeptor to your project's dependencies and initialize it in your `Application` class.

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize the interceptor
        WormaCeptorApi.init(this)
        
        // Optional: Enable "Shake to Open" the viewer
        WormaCeptorApi.startActivityOnShake(this)
    }
}
```

### 2. Network Interceptor

Attach the `WormaCeptorInterceptor` to your OkHttp client to start capturing traffic.

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(WormaCeptorInterceptor())
    .build()
```

---

## 🛠 Features

### 📡 Network Inspection
- **Real-time Monitoring**: Track every request/response as it happens.
- **Deep Telemetry**: View Protocol (HTTP/1.1, HTTP/2, H3), SSL/TLS versions, and precise transfer sizes (Request vs. Response).
- **Search & Filter**: Find specific transactions by URL, Method, or Status code. Includes **syntax highlighting** for JSON bodies.
- **Developer Tools**:
    - **Copy as cURL**: One-tap copy to replay requests in your terminal.
    - **Share as JSON**: Export full transaction details for bug reports.
    - **JSON Formatting**: Automatic prettifying of JSON payloads (with performance safety for large files).

### 💥 Crash Reporting
- **In-App Logs**: View detailed crash reports without needing a console.
- **Root Cause Logic**: Automatically extracts the filename and line number from stack traces for immediate context.
- **Visual Status**: Red status indicators help you quickly identify critical failures in the history list.

---

## 📱 Using the Viewer

Once integrated, you can access the WormaCeptor UI:

1.  **Launch**: Shake your device (if enabled) or call `WormaCeptorApi.getLaunchIntent(context)`.
2.  **Tabs**: 
    - **Transactions**: Browse your network history with colored method badges (GET, POST, etc.).
    - **Crashes**: View the history of exceptions with direct code references.
3.  **Search**: Use the search bar in the Request/Response tabs to find specific strings within payloads (matches are highlighted in yellow).
4.  **Export**: Use the overflow menu (⋮) to clear all data or export the current session to a JSON file.

---

## 🏗 Architecture

WormaCeptor is built with a strictly decoupled architecture:
- **`:domain`**: Canonical entities (Requests, Crashes, etc.).
- **`:core`**: The `CaptureEngine` (writing) and `QueryEngine` (reactive reading).
- **`:features:viewer`**: The high-performance Compose UI.
- **`:platform`**: Hardware-specific logic like Shake Detection.

---

## 🛡 Security & Privacy

WormaCeptor is intended for development and staging environments. 
- All data is stored locally on the device.
- It is highly recommended to use **ProGuard/R8** to strip WormaCeptor from production builds if you handle sensitive PII.

---
*Built with ❤️ for Android Developers.*

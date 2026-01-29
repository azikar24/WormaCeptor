---
name: integration-helper
description: Guide host app developers through WormaCeptor integration and troubleshoot issues. Use when setting up WormaCeptor in a new app, configuring the interceptor, or debugging "why isn't it working?" issues.
---

# Integration Helper

Help host app developers integrate and troubleshoot WormaCeptor.

## When to Use

- Setting up WormaCeptor in a new app
- Configuring OkHttp interceptor options
- Debugging "why isn't it capturing traffic?"
- Understanding debug vs release behavior
- Configuring redaction for sensitive data

## Quick Start Integration

### 1. Add Dependencies

```kotlin
// build.gradle.kts (app module)
dependencies {
    // Required - API client (included in release, no-op)
    implementation("com.github.azikar24.WormaCeptor:api-client:VERSION")

    // Debug only - choose ONE:
    // Option A: Persistent storage (survives app restart)
    debugImplementation("com.github.azikar24.WormaCeptor:api-impl-persistence:VERSION")

    // Option B: In-memory (faster, clears on restart)
    debugImplementation("com.github.azikar24.WormaCeptor:api-impl-imdb:VERSION")
}
```

### 2. Initialize in Application

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        WormaCeptorApi.init(
            context = this,
            logCrashes = true,           // Capture uncaught exceptions
            features = Feature.ALL       // Or specific: Feature.NETWORK or Feature.TOOLS
        )
    }
}
```

### 3. Add OkHttp Interceptor

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(
        WormaCeptorInterceptor()
            .showNotification(true)      // Show notification on traffic
            .maxContentLength(250_000L)  // Max body size to capture
            .redactHeader("Authorization")
            .redactHeader("Cookie")
    )
    .build()

// Use with Retrofit
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .client(okHttpClient)
    .build()
```

### 4. Launch Inspector

```kotlin
// From code
WormaCeptorApi.launch(context)

// Via deep link
startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("wormaceptor://transactions")))

// Via notification (if showNotification enabled)
// Via shake gesture (if enabled)
```

## Implementation Options

| Option | Storage | Speed | Use Case |
|--------|---------|-------|----------|
| `api-impl-persistence` | Room DB | Normal | Production debugging, need history |
| `api-impl-imdb` | Memory | Fast | Quick testing, memory constrained |
| No impl (release) | None | Zero | Release builds - automatic no-op |

## Interceptor Configuration

```kotlin
WormaCeptorInterceptor()
    // Notifications
    .showNotification(true)          // Show notification badge

    // Body handling
    .maxContentLength(250_000L)      // Max bytes to capture (default 250KB)
    .retainDataFor(Period.ONE_WEEK)  // Auto-cleanup old data

    // Redaction (security)
    .redactHeader("Authorization")   // Replace value with [REDACTED]
    .redactHeader("X-Api-Key")
    .redactBody("password")          // Redact in JSON bodies
    .redactBody("credit_card")

    // Filtering
    .excludePath("/health")          // Don't capture health checks
    .excludePath("/metrics")
    .onlyPath("/api/")               // Only capture /api/* paths
```

## Deep Links

| URI | Destination |
|-----|-------------|
| `wormaceptor://transactions` | Network transaction list |
| `wormaceptor://crashes` | Crash reports |
| `wormaceptor://tools` | Tools screen |
| `wormaceptor://tools/memory` | Memory monitor |
| `wormaceptor://tools/fps` | FPS monitor |
| `wormaceptor://tools/database` | Database browser |

## Troubleshooting

### "No transactions appearing"

**Check interceptor order:**
```kotlin
// CORRECT - WormaCeptor BEFORE other interceptors
OkHttpClient.Builder()
    .addInterceptor(WormaCeptorInterceptor())  // First
    .addInterceptor(loggingInterceptor)
    .addInterceptor(authInterceptor)
```

**Check implementation is included:**
```bash
./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep wormaceptor
# Should show api-impl-persistence or api-impl-imdb
```

**Check initialization:**
```kotlin
// Must be called before any network requests
WormaCeptorApi.init(this, ...)
```

### "Crashes in release build"

**Wrong dependency type:**
```kotlin
// WRONG - included in release
implementation("...api-impl-persistence...")

// CORRECT - debug only
debugImplementation("...api-impl-persistence...")
```

The `api-client` uses reflection to find implementations. In release builds with no impl, it automatically uses a no-op provider.

### "Bodies are empty or truncated"

**Check content length:**
```kotlin
// Increase if needed (default 250KB)
.maxContentLength(1_000_000L)  // 1MB
```

**Check content type:**
Supported: JSON, XML, HTML, plain text, form data, multipart
Not captured: Binary, images (shown as metadata only)

### "Sensitive data visible"

**Add redaction:**
```kotlin
.redactHeader("Authorization")
.redactHeader("Cookie")
.redactHeader("X-Auth-Token")
.redactBody("password")
.redactBody("ssn")
.redactBody("credit_card")
```

### "Too much noise from health checks"

**Exclude paths:**
```kotlin
.excludePath("/health")
.excludePath("/ping")
.excludePath("/metrics")
```

### "Can't launch inspector"

**Check context:**
```kotlin
// Need Activity context for launch
WormaCeptorApi.launch(activity)  // Not applicationContext
```

**Check deep link handling:**
```xml
<!-- AndroidManifest.xml -->
<activity android:name=".WormaCeptorActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:scheme="wormaceptor" />
    </intent-filter>
</activity>
```

## ProGuard / R8 Rules

Usually not needed - the library includes consumer rules. If issues arise:

```proguard
-keep class com.azikar24.wormaceptor.** { *; }
-keepnames class com.azikar24.wormaceptor.** { *; }
```

## Verify Installation

```kotlin
// Check if WormaCeptor is active (debug builds)
if (WormaCeptorApi.isEnabled()) {
    Log.d("Setup", "WormaCeptor active")
}

// In release builds, isEnabled() returns false
// All API calls become no-ops
```

## Common Patterns

### Retrofit + Hilt

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(WormaCeptorInterceptor().showNotification(true))
            .build()
    }
}
```

### Multiple OkHttp Clients

```kotlin
// Each client needs the interceptor
val apiClient = OkHttpClient.Builder()
    .addInterceptor(WormaCeptorInterceptor())
    .build()

val imageClient = OkHttpClient.Builder()
    .addInterceptor(WormaCeptorInterceptor().excludePath("/images/"))
    .build()
```

### Conditional Initialization

```kotlin
if (BuildConfig.DEBUG) {
    WormaCeptorApi.init(this, logCrashes = true, features = Feature.ALL)
} else {
    // No-op automatically - but explicit is clearer
    WormaCeptorApi.init(this, logCrashes = false, features = Feature.NONE)
}
```

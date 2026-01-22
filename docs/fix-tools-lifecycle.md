# Fix: Tools Lifecycle - Persistent Monitoring with Koin DI

## Problem

Several tools in the Tools tab would stop collecting data when:
1. Navigating away from the tool screen within WormaCeptor
2. Navigating back to the host app (Demo-MainActivity) and returning to WormaCeptor

This was problematic because these tools are meant to monitor the host app continuously.

**Affected tools:**
- Memory Monitor
- FPS Monitor
- CPU Monitor
- View Borders
- Touch Visualization
- Log Capture
- WebSocket Monitor
- Leak Detection
- Thread Violation

## Root Causes

1. **ViewModel `onCleared()` stopping engines** - ViewModels called `engine.stop()` when cleared
2. **Engines created inside `remember {}`** - Lost state on recomposition
3. **Engines created at Activity level** - Lost when ViewerActivity destroyed

## Solution: Koin Dependency Injection

### Architecture

```
+------------------+
|   Koin Module    |  (Singleton scope - persists app lifetime)
|------------------|
| MemoryEngine     |
| FpsEngine        |
| CpuEngine        |
| TouchVizEngine   |
| ViewBordersEngine|
| LogCaptureEngine |
| WebSocketEngine  |
| LeakEngine       |
| ThreadViolEngine |
+------------------+
         |
         | inject()
         v
+------------------+
| ViewerActivity   |  (Uses injected singletons)
+------------------+
```

### Part 1: Koin Module

**File:** `core/engine/src/main/java/.../di/EngineModule.kt`

```kotlin
val engineModule = module {
    // Performance monitoring engines
    single { MemoryMonitorEngine() }
    single { FpsMonitorEngine() }
    single { CpuMonitorEngine() }

    // Visual debug engines
    single { TouchVisualizationEngine(androidContext()) }
    single { ViewBordersEngine() }

    // Logging and monitoring
    single { LogCaptureEngine() }
    single { WebSocketMonitorEngine() }

    // Detection engines
    single { LeakDetectionEngine() }
    single { ThreadViolationEngine() }
}
```

### Part 2: Library-Safe Koin Initialization

**File:** `core/engine/src/main/java/.../di/WormaCeptorKoin.kt`

```kotlin
object WormaCeptorKoin {
    fun init(context: Context) {
        val koin = GlobalContext.getOrNull()
        if (koin != null) {
            // Host app has Koin - load modules into existing instance
            koin.loadModules(listOf(engineModule))
        } else {
            // Start Koin ourselves
            startKoin {
                androidContext(context.applicationContext)
                modules(engineModule)
            }
        }
    }
}
```

### Part 3: ViewerActivity Injection

**File:** `features/viewer/src/main/java/.../ViewerActivity.kt`

```kotlin
class ViewerActivity : ComponentActivity() {
    // Inject engines via Koin
    private val memoryMonitorEngine: MemoryMonitorEngine by inject()
    private val fpsMonitorEngine: FpsMonitorEngine by inject()
    private val cpuMonitorEngine: CpuMonitorEngine by inject()
    // ... other engines

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Koin before injection
        WormaCeptorKoin.init(applicationContext)
        super.onCreate(savedInstanceState)
        // ...
    }
}
```

### Part 4: ViewModel Changes (No Auto-Stop)

```kotlin
override fun onCleared() {
    super.onCleared()
    // Note: We don't stop the engine here - monitoring persists
    // via Koin singleton scope. User controls via explicit start/stop.
}
```

## Benefits of Koin DI

1. **Testability** - Easy to mock engines in tests
2. **Proper Scoping** - Singleton scope manages lifecycle correctly
3. **Decoupling** - Components don't reference static singletons
4. **Host App Compatible** - Safely integrates with apps that already use Koin

## New Behavior

- **Manual toggle only** - User explicitly starts/stops monitoring
- **Persistent state** - Survives Activity destruction/recreation
- **App-process lifetime** - Engines live until process dies
- **Host app safe** - Works alongside existing Koin setup

## Files Changed

| File | Changes |
|------|---------|
| `core/engine/build.gradle.kts` | Added `api(libs.koin.android)` |
| `features/viewer/build.gradle.kts` | Added `implementation(libs.koin.compose)` |
| `di/EngineModule.kt` | **NEW** - Koin module with singleton engines |
| `di/WormaCeptorKoin.kt` | **NEW** - Library-safe Koin initialization |
| `ViewerActivity.kt` | Uses Koin `by inject()` for engines |
| `MemoryViewModel.kt` | Removed auto-stop in `onCleared()` |
| `FpsViewModel.kt` | Removed auto-stop in `onCleared()` |
| `CpuViewModel.kt` | Removed auto-stop in `onCleared()` |
| `ViewBordersViewModel.kt` | Removed auto-stop in `onCleared()` |
| `MonitoringEngineHolder.kt` | **DELETED** - Replaced by Koin |

## Testing

1. Open WormaCeptor from Demo app
2. Go to FPS Monitor, start monitoring
3. Navigate back to Demo app
4. Interact with Demo app (generates FPS data)
5. Open WormaCeptor again
6. Go to FPS Monitor
7. **Verify**: FPS should still be monitoring with collected data

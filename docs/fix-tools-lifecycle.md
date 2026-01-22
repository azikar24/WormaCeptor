# Fix: Tools Lifecycle - Persistent Monitoring Across Activity Lifecycle

## Problem

Several tools in the Tools tab would stop collecting data when:
1. Navigating away from the tool screen within WormaCeptor
2. **Navigating back to the host app (Demo-MainActivity) and returning to WormaCeptor**

This was problematic because these tools are meant to monitor the host app continuously, not just when actively viewed.

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

### 1. ViewModel `onCleared()` stopping engines (Navigation within WormaCeptor)

ViewModels were calling `engine.stop()` or `engine.disable()` in their `onCleared()` method.

### 2. Engines created inside `remember {}` (Navigation within WormaCeptor)

Engines were being created inside the composable's `remember {}` block, which loses state when the composable leaves composition.

### 3. Engines created at Activity level (Navigation to host app)

Even when engines were created at Activity level, they were lost when ViewerActivity was destroyed (e.g., pressing back to return to Demo app).

## Solution

### Part 1: MonitoringEngineHolder Singleton

Created a new singleton holder that persists engine instances across the entire app process lifetime:

**New file:** `core/engine/src/main/java/.../MonitoringEngineHolder.kt`

```kotlin
object MonitoringEngineHolder {
    private var _memoryMonitorEngine: MemoryMonitorEngine? = null
    private var _fpsMonitorEngine: FpsMonitorEngine? = null
    // ... other engines

    val memoryMonitorEngine: MemoryMonitorEngine
        get() {
            if (_memoryMonitorEngine == null) {
                _memoryMonitorEngine = MemoryMonitorEngine()
            }
            return _memoryMonitorEngine!!
        }
    // ... other getters
}
```

### Part 2: Remove auto-stop in ViewModel `onCleared()`

Updated all affected ViewModels to not stop their engines when cleared:

**Files modified:**
- `features/memory/src/main/java/.../vm/MemoryViewModel.kt`
- `features/fps/src/main/java/.../vm/FpsViewModel.kt`
- `features/cpu/src/main/java/.../vm/CpuViewModel.kt`
- `features/viewborders/src/main/java/.../vm/ViewBordersViewModel.kt`

```kotlin
override fun onCleared() {
    super.onCleared()
    // Note: We don't stop the engine here - monitoring persists across navigation.
    // The engine lifecycle is managed by the user via explicit start/stop.
}
```

### Part 3: Require engine parameter in composables

Changed composables to require engine as a parameter instead of creating internally:

**Files modified:**
- `features/memory/src/main/java/.../MemoryFeature.kt`
- `features/fps/src/main/java/.../FpsFeature.kt`
- `features/cpu/src/main/java/.../CpuFeature.kt`

```kotlin
@Composable
fun MemoryMonitor(
    engine: MemoryMonitorEngine,  // Required parameter
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) { ... }
```

### Part 4: ViewerActivity uses MonitoringEngineHolder

Updated ViewerActivity to get engines from the singleton holder:

**File modified:**
- `features/viewer/src/main/java/.../ViewerActivity.kt`

```kotlin
class ViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Uses singleton engines
        MonitoringEngineHolder.logCaptureEngine.start()
        // ...

        composable("memory") {
            MemoryMonitor(
                engine = MonitoringEngineHolder.memoryMonitorEngine,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        // ... same for all other tools
    }

    override fun onDestroy() {
        super.onDestroy()
        // Note: Engines are NOT stopped here - they persist across Activity lifecycle
    }
}
```

## New Behavior

- **Manual toggle only**: User must explicitly start/stop monitoring
- **Persistent across navigation**: Monitoring continues while navigating within WormaCeptor
- **Persistent across Activity lifecycle**: Monitoring continues when returning to host app and back
- **Stops on**: User manually stops OR app process dies

## Lifecycle Diagram

```
App Process Start
    |
    v
MonitoringEngineHolder created (lazy, singleton)
    |
    +---> ViewerActivity created
    |         |
    |         v
    |     Get engines from holder
    |         |
    |         v
    |     User starts FPS monitoring
    |         |
    |         v
    |     User navigates back to Demo app
    |         |
    |         v
    |     ViewerActivity destroyed
    |     (engines NOT stopped - still in holder)
    |         |
    |         v
    +---> ViewerActivity created again
              |
              v
          Get SAME engines from holder
          (FPS still monitoring!)
```

## Files Changed Summary

| File | Changes |
|------|---------|
| `MonitoringEngineHolder.kt` | **NEW** - Singleton holder for all monitoring engines |
| `MemoryViewModel.kt` | Removed `init { startMonitoring() }`, removed `engine.stop()` in `onCleared()` |
| `FpsViewModel.kt` | Removed `engine.stop()` in `onCleared()` |
| `CpuViewModel.kt` | Removed `init { startMonitoring() }`, removed `engine.stop()` in `onCleared()` |
| `ViewBordersViewModel.kt` | Removed `engine.disable()` in `onCleared()` |
| `MemoryFeature.kt` | Added `engine` parameter to `MemoryMonitor()` composable |
| `FpsFeature.kt` | Made `engine` parameter required in `FpsMonitor()` composable |
| `CpuFeature.kt` | Added `engine` parameter to `CpuMonitor()` composable |
| `ViewerActivity.kt` | Removed local engines, uses `MonitoringEngineHolder` for all engines |

## Testing

1. Open WormaCeptor from Demo app
2. Go to FPS Monitor, start monitoring
3. Navigate back to Demo app
4. Interact with Demo app (generates FPS data)
5. Open WormaCeptor again
6. Go to FPS Monitor
7. **Verify**: FPS should still be monitoring with data from step 4

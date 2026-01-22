# Performance Overlay Design

## Overview

A draggable floating overlay that displays real-time performance metrics (FPS, Memory, CPU) on top of the host app. Tapping expands to show mini sparkline charts, with deep links to open detailed screens in WormaCeptor.

## User Experience

### Collapsed State (Badge)
- Small rounded pill shape (~120dp x 40dp)
- Semi-transparent dark background (80% opacity) with blur effect
- Three sections with icons: `[CPU] 32% | [RAM] 45% | [FPS] 60`
- Icons: 16dp Material icons in accent color
- Text: white, monospace font for consistent width
- Subtle border for visibility against any background

### Expanded State
- Badge grows downward to ~120dp x 180dp
- Three rows, each containing:
  - Icon + current value (left)
  - Mini sparkline chart showing last 30 seconds (right)
- Color coding: green (good), yellow (warning), red (critical)
- "Open WormaCeptor" button at bottom
- Tap outside or swipe up to collapse

### Interaction
- Single tap: expand/collapse
- Long-press: enter drag mode (scale-up animation feedback)
- Drag: move anywhere on screen
- Tap metric row in expanded state: deep link to that specific screen
- Tap "Open WormaCeptor": open Tools tab

### Position Behavior
- Initial position: top-right corner
- Draggable anywhere on screen
- Position persisted across sessions (SharedPreferences)
- Position stored as percentage of screen dimensions for rotation handling

## Technical Implementation

### Service Architecture
- New `PerformanceOverlayEngine` in `core/engine` module
- Uses `WindowManager` with `TYPE_APPLICATION_OVERLAY`
- Requires `SYSTEM_ALERT_WINDOW` permission
- Lifecycle tied to monitoring state - shows only when monitoring active

### Data Flow
- Collects from existing engines:
  - `FpsMonitorEngine`
  - `MemoryMonitorEngine`
  - `CpuMonitorEngine`
- Combines StateFlows into single UI state
- Updates every 1 second (configurable)

### Overlay View
- Custom `ComposeView` inside WindowManager
- Jetpack Compose for badge/expanded UI
- Canvas-based mini sparklines (reuses existing chart patterns)

### Deep Links
- `wormaceptor://tools/memory` - Memory screen
- `wormaceptor://tools/fps` - FPS screen
- `wormaceptor://tools/cpu` - CPU screen
- General "Open" button opens Tools tab

## State Management

### Runtime State
```kotlin
data class PerformanceOverlayState(
    val isExpanded: Boolean = false,
    val position: Offset = Offset.Zero,
    val isDragging: Boolean = false,
    val fpsValue: Int = 0,
    val fpsHistory: List<Float> = emptyList(),
    val memoryPercent: Int = 0,
    val memoryHistory: List<Float> = emptyList(),
    val cpuPercent: Int = 0,
    val cpuHistory: List<Float> = emptyList()
)
```

### Persisted Settings (SharedPreferences)
- `overlay_enabled: Boolean` - overlay active state
- `overlay_position_x: Float` - X position (0.0-1.0 percentage)
- `overlay_position_y: Float` - Y position (0.0-1.0 percentage)
- `overlay_metrics: Set<String>` - which metrics to show (future)

## File Structure

### New Files
```
core/engine/src/main/java/com/azikar24/wormaceptor/core/engine/
  ├── PerformanceOverlayEngine.kt      # WindowManager overlay management
  └── PerformanceOverlayState.kt       # State data classes

core/engine/src/main/java/com/azikar24/wormaceptor/core/engine/ui/
  └── PerformanceOverlayContent.kt     # Compose UI (badge + expanded)

features/viewer/src/main/java/com/azikar24/wormaceptor/feature/viewer/navigation/
  └── DeepLinkHandler.kt               # Handle wormaceptor:// deep links
```

### Modified Files
- `WormaCeptorActivity.kt` - Deep link intent handling
- `SettingsScreen.kt` - Overlay toggle setting
- `WormaCeptorApi.kt` - Public API for overlay control
- `AndroidManifest.xml` - Register deep link scheme

## Dependencies
- Existing monitoring engines (Koin DI)
- Existing theme/design tokens
- WindowManager system service
- SharedPreferences for persistence

## Color Thresholds

| Metric | Green | Yellow | Red |
|--------|-------|--------|-----|
| FPS | >= 55 | 30-54 | < 30 |
| Memory | < 60% | 60-80% | > 80% |
| CPU | < 50% | 50-80% | > 80% |

# Tool Overlay Implementation Progress

## Goal
Add a floating toolbar that appears when View Borders or Measurement Tool is active, allowing users to toggle these tools without returning to WormaCeptor UI.

## Design Decisions
- Combined floating toolbar (vertical stack of 2 buttons)
- Auto-shows when either tool is enabled, auto-hides when both disabled
- Free-floating, draggable anywhere, position persisted
- Active state: filled icon + green color; inactive: outlined + grey

## Implementation Tasks

### Phase 1: Core Engine
- [ ] Create `ToolOverlayState.kt` - state data class
- [ ] Create `ToolOverlayEngine.kt` - main engine following PerformanceOverlayEngine pattern
- [ ] Create `ToolOverlayContent.kt` - Compose UI for the toolbar
- [ ] Register `ToolOverlayEngine` in `EngineModule.kt`

### Phase 2: Engine Modifications
- [ ] Modify `ViewBordersEngine.kt` to support cross-activity persistence
- [ ] Modify `MeasurementEngine.kt` to support cross-activity persistence
- [ ] Register `MeasurementEngine` in `EngineModule.kt` (currently missing)

### Phase 3: Integration
- [ ] Update `ViewBordersViewModel.kt` to trigger ToolOverlayEngine
- [ ] Update Measurement feature to trigger ToolOverlayEngine
- [ ] Test across activity navigation

## Current Status
- [x] Explored codebase architecture
- [x] Wrote design document
- [x] Phase 1: Created ToolOverlayState.kt
- [x] Phase 1: Created ToolOverlayEngine.kt
- [x] Phase 1: Created ToolOverlayContent.kt
- [x] Phase 1: Updated EngineModule.kt (added MeasurementEngine and ToolOverlayEngine)
- [x] Phase 2: Updated ViewBordersViewModel to integrate with ToolOverlayEngine
- [x] Phase 2: Updated ViewBordersFeature to use Koin singletons
- [x] Phase 2: Updated MeasurementFeature to use Koin singletons and ToolOverlayEngine
- [x] Build successful

## Implementation Complete

The floating toolbar feature is now implemented. When the user enables View Borders or Measurement Tool from WormaCeptor, a floating toolbar will appear that:
- Persists across activity navigation within the host app
- Shows two toggle buttons (View Borders and Measurement)
- Is draggable anywhere on screen
- Saves position across sessions
- Auto-shows when either tool is enabled
- Auto-hides when both tools are disabled
- Re-attaches tool overlays when navigating to new activities

## Debugging

Added logging to ToolOverlayEngine with tag "ToolOverlayEngine". To debug:
```
adb logcat -s ToolOverlayEngine
```

Expected log sequence when enabling View Borders:
1. `onViewBordersStateChanged: enabled=true`
2. `updateVisibility: shouldShow=true, isVisible=false`
3. `Showing tool overlay for activity: ViewerActivity`
4. `Overlay view added successfully at position (...)`

If you see `Cannot show overlay - SYSTEM_ALERT_WINDOW permission not granted`, you need to grant "Draw over other apps" permission.

## Known Requirement

The floating toolbar requires `SYSTEM_ALERT_WINDOW` permission (same as Performance Overlay). This permission must be granted in Android Settings > Apps > Your App > Draw over other apps.

## Files to Create
1. `core/engine/src/main/java/.../ToolOverlayState.kt`
2. `core/engine/src/main/java/.../ToolOverlayEngine.kt`
3. `core/engine/src/main/java/.../ui/ToolOverlayContent.kt`

## Files to Modify
1. `core/engine/src/main/java/.../di/EngineModule.kt`
2. `core/engine/src/main/java/.../ViewBordersEngine.kt`
3. `core/engine/src/main/java/.../MeasurementEngine.kt`
4. `features/viewborders/.../ViewBordersViewModel.kt`
5. `features/measurement/.../MeasurementFeature.kt`

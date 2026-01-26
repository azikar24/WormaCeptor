# Tool Overlay Design

## Overview

Add a floating toolbar that appears when View Borders or Measurement Tool is active, allowing users to toggle these tools without returning to WormaCeptor UI.

## Requirements

1. **Persistence**: Toolbar survives activity navigation within the host app
2. **Visibility**: Auto-shows when either tool is enabled, auto-hides when both disabled
3. **Layout**: Vertical stack of two icon buttons
4. **Position**: Free-floating, draggable anywhere, position persisted across sessions
5. **Visual feedback**: Active tools show filled icon with green accent, inactive show outlined grey

## Architecture

### New Components

**`ToolOverlayEngine`** (`core/engine`)
- Manages floating toolbar lifecycle and visibility
- Uses application-level `WindowManager` for cross-activity persistence
- Implements `Application.ActivityLifecycleCallbacks` to track current activity
- Stores position as percentage (0.0-1.0) in SharedPreferences
- Implements `LifecycleOwner`/`SavedStateRegistryOwner` for Compose

**`ToolOverlayState`** (`core/engine`)
- Data class holding toolbar state (visibility, tool states, position)

### Integration

```
ViewBordersEngine.isEnabled ─┐
                             ├─> ToolOverlayEngine.updateVisibility()
MeasurementEngine.isEnabled ─┘

ToolOverlayEngine ─> ViewBordersEngine.toggle()
                 ─> MeasurementEngine.toggle()
```

## UI Specification

### Button Specs
- Size: 48x48dp (WCAG touch target)
- Corner radius: 12.dp
- Spacing between buttons: 8.dp
- Container background: Semi-transparent surface

### Active State
- Background: StatusGreen with 0.12f alpha
- Icon: Filled variant, StatusGreen color

### Inactive State
- Background: Surface with 0.08f alpha
- Icon: Outlined variant, StatusGrey color

### Interactions
- Tap: Toggle respective tool
- Long press + drag: Reposition toolbar
- Press: Scale animation feedback

## Files to Create

1. `core/engine/src/main/java/com/azikar24/wormaceptor/core/engine/ToolOverlayEngine.kt`
2. `core/engine/src/main/java/com/azikar24/wormaceptor/core/engine/ToolOverlayState.kt`

## Files to Modify

1. `ViewBordersEngine.kt` - Expose state flow for observation
2. `MeasurementEngine.kt` - Expose state flow for observation
3. `ViewBordersViewModel.kt` - Initialize ToolOverlayEngine on enable
4. `MeasurementViewModel.kt` - Initialize ToolOverlayEngine on enable
5. Engine module's Koin module - Register ToolOverlayEngine

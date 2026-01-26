# UI Consistency Standards

This document captures the standards for search bars and containers established during the consistency audit.

## Search Bars

**Standard Component:** `WormaCeptorSearchBar` (in `core/ui`)

All search bars should use the unified `WormaCeptorSearchBar` component which provides:
- Filled background style (surfaceVariant at 40% alpha)
- No border indicator (transparent)
- Rounded corners (lg = 12.dp)
- Animated clear button
- Consistent placeholder styling

**Files Updated:**
- `features/filebrowser/ui/FileBrowserScreen.kt` - migrated from Material3 SearchBar
- `features/database/ui/DatabaseListScreen.kt` - migrated from Material3 SearchBar
- `features/database/ui/TableListScreen.kt` - migrated from Material3 SearchBar
- `features/viewer/ui/ToolsTab.kt` - migrated from custom ToolsSearchBar

**Files Already Using WormaCeptorSearchBar:**
- `features/websocket/ui/WebSocketDetailScreen.kt`
- `features/websocket/ui/WebSocketListScreen.kt`
- `features/viewhierarchy/ui/ViewHierarchyScreen.kt`
- `features/securestorage/ui/SecureStorageScreen.kt`
- `features/logs/ui/LogsScreen.kt`
- `features/loadedlibraries/ui/LoadedLibrariesScreen.kt`
- `features/dependenciesinspector/ui/DependenciesInspectorScreen.kt`

## Containers

**Standard Component:** `WormaCeptorContainer` (in `core/ui`)

Two variants available:

### ContainerStyle.Filled
- Solid background (surfaceVariant at strong alpha)
- No border
- Best for: cards, tiles, content areas

### ContainerStyle.Outlined
- Subtle border (outlineVariant at medium alpha)
- Light background (surfaceVariant at subtle alpha)
- Best for: list items, selectable cards, grouped content

**Usage Example:**
```kotlin
// Non-clickable outlined container
WormaCeptorContainer(
    style = ContainerStyle.Outlined,
    modifier = Modifier.padding(16.dp)
) {
    Text("Content here")
}

// Clickable filled container
WormaCeptorContainer(
    onClick = { /* handle click */ },
    style = ContainerStyle.Filled,
) {
    Text("Clickable content")
}
```

## Files Pending Container Migration

These files use the old `.clip().border().background()` pattern and could be migrated to `WormaCeptorContainer` in future work:

- `features/cookies/ui/CookieDetailScreen.kt`
- `features/cookies/ui/CookiesListScreen.kt`
- `features/viewer/ui/components/SelectableTransactionItem.kt`
- `features/viewer/ui/components/LoadingStates.kt`
- `features/viewer/ui/components/MetricsCard.kt`
- `features/viewer/ui/components/ImagePreviewCard.kt`
- `features/touchvisualization/ui/TouchVisualizationScreen.kt`

## Design System Tokens

When styling containers manually, use these consistent values from `WormaCeptorDesignSystem`:

| Purpose | Token | Value |
|---------|-------|-------|
| Border width | `BorderWidth.regular` | 1.dp |
| Border color | `outlineVariant.copy(alpha = Alpha.medium)` | 20% alpha |
| Background (filled) | `surfaceVariant.copy(alpha = Alpha.strong)` | 40% alpha |
| Background (outlined) | `surfaceVariant.copy(alpha = Alpha.subtle)` | 8% alpha |
| Corner radius | `CornerRadius.md` | 8.dp |

## Module Dependencies

Feature modules using `WormaCeptorSearchBar` or `WormaCeptorContainer` must add:
```kotlin
implementation(project(":core:ui"))
```

# Color System Consolidation

## Summary

Consolidated all hardcoded colors into a unified color system within `WormaCeptorColors` object in `Color.kt`. This provides a single source of truth for all semantic colors used across the application.

## Changes Made

### 1. Extended WormaCeptorColors Object

Added three new nested objects to `WormaCeptorColors` in `/features/viewer/src/main/java/com/azikar24/wormaceptor/feature/viewer/ui/theme/Color.kt`:

#### CategoryColors
Tool category colors for visual differentiation in the Tools tab:
- `Inspection` - Indigo (0xFF6366F1)
- `Performance` - Amber (0xFFF59E0B)
- `Network` - Emerald (0xFF10B981)
- `Simulation` - Purple (0xFF8B5CF6)
- `VisualDebug` - Pink (0xFFEC4899)
- `Core` - Blue (0xFF3B82F6)
- `Favorites` - Amber (0xFFF59E0B)
- `Fallback` - Gray (0xFF6B7280)

#### ContentTypeColors
Content type colors for content type chips and visual identification:
- `Json` - Amber (0xFFF59E0B)
- `Xml` - Purple (0xFF8B5CF6)
- `Html` - Pink (0xFFEC4899)
- `Protobuf` - Emerald (0xFF10B981)
- `FormData` - Blue (0xFF3B82F6)
- `Multipart` - Indigo (0xFF6366F1)
- `PlainText` - Gray (0xFF6B7280)
- `Binary` - Red (0xFFEF4444)
- `Pdf` - Red-600 (0xFFDC2626)
- `Image` - Teal (0xFF14B8A6)
- `Unknown` - Gray-400 (0xFF9CA3AF)

#### HttpMethodColors
HTTP method colors for method badges and filters:
- `Get` - Blue (0xFF3B82F6)
- `Post` - Green (0xFF10B981)
- `Put` - Amber (0xFFF59E0B)
- `Patch` - Purple (0xFF9C27B0)
- `Delete` - Red (0xFFEF4444)
- `Head` - Gray (0xFF6B7280)
- `Options` - Violet (0xFF8B5CF6)

### 2. Updated Files to Use Consolidated Colors

#### ToolsTab.kt
- Replaced local `CategoryColors` object with `WormaCeptorColors.CategoryColors`
- Renamed helper object to `CategoryHelper` to avoid naming conflicts
- Updated all references to use centralized color definitions

#### ContentTypeChip.kt
- Replaced inline color values with `WormaCeptorColors.ContentTypeColors` references
- Added import for `WormaCeptorColors`

#### CommonUtils.kt
- Updated `getMethodColor()` function to use `WormaCeptorColors.HttpMethodColors`
- Added support for HEAD and OPTIONS HTTP methods

#### PagedTransactionListScreen.kt
- Updated `methodColor()` function to use `WormaCeptorColors.HttpMethodColors`
- Added support for HEAD and OPTIONS HTTP methods

#### FilterBottomSheetContent.kt
- Updated `methodColor()` function to use `WormaCeptorColors.HttpMethodColors`
- Added support for HEAD and OPTIONS HTTP methods

## Benefits

1. **Single Source of Truth**: All semantic colors are now defined in one location
2. **Type Safety**: Using object properties instead of inline Color() calls
3. **Consistency**: Same color values used across all features
4. **Maintainability**: Easier to update colors globally
5. **Discoverability**: IDE autocomplete makes it easy to find available colors
6. **Documentation**: Each color object has clear documentation about its purpose

## Usage Examples

```kotlin
// Tool category colors
val categoryColor = WormaCeptorColors.CategoryColors.Performance

// Content type colors
val contentColor = WormaCeptorColors.ContentTypeColors.Json

// HTTP method colors
val methodColor = WormaCeptorColors.HttpMethodColors.Post

// Status colors (existing)
val statusColor = WormaCeptorColors.StatusGreen
```

## Migration Notes

- Legacy `ContentPurple` and `ContentCyan` colors are still available but marked as deprecated
- All hardcoded Color() values for categories, content types, and HTTP methods have been removed
- Helper functions now reference the consolidated color objects instead of local definitions

## Related Documentation

See `CLAUDE.md` section on "Semantic Colors" for the complete color palette reference.

# Container Consistency Audit and Standardization

## Design Standard
All cards/containers should follow this pattern:
```kotlin
Surface(
    modifier = modifier,
    shape = WormaCeptorDesignSystem.Shapes.card,  // 8dp corner radius
    tonalElevation = WormaCeptorDesignSystem.Elevation.xs,  // 1dp elevation
    color = backgroundColor,
) {
    Column(modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg)) {  // 16dp padding
        // content
    }
}
```

## Fixed Files

### ✓ MemoryScreen.kt
- Changed Card → Surface
- Changed RoundedCornerShape(16.dp) → WormaCeptorDesignSystem.Shapes.card
- Added tonalElevation = WormaCeptorDesignSystem.Elevation.xs
- Changed padding(16.dp) → padding(WormaCeptorDesignSystem.Spacing.lg)
- Removed CardDefaults import

### ✓ FpsScreen.kt
- Changed RoundedCornerShape(16.dp) → WormaCeptorDesignSystem.Shapes.card
- Changed RoundedCornerShape(12.dp) → WormaCeptorDesignSystem.Shapes.card
- Added tonalElevation = WormaCeptorDesignSystem.Elevation.xs
- Changed padding(16.dp) → padding(WormaCeptorDesignSystem.Spacing.lg)

### ✓ CpuScreen.kt
- Changed Card → Surface
- Changed RoundedCornerShape(16.dp) → WormaCeptorDesignSystem.Shapes.card
- Added tonalElevation = WormaCeptorDesignSystem.Elevation.xs
- Changed padding(16.dp) → padding(WormaCeptorDesignSystem.Spacing.lg)
- Removed Card and CardDefaults imports

### ✓ CrashListScreen.kt
- Removed double wrapping (Surface + Box with border)
- Changed RoundedCornerShape(CornerRadius.md) → WormaCeptorDesignSystem.Shapes.card
- Changed tonalElevation from 0.dp → WormaCeptorDesignSystem.Elevation.xs
- Changed padding(Spacing.md) → padding(WormaCeptorDesignSystem.Spacing.lg)
- Removed border() modifier (no borders except for selected/focused states)

### ✓ ToolsTab.kt
- Changed Card → Surface (for ToolTile)
- Changed RoundedCornerShape(12.dp) → WormaCeptorDesignSystem.Shapes.card
- Added tonalElevation = WormaCeptorDesignSystem.Elevation.xs
- Removed border property (was using borderStroke)
- Removed Card and CardDefaults imports

## Files Still Using Card (Need Review)

These files still use `Card` and need to be standardized:
1. DeviceInfoScreen.kt
2. FileViewerScreen.kt
3. TransactionDetailScreen.kt
4. LocationScreen.kt
5. CrashDetailScreen.kt
6. ViewHierarchyScreen.kt
7. HomeScreen.kt
8. ThreadViolationScreen.kt
9. SecureStorageScreen.kt
10. LoadedLibrariesScreen.kt
11. DependenciesInspectorScreen.kt
12. CookieDetailScreen.kt
13. TouchVisualizationScreen.kt
14. ViewBordersScreen.kt
15. LeakDetectionScreen.kt
16. PushSimulatorScreen.kt
17. RateLimitScreen.kt
18. WebSocketDetailScreen.kt
19. WebSocketListScreen.kt
20. LogsScreen.kt

## Files Using Non-Standard Corner Radii

These files use RoundedCornerShape with 12dp or 16dp instead of 8dp (Shapes.card):
- WebSocketDetailScreen.kt
- WebSocketListScreen.kt
- ViewHierarchyScreen.kt
- ThreadViolationScreen.kt
- SecureStorageScreen.kt
- LogsScreen.kt
- TouchVisualizationScreen.kt
- ViewBordersScreen.kt
- LeakDetectionScreen.kt
- RateLimitScreen.kt

## Inconsistencies Found

### 1. **Container Type Inconsistency**
- Some use `Surface` (correct)
- Some use `Card` (should be Surface)
- Some use `Box` with background (should be Surface)

### 2. **Corner Radius Inconsistency**
- Some use 8dp (correct - Shapes.card)
- Some use 12dp (incorrect)
- Some use 16dp (incorrect)
- Some hardcode RoundedCornerShape instead of using Shapes.card

### 3. **Border Inconsistency**
- Transaction items have borders (intentional for status)
- Crash items had borders (removed - was redundant)
- Tool tiles had borders (removed)
- Most cards should NOT have borders

### 4. **Elevation Inconsistency**
- Some use tonalElevation = 0.dp (incorrect)
- Some use tonalElevation = 1.dp (correct but hardcoded)
- Should use tonalElevation = Elevation.xs

### 5. **Padding Inconsistency**
- Some use padding(16.dp) (hardcoded)
- Some use padding(12.dp)
- Should use padding(Spacing.lg) or CardPadding.regular

## Next Steps

1. Continue fixing remaining screens to use Surface instead of Card
2. Ensure all containers use Shapes.card for shape
3. Ensure all containers use Elevation.xs for tonalElevation
4. Ensure all containers use Spacing.lg for content padding
5. Remove all unnecessary borders (keep only for interactive/selected states)
6. Run spotlessApply and build to verify no issues

## Notes

- TransactionItem in TransactionListScreen.kt intentionally uses Row with background and border for status indication - this is acceptable
- SelectableTransactionItem may have borders for selection state - this is acceptable
- Empty states and special containers may have different styling - review case by case

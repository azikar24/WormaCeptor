# Refactor: Move Loading/Empty/Error State Components to core:ui

**Date**: 2026-01-25
**Status**: Completed

## Summary

Moved reusable loading, empty, and error state components from `features/viewer` to `core/ui` module to enable sharing across all features.

## Changes Made

### New Files Created

1. **`core/ui/src/main/java/com/azikar24/wormaceptor/core/ui/components/WormaCeptorLoadingStates.kt`**
   - Moved generic loading/error state components:
     - `rememberShimmerBrush()` - Shimmer animation brush
     - `SkeletonBox` - Generic skeleton placeholder
     - `LoadingMoreIndicator` - Pagination loading indicator
     - `CompactLoadingSpinner` - Inline spinner
     - `FullScreenLoading` - Full screen loading state
     - `ErrorState` - Error with retry functionality
     - `InlineErrorRetry` - Compact inline error
     - `ErrorType` - Error type enum (GENERIC, NETWORK)
     - `ScrollToTopFab` - Floating action button to scroll to top
     - `ScrollToTopExtendedFab` - Extended FAB with label

2. **`core/ui/src/main/java/com/azikar24/wormaceptor/core/ui/components/WormaCeptorEmptyState.kt`**
   - Created unified empty state component:
     - `WormaCeptorEmptyState` - Generic empty state with customizable title, subtitle, icon, and action

### Modified Files

1. **`features/viewer/build.gradle.kts`**
   - Added dependency on `core:ui` module

2. **`features/viewer/src/main/java/com/azikar24/wormaceptor/feature/viewer/ui/components/LoadingStates.kt`**
   - Removed generic components (now in core:ui)
   - Added re-exports with deprecation annotations for backward compatibility
   - Kept transaction-specific components:
     - `TransactionItemSkeleton`
     - `TransactionListSkeleton`
     - `TransactionDetailSkeleton`
     - `RequestResponseTabSkeleton`
     - `LoadMoreBodyButton`
     - `BodyLoadingProgress`
   - Updated `EnhancedEmptyState` to use `WormaCeptorEmptyState`

## Design Decisions

### Component Separation

**Generic components (moved to core:ui)**:
- These components have no transaction-specific logic
- They accept customizable parameters for flexibility
- They use design system tokens for consistency
- Examples: shimmer animation, error states, loading spinners

**Transaction-specific components (kept in viewer)**:
- These components match exact transaction list/detail layouts
- They depend on transaction domain logic
- They import and use generic components from core:ui
- Examples: `TransactionItemSkeleton`, `TransactionDetailSkeleton`

### Backward Compatibility

To avoid breaking existing code, the viewer module provides:
- Re-exports of core:ui components with original names
- Deprecation annotations pointing to new locations
- Wrappers that inject transaction-specific colors (e.g., `WormaCeptorColors.StatusRed`)

Example:
```kotlin
@Deprecated(
    message = "Use WormaCeptorErrorState from core:ui instead",
    replaceWith = ReplaceWith(
        "WormaCeptorErrorState(message, onRetry, modifier, errorType, isRetrying, WormaCeptorColors.StatusRed)",
        "com.azikar24.wormaceptor.core.ui.components.ErrorState",
    ),
)
@Composable
fun ErrorState(...) {
    WormaCeptorErrorState(..., errorColor = WormaCeptorColors.StatusRed)
}
```

### Error Color Parameterization

The core:ui error components accept an `errorColor` parameter instead of hardcoding `WormaCeptorColors.StatusRed`:
- Enables feature modules to use different semantic colors
- Viewer module passes `WormaCeptorColors.StatusRed` in backward compatibility wrappers
- Other features can use `MaterialTheme.colorScheme.error` or custom colors

### Empty State Simplification

The original `EnhancedEmptyState` had complex conditional logic and custom icon rendering. The new approach:
- Uses generic `WormaCeptorEmptyState` with simple parameters
- Conditional logic moved to call site in viewer module
- Icons use Material Icons instead of custom shapes
- Result: simpler, more maintainable code

## Benefits

1. **Code Reuse**: Other features can now use these components without duplicating code
2. **Consistency**: All features will have consistent loading/error/empty states
3. **Maintainability**: Single source of truth for common UI patterns
4. **Modularity**: Reinforces clean architecture boundaries
5. **Backward Compatibility**: Existing code continues to work without changes

## Usage Examples

### From Other Feature Modules

```kotlin
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.ErrorState
import com.azikar24.wormaceptor.core.ui.components.FullScreenLoading

@Composable
fun MyFeatureScreen() {
    when (state) {
        is Loading -> FullScreenLoading(message = "Loading crashes...")
        is Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.retry() },
            errorType = ErrorType.NETWORK,
        )
        is Empty -> WormaCeptorEmptyState(
            title = "No crashes",
            subtitle = "Your app is running smoothly",
            icon = Icons.Outlined.CheckCircle,
        )
    }
}
```

### Custom Skeleton Components

Feature modules can create their own skeletons using the generic building blocks:

```kotlin
import com.azikar24.wormaceptor.core.ui.components.SkeletonBox
import com.azikar24.wormaceptor.core.ui.components.rememberShimmerBrush

@Composable
fun MyCustomSkeleton() {
    val shimmerBrush = rememberShimmerBrush()

    Row {
        SkeletonBox(width = 48.dp, height = 48.dp, brush = shimmerBrush)
        Column {
            SkeletonBox(width = 120.dp, height = 16.dp, brush = shimmerBrush)
            SkeletonBox(width = 80.dp, height = 12.dp, brush = shimmerBrush)
        }
    }
}
```

## Migration Path for Features

1. **Add dependency** in your feature module's `build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation(project(":core:ui"))
       // ... other dependencies
   }
   ```

2. Import components from `com.azikar24.wormaceptor.core.ui.components`
3. For error states, pass your feature's error color as parameter
4. For empty states, use `WormaCeptorEmptyState` with custom title/subtitle/icon
5. For loading states, use `FullScreenLoading`, `LoadingMoreIndicator`, or `CompactLoadingSpinner`
6. Create feature-specific skeletons using `SkeletonBox` and `rememberShimmerBrush()`

## Testing

- Verified `core:ui` module compiles successfully
- Verified `features:viewer` module compiles successfully with backward compatibility
- Existing transaction list/detail screens continue to work without changes

## Next Steps

1. Gradually migrate other features to use core:ui components directly
2. Remove deprecated wrappers from viewer module in future cleanup (after features migrate)
3. Consider adding more reusable components to core:ui (e.g., `WormaCeptorCard`, `WormaCeptorBadge`)

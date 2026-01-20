ok us# Unused Functions and Components

This document lists unused functions, classes, and components found in the WormaCeptor codebase.

---

## Modifier Extensions (LongPressModifier.kt)

**File:** `features/viewer/src/main/java/com/azikar24/wormaceptor/feature/viewer/ui/components/quickactions/LongPressModifier.kt`

| Function | Line | Description |
|----------|------|-------------|
| `longPressWithFeedback()` | 60 | Modifier extension providing long-press detection with visual feedback |
| `longPressWithProgress()` | 152 | Modifier extension with progress tracking for long-press |
| `LongPressState` class | 225 | State holder class for long-press interactions |
| `rememberLongPressState()` | 237 | Composable function to remember LongPressState |
| `longPressState()` | 247 | Modifier extension for updating LongPressState |
| `animatedLongPress()` | 303 | Composable modifier combining scale animation with long-press state |

---

## Swipe Refresh Components (SwipeRefreshWrapper.kt)

**File:** `features/viewer/src/main/java/com/azikar24/wormaceptor/feature/viewer/ui/components/gesture/SwipeRefreshWrapper.kt`

| Function | Line | Description |
|----------|------|-------------|
| `CustomSwipeRefreshWrapper()` | 103 | Alternative pull-to-refresh using pure Compose gestures with custom indicator |
| `SimpleSwipeRefresh()` | 179 | Simplified wrapper around Material3's PullToRefreshBox |

---

## Gesture Navigation Components (GestureNavigationComponents.kt)

**File:** `features/viewer/src/main/java/com/azikar24/wormaceptor/feature/viewer/ui/components/gesture/GestureNavigationComponents.kt`

| Function | Line | Description |
|----------|------|-------------|
| `CompactPositionIndicator()` | 352 | Compact position indicator showing "3 / 15" format |
| `SwipeBackEdgeShadow()` | 1002 | Edge shadow composable for swipe-back gesture |
| `Float.roundTo()` | 1047 | Private extension function for rounding Float to decimal places |

---

## Multipart Body Parser (MultipartBodyParser.kt)

**File:** `infra/parser/multipart/src/main/java/com/azikar24/wormaceptor/infra/parser/multipart/MultipartBodyParser.kt`

| Function | Line | Description |
|----------|------|-------------|
| `parseWithContentType()` | 89 | Public function for parsing multipart form data with explicit Content-Type header |

---

## Potentially Duplicate/Old Component Files

**Location:** `features/viewer/src/main/java/com/azikar24/wormaceptor/feature/viewer/ui/components/`

These files may have been replaced by newer versions in the `quickactions/` subdirectory:

| File | Status |
|------|--------|
| `SelectableTransactionItem.kt` | Possibly replaced by `quickactions/SelectableTransactionItem.kt` |
| `QuickFilterBar.kt` | Possibly replaced by `quickactions/QuickFilterBar.kt` |
| `TransactionContextMenu.kt` | Possibly replaced by `quickactions/TransactionContextMenu.kt` |

---

## Summary

| Category | Count |
|----------|-------|
| Unused modifier extensions | 6 |
| Unused swipe components | 2 |
| Unused gesture components | 3 |
| Unused parser functions | 1 |
| Potentially duplicate files | 3 |
| **Total** | **15** |

---

## Observations

1. **Pattern**: Many unused functions are alternative implementations or refactoring remnants where one approach was chosen and others remain
2. **Modifier extensions**: Long-press variants may have been experimental or replaced by other gesture handling
3. **Composable variants**: Multiple similar composables suggest evolutionary development where alternatives persist
4. **API surface**: `parseWithContentType()` suggests a public API that was superseded by the simpler `parse()` method

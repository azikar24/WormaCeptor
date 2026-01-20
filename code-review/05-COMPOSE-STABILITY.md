# Compose Stability Issues

This document lists Jetpack Compose stability and recomposition issues.

**Location:** `features/viewer/src/main/java/`

---

## Critical Issues

### 1. Missing @Stable/@Immutable on Data Classes

**File:** `ui/util/KeyboardShortcuts.kt:48-66`

**Problem:** Data classes used in Composables lack stability annotations.

**Bad Code:**
```kotlin
data class ShortcutKey(
    val key: Key,
    val ctrl: Boolean = false,
    val shift: Boolean = false,
    val alt: Boolean = false
)

data class KeyboardShortcutCallbacks(
    val onRefresh: () -> Unit = {},
    val onSearch: () -> Unit = {},
    val onSelectAll: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onClear: () -> Unit = {},
    val onExport: () -> Unit = {},
    val onCopyCurl: () -> Unit = {}
)
```

**Impact:** Every time `KeyboardShortcutCallbacks` is created, it triggers recomposition even if lambdas haven't changed.

**Fix:**
```kotlin
@Immutable
data class ShortcutKey(
    val key: Key,
    val ctrl: Boolean = false,
    val shift: Boolean = false,
    val alt: Boolean = false
)

@Stable
data class KeyboardShortcutCallbacks(
    val onRefresh: () -> Unit = {},
    val onSearch: () -> Unit = {},
    val onSelectAll: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onClear: () -> Unit = {},
    val onExport: () -> Unit = {},
    val onCopyCurl: () -> Unit = {}
)
```

---

### 2. Unstable Lambda Creation in remember Block

**File:** `ui/HomeScreen.kt:170-192`

**Problem:** Lambda captures create new instances on every recomposition. The remember keys include `selectedIds` (a Set), which changes frequently.

**Bad Code:**
```kotlin
val keyboardCallbacks = remember(isSelectionMode, selectedIds) {
    KeyboardShortcutCallbacks(
        onRefresh = onRefreshTransactions,
        onSearch = { showFilterSheet = true },
        onDelete = {
            if (isSelectionMode && selectedIds.isNotEmpty()) {
                showDeleteSelectedDialog = true
            }
        },
        // ...
    )
}
```

**Impact:** High recomposition frequency during selection mode.

**Fix:** Use size instead of entire set as key:
```kotlin
val keyboardCallbacks = remember(isSelectionMode, selectedIds.size) {
    KeyboardShortcutCallbacks(
        // ...
    )
}
```

---

### 3. Missing @Immutable on ComposeSyntaxColors

**File:** `ui/theme/SyntaxColors.kt:21-40`

**Problem:** Data class holds only immutable Color values but lacks annotation.

**Bad Code:**
```kotlin
data class ComposeSyntaxColors(
    val keyword: Color,
    val string: Color,
    val number: Color,
    // ... many more Color properties
)
```

**Impact:** Compose cannot prove stability, causing recomposition in syntax highlighting components.

**Fix:**
```kotlin
@Immutable
data class ComposeSyntaxColors(
    val keyword: Color,
    val string: Color,
    val number: Color,
    // ...
)
```

---

## Important Issues

### 4. Unstable List Parameter

**File:** `ui/HomeScreen.kt:87-88`

**Problem:** List parameters are unstable in Compose. Even if content is the same, List instance changes trigger recomposition.

**Bad Code:**
```kotlin
fun HomeScreen(
    transactions: List<TransactionSummary>,
    // ...
)
```

**Fix:** Use immutable collections:
```kotlin
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun HomeScreen(
    transactions: ImmutableList<TransactionSummary>,
    // ...
)

// In caller:
HomeScreen(
    transactions = transactions.toImmutableList(),
    // ...
)
```

---

### 5. Map Parameters Without Immutability

**File:** `ui/HomeScreen.kt:518-528`

**Problem:** Regular Maps are unstable.

**Bad Code:**
```kotlin
val methodCounts = remember(allTransactions) {
    allTransactions.groupBy { it.method }.mapValues { it.value.size }
}
val statusCounts = remember(allTransactions) {
    mapOf(
        200..299 to allTransactions.count { (it.code ?: 0) in 200..299 },
        // ...
    )
}
```

**Fix:**
```kotlin
import kotlinx.collections.immutable.toImmutableMap

val methodCounts = remember(allTransactions) {
    allTransactions.groupBy { it.method }
        .mapValues { it.value.size }
        .toImmutableMap()
}
val statusCounts = remember(allTransactions) {
    mapOf(/* ... */).toImmutableMap()
}
```

---

### 6. Missing derivedStateOf for Computed Values

**File:** `ui/HomeScreen.kt:230-235`

**Problem:** Values recomputed on every recomposition.

**Bad Code:**
```kotlin
val isFiltering = filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank()
val filterCount = listOfNotNull(
    filterMethod,
    filterStatusRange,
    searchQuery.takeIf { it.isNotBlank() }
).size
```

**Fix:**
```kotlin
val isFiltering by remember {
    derivedStateOf {
        filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank()
    }
}
val filterCount by remember {
    derivedStateOf {
        listOfNotNull(
            filterMethod,
            filterStatusRange,
            searchQuery.takeIf { it.isNotBlank() }
        ).size
    }
}
```

---

### 7. Unused rememberCoroutineScope

**File:** `ui/components/gesture/SwipeRefreshWrapper.kt:111`

**Problem:** `rememberCoroutineScope()` called but result never used.

**Bad Code:**
```kotlin
fun CustomSwipeRefreshWrapper(...) {
    rememberCoroutineScope()  // Unused
    var pullDistance by remember { mutableFloatStateOf(0f) }
}
```

**Fix:** Remove the unused line.

---

## Summary

| Issue | Severity | Impact |
|-------|----------|--------|
| Missing @Stable/@Immutable | Critical | Unnecessary recompositions |
| Unstable lambda in remember | Critical | Selection mode performance |
| Missing @Immutable on SyntaxColors | Critical | Syntax highlighting performance |
| Unstable List parameter | Important | HomeScreen recomposition |
| Unstable Map parameters | Important | Filter sheet performance |
| Missing derivedStateOf | Important | Unnecessary computation |
| Unused rememberCoroutineScope | Minor | Memory waste |

**Total Issues:** 7

## Required Dependency

To use immutable collections, add to `build.gradle.kts`:

```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
```

# Compose Stability Issues

This document lists Jetpack Compose stability and recomposition issues.

**Location:** `features/viewer/src/main/java/`

---

## Critical Issues
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

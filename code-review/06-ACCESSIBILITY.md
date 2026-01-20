# Accessibility Issues

This document lists accessibility issues found in the Jetpack Compose UI code.

**Location:** `features/viewer/src/main/java/`

---

## Critical Issues

### 1. Touch Target Too Small

**File:** `ui/HomeScreen.kt:451-463`

**Problem:** IconButton has explicit size of 32.dp, below the 48.dp minimum.

**Bad Code:**
```kotlin
IconButton(
    onClick = {
        onClearFilters()
        onSearchChanged("")
    },
    modifier = Modifier.size(32.dp)  // Too small!
) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Clear all filters",
        modifier = Modifier.size(20.dp)
    )
}
```

**Violation:** WCAG 2.5.5 Target Size - Touch targets should be at least 48x48 dp.

**Fix:**
```kotlin
IconButton(
    onClick = { /* ... */ },
    modifier = Modifier.size(48.dp)  // Minimum touch target
) {
    Icon(
        imageVector = Icons.Default.Close,
        contentDescription = "Clear all filters",
        modifier = Modifier.size(20.dp)  // Icon can stay smaller
    )
}
```

---

### 2. Missing Semantics for Custom Checkbox

**File:** `ui/components/quickactions/SelectableTransactionItem.kt:286-338`

**Problem:** Custom checkbox doesn't announce checked/unchecked state to screen readers.

**Bad Code:**
```kotlin
@Composable
fun SelectionCheckbox(
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Int = 24
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            // Missing semantics!
    )
}
```

**Violation:** WCAG 4.1.2 Name, Role, Value

**Fix:**
```kotlin
Box(
    modifier = modifier
        .size(size.dp)
        .semantics {
            role = Role.Checkbox
            stateDescription = if (isSelected) "Selected" else "Not selected"
        }
        .scale(scale)
        .clip(CircleShape)
        .background(backgroundColor)
)
```

---

### 3. Missing contentDescription on Filter Badge

**File:** `ui/HomeScreen.kt:237-252`

**Problem:** Badge count not announced by screen readers.

**Bad Code:**
```kotlin
BadgedBox(
    badge = {
        if (isFiltering) {
            Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.xs)
            ) {
                Text(text = filterCount.toString())
            }
        }
    }
)
```

**Violation:** WCAG 1.1.1 Non-text Content

**Fix:**
```kotlin
Badge(
    modifier = Modifier
        .padding(WormaCeptorDesignSystem.Spacing.xs)
        .semantics {
            contentDescription = "$filterCount filters active"
        }
)
```

---

### 4. Missing contentDescription on Multiple Icons

**Files:** Multiple locations

| File | Line | Icon |
|------|------|------|
| `ui/HomeScreen.kt` | 289 | Share icon |
| `ui/HomeScreen.kt` | 297 | DeleteSweep icon |
| `ui/HomeScreen.kt` | 306 | Share icon |
| `ui/HomeScreen.kt` | 314 | DeleteSweep icon |
| `ui/FilterBottomSheetContent.kt` | 247 | Search icon |
| `ui/FilterBottomSheetContent.kt` | 293 | Section header icons |
| `ui/components/ImagePreviewCard.kt` | 273, 514, 570 | Various icons |
| `ui/components/FullscreenImageViewer.kt` | 558, 591 | Metadata/action icons |
| `ui/TransactionDetailScreen.kt` | 843 | Section header icons |

**Bad Code:**
```kotlin
Icon(Icons.Default.Share, null)
Icon(Icons.Default.Search, contentDescription = null)
```

**Violation:** WCAG 1.1.1 Non-text Content

**Fix:**
```kotlin
Icon(Icons.Default.Share, contentDescription = "Share")
Icon(Icons.Default.Search, contentDescription = "Search")
```

---

### 5. Missing Semantics for Filter Chips

**File:** `ui/HomeScreen.kt:377-447`

**Problem:** Active filter chips don't announce their removable state.

**Bad Code:**
```kotlin
AssistChip(
    onClick = { onSearchChanged("") },
    label = { Text(text = "\"$searchQuery\"") },
    trailingIcon = {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            modifier = Modifier.size(16.dp)
        )
    }
)
```

**Violation:** WCAG 4.1.2 Name, Role, Value

**Fix:**
```kotlin
AssistChip(
    onClick = { onSearchChanged("") },
    modifier = Modifier.semantics {
        role = Role.Button
        stateDescription = "Active search filter: $searchQuery, tap to remove"
    },
    label = { Text(text = "\"$searchQuery\"") },
    // ...
)
```

---

### 6. Missing State Announcement for Filter Cards

**File:** `ui/FilterBottomSheetContent.kt:398-529`

**Problem:** GridFilterCard doesn't announce selected/unselected state changes.

**Bad Code:**
```kotlin
Box(
    modifier = modifier
        .clickable(onClick = onClick, enabled = count > 0)
        // Missing state semantics
        .padding(WormaCeptorDesignSystem.Spacing.md)
)
```

**Violation:** WCAG 4.1.2 Name, Role, Value

**Fix:**
```kotlin
Box(
    modifier = modifier
        .clickable(onClick = onClick, enabled = count > 0)
        .semantics {
            role = Role.Checkbox
            stateDescription = if (isSelected) "Selected" else "Not selected"
        }
        .padding(WormaCeptorDesignSystem.Spacing.md)
)
```

---

### 7. Missing Heading Semantics for Section Headers

**File:** `ui/MetricsCard.kt:408-428`

**Problem:** Section headers don't use heading semantics for navigation.

**Bad Code:**
```kotlin
@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(...) {
        Icon(imageVector = icon, contentDescription = null, ...)
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            // Missing heading semantics
        )
    }
}
```

**Violation:** WCAG 1.3.1 Info and Relationships

**Fix:**
```kotlin
Text(
    text = title,
    style = MaterialTheme.typography.titleSmall,
    modifier = Modifier.semantics { heading() }
)
```

---

## Summary

| Issue | WCAG | Count |
|-------|------|-------|
| Missing contentDescription | 1.1.1 | 15+ icons |
| Missing state semantics | 4.1.2 | 3 components |
| Missing heading semantics | 1.3.1 | 1 component |
| Touch target too small | 2.5.5 | 1 button |
| **Total** | | **20+** |

## Priority Recommendations

1. **Immediate:** Fix touch target size (impacts motor-impaired users)
2. **High:** Add semantics to custom checkbox (critical for selection mode)
3. **High:** Add contentDescription to all interactive icons
4. **Medium:** Add state announcements for toggles/filters
5. **Low:** Add heading semantics for navigation

## Testing Tools

- **TalkBack** - Android's built-in screen reader
- **Accessibility Scanner** - Google's accessibility testing app
- **Compose UI Testing** - `assertContentDescriptionEquals()`, `assertIsSelected()`

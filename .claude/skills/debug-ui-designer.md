---
name: debug-ui-designer
description: Design debugging tool interfaces following WormaCeptor's design system, UX patterns, and responsive layouts. Use when designing new tool UIs, improving screen layouts, ensuring accessibility, or adapting for different screen sizes.
---

# Debug UI Designer

Design responsive, accessible debugging tool interfaces for WormaCeptor.

## When to Use

- Designing a new tool's UI
- Improving existing screen layouts
- Creating consistent component patterns
- Reviewing UI for accessibility compliance
- Adapting layouts for different screen sizes

## Design System Reference

### Spacing Scale (4dp Baseline Grid)

```kotlin
object Spacing {
    val xxs = 2.dp    // Micro gaps
    val xs = 4.dp     // Tight spacing
    val sm = 8.dp     // Small gaps
    val md = 12.dp    // Default spacing
    val lg = 16.dp    // Section gaps
    val xl = 24.dp    // Large sections
    val xxl = 32.dp   // Page margins
    val xxxl = 48.dp  // Hero spacing
}
```

### Corner Radius

```kotlin
object CornerRadius {
    val xs = 4.dp     // Subtle rounding
    val sm = 8.dp     // Cards, inputs
    val md = 12.dp    // Dialogs
    val lg = 16.dp    // Large cards
    val xl = 24.dp    // Prominent elements
    val pill = 999.dp // Fully rounded
}
```

### Elevation

```kotlin
object Elevation {
    val none = 0.dp   // Flat
    val xs = 1.dp     // Subtle lift
    val sm = 2.dp     // Cards
    val md = 4.dp     // Raised elements
    val lg = 6.dp     // Floating actions
}
```

### Alpha Values

```kotlin
object Alpha {
    val subtle = 0.08f   // Barely visible overlays
    val light = 0.12f    // Light tints
    val medium = 0.20f   // Medium overlays
    val strong = 0.40f   // Strong overlays
    val intense = 0.60f  // Heavy overlays
}
```

### Animation Durations

```kotlin
object Animation {
    val fast = 150    // Quick feedback
    val normal = 250  // Standard transitions
    val slow = 350    // Emphasis animations
}
```

## Semantic Colors

```kotlin
// Status indicators
StatusGreen   // Success, healthy, enabled
StatusAmber   // Warning, pending, caution
StatusRed     // Error, failure, critical
StatusBlue    // Info, in-progress, selected
StatusGrey    // Disabled, inactive, neutral

// Usage
Text(
    text = "200 OK",
    color = StatusGreen
)

Text(
    text = "500 Error",
    color = StatusRed
)
```

## Responsive Design Patterns

### Window Size Classes

```kotlin
@Composable
fun AdaptiveScreen() {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> {
            // Phone portrait: single column, stacked navigation
            CompactLayout()
        }
        WindowWidthSizeClass.MEDIUM -> {
            // Tablet portrait, phone landscape: optional rail
            MediumLayout()
        }
        WindowWidthSizeClass.EXPANDED -> {
            // Tablet landscape, desktop: list-detail, persistent rail
            ExpandedLayout()
        }
    }
}
```

### Adaptive List-Detail

```kotlin
@Composable
fun TransactionListDetail(
    transactions: ImmutableList<Transaction>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (maxWidth >= 840.dp) {
            // Side-by-side on tablets
            Row(Modifier.fillMaxSize()) {
                TransactionList(
                    transactions = transactions,
                    selectedId = selectedId,
                    onSelect = onSelect,
                    modifier = Modifier.width(360.dp)
                )
                VerticalDivider()
                TransactionDetail(
                    transactionId = selectedId,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Stacked on phones - use navigation
            if (selectedId != null) {
                TransactionDetail(transactionId = selectedId)
            } else {
                TransactionList(
                    transactions = transactions,
                    onSelect = onSelect
                )
            }
        }
    }
}
```

### Flexible Grid

```kotlin
@Composable
fun ToolsGrid(
    tools: ImmutableList<Tool>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val columns = when {
            maxWidth >= 1200.dp -> 4
            maxWidth >= 840.dp -> 3
            maxWidth >= 600.dp -> 2
            else -> 1
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            items(tools) { tool ->
                ToolCard(tool = tool)
            }
        }
    }
}
```

### Orientation Handling

```kotlin
@Composable
fun PerformanceOverview() {
    val configuration = LocalConfiguration.current

    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        // Landscape: metrics side by side
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FpsMetric(Modifier.weight(1f))
            MemoryMetric(Modifier.weight(1f))
            CpuMetric(Modifier.weight(1f))
        }
    } else {
        // Portrait: metrics stacked
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            FpsMetric()
            MemoryMetric()
            CpuMetric()
        }
    }
}
```

### Safe Area Handling

```kotlin
@Composable
fun SafeScreen(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        content()
    }
}

// Or with scaffold
Scaffold(
    contentWindowInsets = WindowInsets.systemBars
) { paddingValues ->
    Content(Modifier.padding(paddingValues))
}
```

### Foldable Support

```kotlin
@Composable
fun FoldableAwareLayout() {
    val windowLayoutInfo = currentWindowLayoutInfo()
    val foldingFeature = windowLayoutInfo.displayFeatures
        .filterIsInstance<FoldingFeature>()
        .firstOrNull()

    when {
        foldingFeature?.state == FoldingFeature.State.HALF_OPENED -> {
            // Table-top mode: content above fold, controls below
            TableTopLayout()
        }
        foldingFeature?.orientation == FoldingFeature.Orientation.VERTICAL -> {
            // Book mode: dual pane
            BookLayout()
        }
        else -> {
            // Normal layout
            DefaultLayout()
        }
    }
}
```

## Debugging UX Patterns

### Dense Information Display

```kotlin
@Composable
fun TransactionRow(
    transaction: Transaction,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column {
        // Always visible: key info
        Row(
            modifier = Modifier
                .clickable(onClick = onToggle)
                .padding(Spacing.sm)
        ) {
            StatusIndicator(transaction.responseCode)
            Spacer(Modifier.width(Spacing.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    text = transaction.method + " " + transaction.path,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transaction.host,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${transaction.duration}ms",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Expandable: full details
        AnimatedVisibility(visible = isExpanded) {
            TransactionDetails(transaction)
        }
    }
}
```

### Search and Filter First-Class

```kotlin
@Composable
fun SearchableList(
    items: ImmutableList<Item>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    activeFilters: ImmutableSet<Filter>,
    onFilterToggle: (Filter) -> Unit
) {
    Column {
        // Persistent search bar
        SearchBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.sm)
        )

        // Filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            items(Filter.entries) { filter ->
                FilterChip(
                    selected = filter in activeFilters,
                    onClick = { onFilterToggle(filter) },
                    label = { Text(filter.label) }
                )
            }
        }

        // Results
        LazyColumn {
            items(items) { item ->
                ItemRow(item)
            }
        }
    }
}
```

### Bulk Actions

```kotlin
@Composable
fun SelectableList(
    items: ImmutableList<Item>,
    selectedIds: ImmutableSet<String>,
    onSelectionChange: (ImmutableSet<String>) -> Unit,
    onBulkAction: (BulkAction) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn {
            items(items, key = { it.id }) { item ->
                SelectableRow(
                    item = item,
                    isSelected = item.id in selectedIds,
                    onToggle = {
                        val newSelection = if (item.id in selectedIds) {
                            selectedIds - item.id
                        } else {
                            selectedIds + item.id
                        }
                        onSelectionChange(newSelection.toImmutableSet())
                    }
                )
            }
        }

        // Floating bulk action bar
        AnimatedVisibility(
            visible = selectedIds.isNotEmpty(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BulkActionBar(
                selectedCount = selectedIds.size,
                onDelete = { onBulkAction(BulkAction.Delete) },
                onShare = { onBulkAction(BulkAction.Share) },
                onClear = { onSelectionChange(persistentSetOf()) }
            )
        }
    }
}
```

### Copy/Share/Export Actions

```kotlin
@Composable
fun ActionableContent(
    content: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onExport: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, "Copy")
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, "Share")
            }
            IconButton(onClick = onExport) {
                Icon(Icons.Default.Download, "Export")
            }
        }

        SelectionContainer {
            Text(text = content)
        }
    }
}
```

## Accessibility Requirements

### WCAG 2.1 AA Compliance

```kotlin
// Minimum touch target: 48x48dp
IconButton(
    onClick = { },
    modifier = Modifier.size(48.dp)  // Never smaller
) {
    Icon(Icons.Default.Close, contentDescription = "Close")
}

// Contrast ratio: 4.5:1 minimum
Text(
    text = "Important",
    color = MaterialTheme.colorScheme.onSurface  // Verified contrast
)

// Content descriptions for all icons
Icon(
    imageVector = Icons.Default.Error,
    contentDescription = "Error: Request failed"  // Never null for meaningful icons
)
```

### Semantic Modifiers

```kotlin
Row(
    modifier = Modifier
        .selectable(
            selected = isSelected,
            onClick = onSelect,
            role = Role.Tab
        )
        .semantics {
            selected = isSelected
            stateDescription = if (isSelected) "Selected" else "Not selected"
        }
) {
    // Content
}
```

## Existing Components Reference

Reuse components from `features/viewer/ui/components/`:

| Component | Use For |
|-----------|---------|
| `JsonTreeView` | Collapsible JSON with syntax highlighting |
| `XmlTreeView` | Collapsible XML with syntax highlighting |
| `PaginatedBodyView` | Large content with pagination |
| `HighlightedText` | Search term highlighting |
| `BulkActionBar` | Multi-select action toolbar |
| `FullscreenImageViewer` | Pinch-zoom image viewer |
| `SearchBar` | Expandable search input |
| `FilterChip` | Selectable filter tag |

## Design Checklist

Before finalizing any UI:

- [ ] Uses design system tokens (no hardcoded dp/colors)
- [ ] Follows 4dp baseline grid
- [ ] Touch targets >= 48x48dp
- [ ] Contrast ratio >= 4.5:1
- [ ] Content descriptions on all icons
- [ ] Responsive: Compact, Medium, Expanded layouts
- [ ] Handles system bars properly (WindowInsets)
- [ ] Search/filter accessible within one tap
- [ ] Expandable details for dense info
- [ ] Copy/share actions available
- [ ] Loading and empty states designed
- [ ] Error states with recovery actions

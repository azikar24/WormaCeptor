# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WormaCeptor V2 is a modular Android debugging toolkit and network inspector. It uses Clean Architecture with 50+ modules, designed for production safety via physical dependency separation (debugImplementation).

## Build Commands

```bash
./gradlew build                    # Full build
./gradlew :app:installDebug        # Run demo app
./gradlew spotlessApply            # Format code (ktlint)
./gradlew spotlessCheck            # Check formatting
./gradlew detekt                   # Static analysis
./gradlew lint                     # Android Lint
./gradlew :test:architecture:test  # Architecture boundary tests
```

## Architecture

### Module Layers

| Layer | Purpose | Key Constraint |
|-------|---------|----------------|
| `api/` | Public entry points for host apps | No implementation logic |
| `core/engine` | Business logic, monitoring engines | No UI, no Android framework |
| `domain/entities` | Data models (Transaction, Request, Response) | Zero dependencies |
| `domain/contracts` | Interfaces for infra implementations | Zero dependencies |
| `features/` | Jetpack Compose UI screens (31 modules) | Only depends on Core, Domain |
| `infra/` | Concrete implementations (persistence, parsers, syntax) | Swappable, Core doesn't know these exist |
| `platform/android` | Android-specific utilities (notifications, shake) | Isolated from Core |

### Dependency Rules

- **Features cannot depend on Infra** - enforced by ArchUnit tests in `:test:architecture`
- **Core cannot depend on Android Framework**
- Use `implementation` for everything; `api` only for shared contracts
- `internal` modifier by default for non-public classes

### Host App Integration

```kotlin
// Required
implementation("com.github.azikar24.WormaCeptor:api-client:VERSION")

// Debug only (choose one)
debugImplementation("com.github.azikar24.WormaCeptor:api-impl-persistence:VERSION")  // Room-based
debugImplementation("com.github.azikar24.WormaCeptor:api-impl-imdb:VERSION")          // In-memory
```

## Tech Stack

- Kotlin 2.0.21, Min SDK 23, Target SDK 36
- Jetpack Compose (BOM 2024.10.01)
- Room 2.6.1, OkHttp 4.12.0, Koin 4.0.0
- Coroutines 1.8.1

## Code Style

- Standard Kotlin conventions with Jetpack Compose
- All new UI must use Compose
- Prefix core classes with `WormaCeptor`
- 4px baseline grid for UI spacing
- WCAG 2.1 AA accessibility (4.5:1 contrast, 48x48dp touch targets)

## Design Principles

- Optimize for modularity and long-term maintainability over speed
- Never let debug(WormaCeptor plugin/library) code leak into release builds
- If a feature adds a heavy library, create a new module
- No generic `utils` folders - context-specific modules only

## Key Directories

- `config/detekt/` - Detekt static analysis config
- `docs/architecture/` - Architecture documentation
- `docs/reference/` - Feature inventory and technical docs

## Adding New Features

1. Create module in `features/` directory
2. Depend only on `core:engine` and `domain:*`
3. Never access infra modules directly
4. Add to `settings.gradle.kts`

## Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Core classes | `WormaCeptor` prefix | `WormaCeptorCollector`, `WormaCeptorInterceptor` |
| Engines | `Engine` suffix | `TransactionEngine`, `CrashEngine` |
| ViewModels | `ViewModel` suffix | `TransactionListViewModel` |
| Repositories | `Repository` suffix | `TransactionRepository` |
| Feature singletons | Object with `Feature` suffix | `TransactionFeature` |
| Packages | `com.azikar24.wormaceptor.feature.{name}` | `com.azikar24.wormaceptor.feature.transactions` |

## Feature Module Structure

```
features/{name}/
├── {Name}Feature.kt          # Entry point singleton
├── data/
│   ├── {Name}DataSource.kt
│   └── {Name}RepositoryImpl.kt
├── ui/
│   ├── {Name}Screen.kt
│   └── theme/{Name}DesignSystem.kt
└── vm/
    └── {Name}ViewModel.kt
```

## Compose UI Patterns

### Parameter Ordering
```kotlin
@Composable
fun MyComponent(
    state: MyState,              // State first
    onAction: (Action) -> Unit,  // Callbacks second
    modifier: Modifier = Modifier // Modifier last
)
```

### Collections
Use `ImmutableList`/`ImmutableSet` from `kotlinx.collections.immutable` for Compose state to prevent unnecessary recompositions.

### Design System Objects
```kotlin
object MyFeatureDesignSystem {
    object Spacing {
        val Small = 4.dp
        val Medium = 8.dp
        val Large = 16.dp
    }
    object Colors { ... }
    object CornerRadius { ... }
}
```

## ViewModel Patterns

```kotlin
class MyViewModel(
    private val repository: MyRepository
) : ViewModel() {

    // Private mutable state with underscore prefix
    private val _searchQuery = MutableStateFlow("")

    // Public immutable state
    val items: StateFlow<List<Item>> = combine(
        repository.items,
        _searchQuery
    ) { items, query ->
        items.filter { it.matches(query) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Factory for DI
    class Factory(private val repository: MyRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MyViewModel(repository) as T
        }
    }
}
```

## Anti-Patterns to Avoid

| Anti-Pattern | Why | Do Instead |
|--------------|-----|------------|
| `runBlocking` | Causes ANR on main thread | Use `suspend` functions or `viewModelScope.launch` |
| `!!` chains | Crashes at runtime | Use `?.let {}`, `?:`, or require non-null at boundaries |
| Global mutable singletons | Race conditions, testing nightmare | Use Koin scoped instances |
| Bare `try { } catch (e: Exception)` | Hides bugs, catches cancellation | Catch specific exceptions |
| Manual resource management | Leaks | Use `.use {}` for Closeable |
| `Thread.sleep()` | Blocks thread | Use `delay()` in coroutines |

## Detekt Rules Summary

Key thresholds from `config/detekt/detekt.yml`:

- **Max method length**: 60 lines
- **Max function parameters**: 6 (8 for constructors)
- **Max nested depth**: 4 blocks
- **Magic numbers**: Only -1, 0, 1, 2 allowed without constants
- **Forbidden comments**: No FIXME/STOPSHIP/TODO in commits

Run `./gradlew detekt` before committing.

## Koin DI Pattern

```kotlin
// In engineModule
val engineModule = module {
    single { TransactionEngine(get(), get()) }
    single { CrashEngine(get()) }
}

// WormaCeptorKoin handles dual-instance strategy:
// - Uses host app's Koin if available
// - Falls back to own Koin instance if not
```

Engines are registered as singletons. Features use `koinViewModel()` for ViewModels.

## Deep Links

| Route | Destination |
|-------|-------------|
| `wormaceptor://tools` | Tools screen |
| `wormaceptor://tools/{feature}` | Specific tool |
| `wormaceptor://crashes` | Crash list |
| `wormaceptor://transactions` | Network transactions |

## Further Reading

- `docs/architecture/` - Detailed architecture decisions
- `docs/reference/` - Feature inventory and API docs
- `config/detekt/detekt.yml` - Full static analysis rules

## Testing Setup

| Library | Version | Purpose |
|---------|---------|---------|
| JUnit | 4.13.2 | Unit test framework |
| MockK | 1.13.12 | Kotlin mocking |
| kotlinx-coroutines-test | 1.8.1 | Coroutine testing |
| ArchUnit | - | Architecture boundary enforcement |

**Test Commands:**
```bash
./gradlew test                     # All unit tests
./gradlew :test:architecture:test  # Architecture boundary tests
./gradlew connectedAndroidTest     # Instrumentation tests
```

**File Locations:**
- Unit tests: `src/test/java/`
- Instrumentation tests: `src/androidTest/java/`
- Architecture tests: `test/architecture/`

Note: Detekt excludes test directories from naming rules.

## Error Handling Patterns

| Pattern | Usage | Example |
|---------|-------|---------|
| Silent failure with logging | Non-critical operations | `Log.w(TAG, "Parse failed", e)` |
| `isValid` flag | Parsing results | `ParsedBody.isValid` instead of Result/Either |
| Domain entities for errors | Crash/error tracking | `Crash`, `LeakInfo`, `ThreadViolation` |
| Specific exception catching | Try-catch blocks | `catch (e: JsonSyntaxException)` not `catch (e: Exception)` |

**Do NOT:**
- Throw exceptions up the call stack for expected failures
- Create custom exception types - use domain entities
- Catch generic `Exception` (hides bugs, catches cancellation)

## Design System Tokens

Reference: `features/viewer/ui/theme/DesignSystem.kt` - `WormaCeptorDesignSystem` object

### Spacing Scale

| Token | Value | Usage |
|-------|-------|-------|
| `xxs` | 2.dp | Micro gaps |
| `xs` | 4.dp | Tight spacing |
| `sm` | 8.dp | Small gaps |
| `md` | 12.dp | Default spacing |
| `lg` | 16.dp | Section gaps |
| `xl` | 24.dp | Large sections |
| `xxl` | 32.dp | Page margins |
| `xxxl` | 48.dp | Hero spacing |

### Corner Radius

| Token | Value |
|-------|-------|
| `xs` | 4.dp |
| `sm` | 8.dp |
| `md` | 12.dp |
| `lg` | 16.dp |
| `xl` | 24.dp |
| `pill` | 999.dp |

### Elevation

| Token | Value |
|-------|-------|
| `none` | 0.dp |
| `xs` | 1.dp |
| `sm` | 2.dp |
| `md` | 4.dp |
| `lg` | 6.dp |

### Alpha Values

| Token | Value | Usage |
|-------|-------|-------|
| `subtle` | 0.08f | Barely visible overlays |
| `light` | 0.12f | Light tints |
| `medium` | 0.20f | Medium overlays |
| `strong` | 0.40f | Strong overlays |
| `intense` | 0.60f | Heavy overlays |

### Animation Duration

| Token | Value |
|-------|-------|
| `fast` | 150ms |
| `normal` | 250ms |
| `slow` | 350ms |

## Semantic Colors

Reference: `features/viewer/ui/theme/Color.kt`

| Color | Purpose |
|-------|---------|
| `StatusGreen` | Success, healthy, enabled |
| `StatusAmber` | Warning, pending, caution |
| `StatusRed` | Error, failure, critical |
| `StatusBlue` | Info, in-progress, selected |
| `StatusGrey` | Disabled, inactive, neutral |

**Category Colors** for tool grouping (Network, Storage, Performance, Debugging, UI).

**Material You:** Dynamic colors supported on Android 12+ via `dynamicLightColorScheme()` / `dynamicDarkColorScheme()`.

## Common UI Components

Reference: `features/viewer/ui/components/`

| Component | Purpose | Location |
|-----------|---------|----------|
| `JsonTreeView` | Collapsible JSON with syntax highlighting | `components/json/` |
| `XmlTreeView` | Collapsible XML with syntax highlighting | `components/xml/` |
| `PaginatedBodyView` | Large content pagination | `components/body/` |
| `HighlightedText` | Search term highlighting | `components/` |
| `BulkActionBar` | Multi-select action toolbar | `components/` |
| `FullscreenImageViewer` | Pinch-zoom image viewer | `components/media/` |
| `PdfViewerScreen` | PDF rendering | `components/media/` |
| `SearchBar` | Expandable search input | `components/` |
| `FilterChip` | Selectable filter tag | `components/` |

## Navigation Patterns

### Tab Navigation
- `HorizontalPager` with 3 tabs: Transactions, Crashes, Tools
- State observed via `snapshotFlow { pagerState.currentPage }`
- Haptic feedback on tab change via `LocalHapticFeedback`

### Deep Links
Handled by `DeepLinkHandler` sealed class:
```kotlin
sealed class DeepLinkHandler {
    data object Tools : DeepLinkHandler()
    data class ToolDetail(val route: String) : DeepLinkHandler()
    data object Crashes : DeepLinkHandler()
    data object Transactions : DeepLinkHandler()
}
```

### Tool Routes
String-based identifiers: `"memory"`, `"fps"`, `"database"`, `"shared_prefs"`, `"leak_canary"`, etc.

### Bottom Sheets
Use `ModalBottomSheet` for filters and secondary actions.

## Compose State Patterns

Beyond ViewModel state management:

| Pattern | Usage | Example |
|---------|-------|---------|
| `remember { mutableStateOf() }` | UI-only state (expanded, selected) | `var isExpanded by remember { mutableStateOf(false) }` |
| `LaunchedEffect(key)` | Side effects on state change | `LaunchedEffect(deepLink) { handleDeepLink(it) }` |
| `snapshotFlow { }` | Convert Compose state to Flow | `snapshotFlow { pagerState.currentPage }` |
| `derivedStateOf { }` | Computed values from state | `val hasSelection by derivedStateOf { selected.isNotEmpty() }` |
| `rememberSaveable` | Survive config changes | `var query by rememberSaveable { mutableStateOf("") }` |

### Haptic Feedback
```kotlin
val haptic = LocalHapticFeedback.current
haptic.performHapticFeedback(HapticFeedbackType.LongPress)
```

## Accessibility Patterns

- **WCAG 2.1 AA compliance**: 4.5:1 contrast ratio, 48x48dp minimum touch targets
- **Semantics modifiers**: `Modifier.semantics { role = Role.Button; selected = isSelected }`
- **Content descriptions**: Required for all icons and images
- **Edge-to-edge**: Use `WindowInsets` for proper padding under system bars

```kotlin
Modifier
    .fillMaxSize()
    .padding(WindowInsets.systemBars.asPaddingValues())
```

## Resource Conventions

### String Resources
- All user-visible strings in `values/strings.xml`
- Access via `stringResource(R.string.feature_title)`
- Currently English only - no i18n infrastructure

### Drawable Resources
- Prefer vector drawables (`ic_*.xml`)
- Material Icons via `Icons.Default.*` or `Icons.Outlined.*`

### Dimension Resources
- Prefer design system tokens over dimension resources
- Use `WormaCeptorDesignSystem.Spacing.*` instead of `@dimen/`

## Code Review and Plans

- Save code reviews and plans to `.claude/` folder with descriptive names
- Examples: `.claude/review-toast-migration.md`, `.claude/plan-auth-refactor.md`

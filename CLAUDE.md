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

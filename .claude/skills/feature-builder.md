---
name: feature-builder
description: Scaffold and implement new WormaCeptor feature modules following strict patterns. Use when creating new debugging tools, inspectors, or extending existing features with new screens.
---

# Feature Builder

Scaffold and implement new feature modules following WormaCeptor's architecture.

## When to Use

- Creating a new debugging tool (e.g., "add a battery monitor")
- Adding a new inspector (e.g., "add a ContentProvider browser")
- Extending existing features with new screens

## Process

### 1. Gather Requirements

Ask the user:
- What is the feature name? (e.g., "battery", "content-provider")
- What data does it display/monitor?
- Does it need a background engine in `core/engine`?
- Does it need new domain entities?

### 2. Create Module Structure

Generate the standard structure in `features/{name}/`:

```
features/{name}/
├── build.gradle.kts
├── src/main/java/com/azikar24/wormaceptor/feature/{name}/
│   ├── {Name}Feature.kt              # Entry point singleton
│   ├── di/{Name}Module.kt            # Koin DI registration
│   ├── vm/{Name}ViewModel.kt         # State management
│   ├── ui/
│   │   ├── {Name}Screen.kt           # Main Compose UI
│   │   └── components/               # Feature-specific components
│   └── theme/{Name}DesignSystem.kt   # Design tokens (if needed)
└── src/main/AndroidManifest.xml
```

### 3. Build Configuration

```kotlin
// build.gradle.kts
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.azikar24.wormaceptor.feature.{name}"
    // Standard config from other feature modules
}

dependencies {
    implementation(project(":core:engine"))
    implementation(project(":domain:entities"))
    implementation(project(":domain:contracts"))
    implementation(project(":core:ui"))

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    // Koin
    implementation(libs.koin.compose)

    // Collections
    implementation(libs.kotlinx.collections.immutable)
}
```

### 4. Feature Entry Point

```kotlin
// {Name}Feature.kt
object {Name}Feature {
    const val ROUTE = "{name}"

    fun initialize() {
        // Register with Koin if needed
    }
}
```

### 5. ViewModel Pattern

```kotlin
class {Name}ViewModel(
    private val repository: {Name}Repository  // or Engine
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val state: StateFlow<{Name}State> = combine(
        repository.data,
        _searchQuery
    ) { data, query ->
        {Name}State(
            items = data.filter { it.matches(query) }.toImmutableList(),
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = {Name}State()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    class Factory(private val repository: {Name}Repository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return {Name}ViewModel(repository) as T
        }
    }
}

data class {Name}State(
    val items: ImmutableList<{Item}> = persistentListOf(),
    val searchQuery: String = ""
)
```

### 6. Compose Screen Pattern

```kotlin
@Composable
fun {Name}Screen(
    state: {Name}State,                    // State first
    onAction: ({Name}Action) -> Unit,      // Callbacks second
    modifier: Modifier = Modifier          // Modifier last
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        // Search bar if needed
        // Content list/grid
    }
}

sealed interface {Name}Action {
    data class Search(val query: String) : {Name}Action
    data class Select(val item: {Item}) : {Name}Action
}
```

### 7. Register in settings.gradle.kts

Add to the features section:
```kotlin
include(":features:{name}")
```

### 8. Wire to Tools Navigation

Update the tools screen to include the new feature route.

## Guardrails

**NEVER:**
- Create dependencies on `infra/` modules from features
- Use `implementation` for anything that should be `internal`
- Hardcode spacing/colors - use design system tokens
- Use mutable collections in Compose state
- Exceed Detekt limits: 60 line methods, 6 params, 4 nesting depth

**ALWAYS:**
- Use `ImmutableList`/`ImmutableSet` from kotlinx.collections.immutable
- Follow parameter ordering: state, callbacks, modifier
- Prefix core classes with `WormaCeptor`
- Use `Engine` suffix for background monitors
- Run `./gradlew detekt` before completing

## Reference Modules

Look at these for patterns:
- `features/fps` - Simple monitor with overlay
- `features/memory` - Monitor with thresholds
- `features/viewer` - Complex feature with tabs
- `features/database` - Inspector with query capability

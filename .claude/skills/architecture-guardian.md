---
name: architecture-guardian
description: Validate WormaCeptor architecture boundaries and conventions. Use before creating PRs, after cross-module changes, when unsure if a dependency is allowed, or when reviewing contributor code.
---

# Architecture Guardian

Validate architecture boundaries and project conventions for WormaCeptor.

## When to Use

- Before creating a PR
- After making cross-module changes
- When unsure if a dependency is allowed
- Reviewing contributor PRs
- After refactoring

## Validation Checklist

### 1. Layer Dependencies

Run and interpret: `./gradlew :test:architecture:test`

**Allowed dependencies:**

| Layer | Can Depend On |
|-------|---------------|
| `features/*` | `core:engine`, `core:ui`, `domain:entities`, `domain:contracts` |
| `core:engine` | `domain:entities`, `domain:contracts` |
| `core:ui` | `domain:entities`, Compose libraries |
| `domain:entities` | Nothing (zero dependencies) |
| `domain:contracts` | `domain:entities` only |
| `infra/*` | `domain:contracts`, `domain:entities`, implementation libraries |
| `api:client` | `domain:entities`, `domain:contracts` |
| `api:impl:*` | `core:engine`, `infra/*`, `domain:*` |
| `platform:android` | Android framework, `domain:*` |

**VIOLATIONS to flag:**
- Features importing from `infra/*`
- Core importing Android framework classes
- Domain importing anything internal
- Circular dependencies between modules

### 2. Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Core classes | `WormaCeptor` prefix | `WormaCeptorCollector` |
| Engines | `Engine` suffix | `FpsMonitorEngine` |
| ViewModels | `ViewModel` suffix | `TransactionListViewModel` |
| Repositories | `Repository` suffix | `TransactionRepository` |
| Feature singletons | `Feature` suffix | `TransactionFeature` |
| Packages | `com.azikar24.wormaceptor.feature.{name}` | - |

### 3. Anti-Pattern Detection

Search for and flag:

```kotlin
// FORBIDDEN - causes ANR
runBlocking { }

// FORBIDDEN - runtime crashes
value!!.property!!.method()

// FORBIDDEN - hides bugs, catches cancellation
try { } catch (e: Exception) { }

// FORBIDDEN - race conditions
object Singleton { var mutableState = ... }

// FORBIDDEN - blocks thread
Thread.sleep(1000)

// FORBIDDEN - leaks resources
val stream = FileInputStream(file)
// ... without .use { }
```

**Replacements:**
- `runBlocking` → `suspend` functions or `viewModelScope.launch`
- `!!` chains → `?.let {}`, `?:`, or `require()` at boundaries
- Bare `Exception` → Catch specific types: `JsonSyntaxException`, `IOException`
- Global mutable → Koin scoped instances
- `Thread.sleep()` → `delay()` in coroutines
- Manual streams → `.use { }` for Closeable

### 4. Compose Patterns

**Parameter ordering:**
```kotlin
// CORRECT
@Composable
fun MyScreen(
    state: MyState,              // 1. State
    onAction: (Action) -> Unit,  // 2. Callbacks
    modifier: Modifier = Modifier // 3. Modifier last
)

// WRONG
fun MyScreen(
    modifier: Modifier = Modifier,
    state: MyState,
    onAction: (Action) -> Unit
)
```

**Collections in state:**
```kotlin
// CORRECT - prevents recomposition
val items: ImmutableList<Item>

// WRONG - causes unnecessary recomposition
val items: List<Item>
```

**StateFlow pattern:**
```kotlin
// CORRECT
.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyState
)

// WRONG - never stops collecting
started = SharingStarted.Eagerly
```

### 5. Detekt Thresholds

Run: `./gradlew detekt`

| Rule | Limit |
|------|-------|
| Method length | 60 lines max |
| Function parameters | 6 max (8 for constructors) |
| Nesting depth | 4 blocks max |
| Magic numbers | Only -1, 0, 1, 2 allowed |
| Forbidden comments | No FIXME/STOPSHIP/TODO |

### 6. Visibility Modifiers

```kotlin
// DEFAULT - use internal for non-public
internal class FeatureImpl

// ONLY when needed by other modules
class PublicApi

// NEVER expose implementation details
// internal class should not leak through public API
```

### 7. Import Rules

```kotlin
// FORBIDDEN - wildcard imports
import com.example.*

// CORRECT - explicit imports
import com.example.SpecificClass
```

## Output Format

Generate a report:

```
## Architecture Validation Report

### Layer Violations
- [FAIL] features/cookies depends on infra/persistence (CookiesRepository.kt:15)
- [PASS] No core → Android framework dependencies

### Naming Violations
- [FAIL] MyHelper.kt should be WormaCeptorMyHelper or more specific name
- [PASS] All ViewModels have correct suffix

### Anti-Patterns Found
- [FAIL] runBlocking in TransactionEngine.kt:42
- [FAIL] Bare Exception catch in Parser.kt:28

### Compose Issues
- [WARN] Mutable List in state: SearchState.kt:12
- [FAIL] Wrong parameter order: MyScreen.kt:8

### Detekt Issues
- [FAIL] Method too long: processData() is 75 lines (max 60)

### Summary
- 4 failures, 1 warning
- Must fix before merge
```

## Commands to Run

```bash
# Full validation suite
./gradlew detekt spotlessCheck lint :test:architecture:test

# Quick check
./gradlew detekt :test:architecture:test

# Format code
./gradlew spotlessApply
```

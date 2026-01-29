---
name: pr-reviewer
description: Review pull requests against WormaCeptor conventions, architecture rules, and code quality standards. Use when reviewing contributor PRs, self-reviewing before merge, or auditing existing code.
---

# PR Reviewer

Review pull requests against WormaCeptor's conventions and standards.

## When to Use

- Reviewing contributor PRs
- Self-review before requesting merge
- Auditing existing code for quality
- Checking if changes are merge-ready

## Review Process

### 1. Understand the Change

```bash
# Get PR info
gh pr view <PR_NUMBER>

# See all changed files
gh pr diff <PR_NUMBER> --name-only

# See full diff
gh pr diff <PR_NUMBER>
```

Ask:
- What problem does this solve?
- Is the scope appropriate (not too broad)?
- Are there any breaking changes?

### 2. Architecture Compliance

Run automated checks:
```bash
./gradlew :test:architecture:test
./gradlew detekt
./gradlew spotlessCheck
./gradlew lint
```

Manual checks:

| Rule | Check |
|------|-------|
| Features → Infra | Features must NOT import from `infra/*` |
| Core → Android | Core must NOT import `android.*` (except annotations) |
| Domain deps | `domain/entities` and `domain/contracts` have ZERO internal deps |
| Visibility | Non-public classes use `internal` modifier |

### 3. Naming Conventions

| Type | Convention | Violation Example |
|------|------------|-------------------|
| Core classes | `WormaCeptor` prefix | `Collector` → `WormaCeptorCollector` |
| Engines | `Engine` suffix | `FpsMonitor` → `FpsMonitorEngine` |
| ViewModels | `ViewModel` suffix | `TransactionList` → `TransactionListViewModel` |
| Repositories | `Repository` suffix | `TransactionStore` → `TransactionRepository` |
| Features | `Feature` suffix | `Transaction` → `TransactionFeature` |

### 4. Code Quality

#### Anti-Patterns to Flag

```kotlin
// BLOCK: ANR risk
runBlocking { }
// Suggest: Use suspend function or viewModelScope.launch

// BLOCK: Crash risk
value!!.property!!
// Suggest: Use ?.let {}, ?:, or require() at boundaries

// BLOCK: Hides bugs
catch (e: Exception) { }
// Suggest: Catch specific exceptions

// BLOCK: Race conditions
object Singleton {
    var mutableState = mutableListOf()
}
// Suggest: Use Koin scoped instances

// BLOCK: Thread blocking
Thread.sleep(1000)
// Suggest: Use delay() in coroutines

// WARN: Resource leak
val stream = FileInputStream(file)
// Suggest: Use .use { } block
```

#### Detekt Thresholds

| Metric | Limit | Action |
|--------|-------|--------|
| Method length | 60 lines | BLOCK if exceeded |
| Parameters | 6 (8 for constructors) | BLOCK if exceeded |
| Nesting depth | 4 blocks | BLOCK if exceeded |
| Magic numbers | Only -1, 0, 1, 2 | WARN, suggest constant |
| Forbidden comments | FIXME/STOPSHIP/TODO | BLOCK in production code |

### 5. Compose Patterns

#### Parameter Ordering

```kotlin
// CORRECT
@Composable
fun MyScreen(
    state: MyState,              // 1. State
    onAction: (Action) -> Unit,  // 2. Callbacks
    modifier: Modifier = Modifier // 3. Modifier (last, with default)
)

// WRONG - flag for correction
fun MyScreen(
    modifier: Modifier = Modifier,  // Wrong position
    onAction: (Action) -> Unit,
    state: MyState
)
```

#### State Collections

```kotlin
// CORRECT - stable for recomposition
val items: ImmutableList<Item>
val selected: ImmutableSet<String>

// WRONG - causes unnecessary recomposition
val items: List<Item>
val selected: Set<String>
```

#### StateFlow Pattern

```kotlin
// CORRECT
.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = initialState
)

// WRONG - never stops collecting
started = SharingStarted.Eagerly

// WRONG - no timeout
started = SharingStarted.WhileSubscribed()
```

### 6. UI/UX Standards

| Requirement | Check |
|-------------|-------|
| Touch targets | >= 48x48dp |
| Contrast ratio | >= 4.5:1 (WCAG AA) |
| Content descriptions | All meaningful icons |
| Design tokens | No hardcoded dp/colors |
| Spacing | Follows 4dp grid |

### 7. Test Coverage

Check for:
- New public APIs have tests
- Bug fixes include regression tests
- Complex logic has unit tests
- Architecture boundaries tested (if new module)

### 8. Documentation

Required for:
- New public APIs → KDoc comments
- New modules → Update `settings.gradle.kts`
- Breaking changes → Migration notes
- New features → Update `docs/reference/02-feature-inventory.md`

NOT required (avoid noise):
- Internal implementation details
- Self-explanatory code
- Private functions

## Review Output Format

```markdown
## PR Review: #{PR_NUMBER} - {Title}

### Summary
{One sentence description of the change}

### Architecture Compliance
- [ ] Layer dependencies valid
- [ ] Naming conventions followed
- [ ] Visibility modifiers correct

### Code Quality
- [ ] No anti-patterns
- [ ] Detekt passes
- [ ] Spotless passes

### Compose Patterns (if applicable)
- [ ] Parameter ordering correct
- [ ] Immutable collections used
- [ ] StateFlow pattern correct

### UI/UX (if applicable)
- [ ] Accessibility requirements met
- [ ] Design tokens used
- [ ] Responsive patterns applied

### Blocking Issues
1. {Issue description} - {file:line}
   **Fix:** {Specific suggestion}

### Suggestions (Non-blocking)
1. {Suggestion} - {file:line}

### Verdict
- [ ] **Approve** - Ready to merge
- [ ] **Request Changes** - Blocking issues found
- [ ] **Comment** - Questions or suggestions only
```

## Quick Review Commands

```bash
# Full validation
./gradlew detekt spotlessCheck lint :test:architecture:test

# Check specific module
./gradlew :features:viewer:detekt

# Auto-fix formatting
./gradlew spotlessApply

# View PR locally
gh pr checkout <PR_NUMBER>
```

## Common Review Feedback Templates

### Missing Internal Modifier
```
This class appears to be implementation-only. Consider adding `internal` modifier to prevent accidental public API exposure.
```

### Wrong Parameter Order
```
Compose convention: parameters should be ordered as (state, callbacks, modifier). Please reorder to match project patterns.
```

### Mutable Collection in State
```
Using `List<T>` in Compose state can cause unnecessary recompositions. Please use `ImmutableList<T>` from kotlinx.collections.immutable.
```

### Hardcoded Dimensions
```
Please use design system tokens instead of hardcoded values:
- `16.dp` → `WormaCeptorDesignSystem.Spacing.lg`
- `8.dp` → `WormaCeptorDesignSystem.Spacing.sm`
```

### Missing Content Description
```
Icon is missing contentDescription. For accessibility, please add a meaningful description or use `null` only for purely decorative icons.
```

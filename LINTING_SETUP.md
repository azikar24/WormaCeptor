# Linting and Code Quality Infrastructure

This document describes the linting and code quality tools configured for WormaCeptor.

## Files Created/Modified

### Created

| File | Purpose |
|------|---------|
| `.editorconfig` | IDE consistency settings (indentation, line endings, etc.) |
| `config/detekt/detekt.yml` | Detekt static analysis configuration |
| `lint.xml` | Android Lint configuration |
| `.github/workflows/lint.yml` | GitHub Actions CI workflow |
| `.lefthook.yml` | Pre-commit hooks configuration |
| `*/detekt-baseline.xml` | Detekt baselines in each subproject (22 files) |
| `*/lint-baseline.xml` | Android Lint baselines in each subproject (20+ files) |

### Modified

| File | Changes |
|------|---------|
| `gradle/libs.versions.toml` | Added detekt, spotless, and compose-rules versions |
| `build.gradle.kts` | Added Spotless, Detekt, and Lint configurations |

## Tools Overview

### Spotless + ktlint

- **Purpose**: Code formatting enforcement
- **Configuration**: Root `build.gradle.kts`
- **ktlint version**: 1.2.1

Disabled rules (can't be auto-fixed or conflict with project conventions):
- `no-wildcard-imports`
- `filename`
- `max-line-length`
- `backing-property-naming`
- `property-naming`
- `function-naming` (conflicts with Compose naming)
- `value-parameter-comment`
- `comment-wrapping`
- `class-naming`

### Detekt

- **Purpose**: Static code analysis for Kotlin
- **Configuration**: `config/detekt/detekt.yml`
- **Version**: 1.23.5
- **Plugins**: detekt-formatting, compose-rules-detekt

Key rules enabled:
- Complexity checks (LongMethod, CyclomaticComplexMethod, etc.)
- Style checks (MagicNumber, MaxLineLength, UnusedPrivateMember, etc.)
- Potential bugs (UnsafeCast, UnusedVariable, etc.)
- Performance (SpreadOperator, ArrayPrimitive, etc.)
- Coroutines (GlobalCoroutineUsage, RedundantSuspendModifier, etc.)

### Android Lint

- **Purpose**: Android-specific code analysis
- **Configuration**: `lint.xml`
- **Baseline**: Per-module `lint-baseline.xml`

Key checks:
- Accessibility (ContentDescription, LabelFor)
- Security (HardcodedDebugMode, AllowBackup)
- Performance (UseCompoundDrawables, UseSparseArrays)
- Compose (ComposableLambdaParameterPosition, ComposableNaming)

## Available Commands

| Command | Purpose |
|---------|---------|
| `./gradlew spotlessApply` | Auto-fix formatting issues |
| `./gradlew spotlessCheck` | Check formatting without fixing |
| `./gradlew detekt` | Run static analysis |
| `./gradlew detektBaseline` | Update Detekt baselines |
| `./gradlew lint` | Run Android Lint |
| `./gradlew updateLintBaseline` | Update Lint baselines |

## CI/CD Integration

### GitHub Actions

The workflow (`.github/workflows/lint.yml`) runs on:
- Pull requests to `master` or `main`
- Pushes to `master` or `main`

Jobs:
1. **detekt** - Runs Detekt and uploads SARIF to GitHub
2. **spotless** - Checks code formatting
3. **android-lint** - Runs Android Lint and uploads results

### Pre-commit Hooks (Lefthook)

Configuration in `.lefthook.yml`:

**pre-commit:**
- Runs `spotlessApply` on changed Kotlin files

**pre-push:**
- Runs `detekt`
- Runs `lint`

## Setup Instructions

### 1. Install Lefthook (Optional but Recommended)

```bash
# macOS
brew install lefthook

# or via npm
npm install -g @evilmartians/lefthook

# Then install hooks
lefthook install
```

### 2. First-time Setup

The baselines have already been generated. To regenerate them:

```bash
# Regenerate Detekt baselines
./gradlew detektBaseline

# Regenerate Lint baselines
./gradlew updateLintBaseline
```

### 3. IDE Integration

The `.editorconfig` file will be automatically picked up by Android Studio and other IDEs that support EditorConfig.

For Detekt in Android Studio:
1. Install the "Detekt" plugin
2. Point it to `config/detekt/detekt.yml`

## Baseline Strategy

Baselines capture existing violations so that:
- New code is still checked against all rules
- Existing violations can be fixed gradually
- CI passes while technical debt is addressed

To fix violations in the baseline:
1. Fix the code issue
2. Regenerate the baseline: `./gradlew detektBaseline` or `./gradlew updateLintBaseline`
3. The fixed issue will be removed from the baseline

## Troubleshooting

### Spotless fails with "cannot auto-fix"

Some rules can't be auto-fixed. Either:
- Fix manually
- Add the rule to the disabled list in `build.gradle.kts`

### Detekt reports too many issues

Run `./gradlew detektBaseline` to update the baseline with current issues.

### Lint fails on new code

If lint fails on genuinely new issues, fix them or run `./gradlew updateLintBaseline` to add them to the baseline (not recommended for new code).

## Metrics

Initial state captured in baselines:
- Detekt: ~2000 weighted issues
- Android Lint: 1 error, 118 warnings

Target metrics:
- Lint violations: < 50 (gradual reduction)
- Format consistency: 100% (enforced by Spotless)
- PR lint pass rate: > 95%

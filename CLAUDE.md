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

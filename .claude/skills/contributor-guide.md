---
name: contributor-guide
description: Onboard contributors and help navigate WormaCeptor's 50+ module codebase. Use when someone asks "where do I start?", wants to understand how a feature works, or needs to find the right module to modify.
---

# Contributor Guide

Help contributors navigate and understand the WormaCeptor codebase.

## When to Use

- New contributor asks "where do I start?"
- Questions about how a feature works
- Finding the right module to modify
- Understanding data flow through layers

## Architecture Overview

WormaCeptor uses Clean Architecture with 7 layer groups across 50+ modules:

```
┌─────────────────────────────────────────────────────────┐
│                      HOST APP                            │
│  ┌─────────────────────────────────────────────────┐    │
│  │              API CLIENT (Public)                 │    │
│  │  WormaCeptorApi, WormaCeptorInterceptor         │    │
│  └──────────────────────┬──────────────────────────┘    │
└─────────────────────────┼───────────────────────────────┘
                          │ Discovery (reflection)
┌─────────────────────────▼───────────────────────────────┐
│                   API IMPL (Debug only)                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│  │Persistence│  │  IMDB    │  │  No-Op   │              │
│  │  (Room)   │  │(In-Memory)│  │(Release) │              │
│  └─────┬─────┘  └────┬─────┘  └──────────┘              │
└────────┼─────────────┼──────────────────────────────────┘
         │             │
┌────────▼─────────────▼──────────────────────────────────┐
│                    CORE ENGINE                           │
│  TransactionEngine, CrashEngine, FpsMonitorEngine       │
│  No Android Framework - pure Kotlin + Coroutines        │
└────────────────────────┬────────────────────────────────┘
                         │
         ┌───────────────┼───────────────┐
         │               │               │
┌────────▼───────┐ ┌─────▼─────┐ ┌───────▼───────┐
│    DOMAIN      │ │   INFRA   │ │   FEATURES    │
│  ┌──────────┐  │ │ ┌───────┐ │ │ ┌───────────┐ │
│  │ Entities │  │ │ │Parser │ │ │ │  Viewer   │ │
│  │(models)  │  │ │ │Syntax │ │ │ │  FPS      │ │
│  ├──────────┤  │ │ │SQLite │ │ │ │  Memory   │ │
│  │Contracts │  │ │ │OkHttp │ │ │ │  Crashes  │ │
│  │(interfaces)│ │ │ └───────┘ │ │ │  Tools    │ │
│  └──────────┘  │ └───────────┘ │ │  +26 more │ │
└────────────────┘               │ └───────────┘ │
                                 └───────────────┘
```

## Layer Responsibilities

| Layer | Purpose | Key Constraint |
|-------|---------|----------------|
| `api/client` | Public entry point for host apps | No implementation logic |
| `api/impl/*` | Debug/release implementations | Chosen at build time |
| `core/engine` | Business logic, monitoring | No UI, no Android framework |
| `domain/entities` | Data models | Zero dependencies |
| `domain/contracts` | Interfaces for infra | Zero dependencies |
| `features/*` | Jetpack Compose UI (31 modules) | Only Core + Domain |
| `infra/*` | Concrete implementations | Swappable, Core-agnostic |
| `platform/android` | Android utilities | Isolated from Core |

## Finding Code

### By Feature

| Feature | Modules |
|---------|---------|
| Network inspection | `features/viewer`, `core/engine`, `infra/parser/*` |
| FPS monitoring | `features/fps`, `core/engine` |
| Memory monitoring | `features/memory`, `core/engine` |
| Crash reporting | `features/crashes`, `core/engine` |
| Database browser | `features/database`, `infra/persistence/sqlite` |
| SharedPrefs viewer | `features/shared-prefs` |
| Leak detection | `features/leak-canary` |

### By Responsibility

| Task | Location |
|------|----------|
| Capture HTTP requests | `api/client/WormaCeptorInterceptor.kt` |
| Store transactions | `infra/persistence/sqlite/` |
| Parse JSON bodies | `infra/parser/json/` |
| Syntax highlighting | `infra/syntax/` |
| Display transaction list | `features/viewer/ui/` |
| Performance overlay | `features/overlay/` |

## Data Flow Example: Network Transaction

1. **Capture**: `WormaCeptorInterceptor` (api/client) intercepts OkHttp request
2. **Create**: Builds `NetworkTransaction` entity (domain/entities)
3. **Store**: `TransactionEngine` (core/engine) saves via `TransactionRepository` interface
4. **Persist**: `TransactionRepositoryImpl` (infra/persistence) writes to Room DB
5. **Query**: `TransactionListViewModel` (features/viewer) observes Flow
6. **Display**: `TransactionListScreen` (features/viewer) renders Compose UI

## First Contribution Workflow

### 1. Setup

```bash
git clone https://github.com/azikar24/WormaCeptor.git
cd WormaCeptor
./gradlew build
./gradlew :app:installDebug  # Run demo app
```

### 2. Find Your Area

- **Bug fix**: Search for the UI text or error message
- **New feature**: Check `docs/reference/02-feature-inventory.md` for planned features
- **Enhancement**: Find the existing feature module

### 3. Make Changes

```bash
# Create feature branch
git checkout -b feature/your-feature

# Make changes following patterns in CLAUDE.md

# Validate
./gradlew spotlessApply  # Format
./gradlew detekt         # Static analysis
./gradlew :test:architecture:test  # Boundaries
```

### 4. Submit

- PR to `master` branch
- Follow commit conventions
- No force push without approval

## Common Questions

**Q: How does the interceptor capture HTTPS traffic?**
A: OkHttp's interceptor chain runs after TLS decryption. The interceptor sees decrypted request/response.

**Q: Where are transactions stored?**
A: Room database via `infra/persistence/sqlite`. Schema in `TransactionEntity.kt`.

**Q: How do I add a new body parser?**
A: Create module in `infra/parser/{format}/`, implement `BodyParser` interface from `domain/contracts`.

**Q: Why doesn't my feature see infra modules?**
A: By design. Features depend only on `domain/contracts` interfaces. Infra is injected at runtime via Koin.

**Q: How do I test architecture rules?**
A: `./gradlew :test:architecture:test` runs ArchUnit tests that enforce layer boundaries.

## Key Files to Know

| File | Purpose |
|------|---------|
| `CLAUDE.md` | All conventions and patterns |
| `settings.gradle.kts` | Module registration |
| `docs/architecture/REPO_STRUCTURE.md` | Module layout |
| `docs/reference/01-technical-documentation.md` | Full architecture |
| `config/detekt/detekt.yml` | Static analysis rules |
| `features/viewer/ui/theme/DesignSystem.kt` | UI tokens |

## Getting Help

- Read `CLAUDE.md` for conventions
- Check `docs/` for architecture decisions
- Look at similar modules for patterns
- Run `./gradlew detekt` for immediate feedback

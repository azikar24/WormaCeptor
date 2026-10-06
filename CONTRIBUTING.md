# Contributing to WormaCeptor

Welcome! We appreciate your interest in contributing to WormaCeptor. Whether you're fixing a bug, adding a feature, or improving documentation, your contributions help make this project better for everyone.

## Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/AziKar24/WormaCeptor.git
   cd WormaCeptor
   ```

2. **Install JDK 17+** — The project requires JDK 17 or higher.

3. **Open in Android Studio** — Use the latest stable version of Android Studio. Import the project and let Gradle sync complete.

4. **Build the project**
   ```bash
   ./gradlew build
   ```

## Architecture Rules

WormaCeptor follows **Clean Architecture** with strict boundaries enforced by [ArchUnit](https://www.archunit.org/) tests. Violating these rules will cause CI to fail.

- **Features** can ONLY depend on **Core** and **Domain** — never on Infra modules.
- **Core** cannot depend on the Android Framework or Infra modules.
- **Domain** has zero internal dependencies — it is a pure Kotlin module with no external library references.
- If your feature introduces a heavy library, **create a new module** rather than adding the dependency to an existing one.

These boundaries keep the codebase decoupled, testable, and maintainable.

## Code Style

WormaCeptor uses **Spotless** (with ktlint) for formatting and **Detekt** for static analysis.

Before committing, always run:
```bash
./gradlew spotlessApply
```

This automatically formats your code to match the project's style. Detekt will flag code smells, complexity issues, and style violations during the build.

## Module Structure

| Responsibility       | Location            | Description                                      |
|----------------------|---------------------|--------------------------------------------------|
| Business logic       | `core/engine`       | Core processing logic, interceptors, and engines |
| Data models          | `domain/entities`   | Plain Kotlin data classes and value objects       |
| Interfaces           | `domain/contracts`  | Repository interfaces and use case contracts     |
| UI screens           | `features/*`        | Jetpack Compose screens and ViewModels           |
| Storage / Network    | `infra/*`           | Database, network, file system implementations   |
| MCP server           | `mcp/device-server` | Debug-only HTTP API the bridge queries (Android) |
| MCP bridge           | `mcp/bridge`        | JVM CLI speaking MCP over stdio                  |

An MCP tool spans four places that must change together: the tool in `mcp/bridge/.../mcp/tools/*Tools.kt`, the route in `mcp/device-server/.../routes/*Routes.kt`, its DTO in `.../serialization/dto/`, and `docs/MCP.md`. `ToolRouteTest` and `ToolDocsTest` (`./gradlew :mcp:bridge:test`) fail when they drift.

## Quality Checks

Run these commands to verify your changes before submitting a PR:

| Command                                  | Purpose                        |
|------------------------------------------|--------------------------------|
| `./gradlew build`                        | Full build                     |
| `./gradlew spotlessCheck`                | Code formatting validation     |
| `./gradlew detekt`                       | Static analysis                |
| `./gradlew lint`                         | Android Lint                   |
| `./gradlew :test:architecture:test`      | Architecture boundary tests    |
| `./gradlew codeQuality`                  | All quality checks combined    |

Running `./gradlew codeQuality` is the fastest way to verify everything passes before pushing.

## PR Process

### Branch Naming

Use a descriptive prefix for your branch:

- `feature/` — New functionality (e.g., `feature/grpc-inspector`)
- `fix/` — Bug fixes (e.g., `fix/crash-on-large-payload`)
- `refactor/` — Code improvements without behavior changes (e.g., `refactor/simplify-interceptor`)

### Commit Conventions

Follow conventional commit messages:

- `feat:` — A new feature
- `fix:` — A bug fix
- `refactor:` — Code restructuring without behavior changes
- `docs:` — Documentation updates
- `test:` — Adding or updating tests
- `chore:` — Build, CI, or tooling changes

Examples:
```
feat: add gRPC request inspection
fix: prevent crash when response body exceeds 10MB
refactor: extract shared UI components into common module
```

### PR Guidelines

- Keep PRs **small and focused** — one logical change per PR.
- Include **tests** for new functionality.
- Ensure all quality checks pass before requesting review.
- Provide a clear description of what changed and why.

## Adding a New Feature Module

To add a new feature module that follows existing patterns:

1. **Create the module directory** under `features/` (e.g., `features/my-feature`).
2. **Add a `build.gradle.kts`** file following the conventions of existing feature modules.
3. **Depend only on Core and Domain modules** — never on Infra or other feature modules.
4. **Register the module** in `settings.gradle.kts`.
5. **Create your Compose screens** and ViewModels within the module.
6. **Provide Koin modules** for dependency injection, following the existing DI patterns.
7. **Add a deep link route** if the feature should be navigable via the `wormaceptor://` scheme.
8. **Write tests** — unit tests for business logic and UI tests for Compose screens.
9. **Run `./gradlew :test:architecture:test`** to confirm your module respects the architecture boundaries.

Look at existing feature modules (e.g., `features/network`, `features/console`) as references for structure and conventions.

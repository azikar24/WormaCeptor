# ARCHITECTURE_AS_IS

## 1. System Overview
Wormaceptor is an Android library designed for intercepting and logging network traffic and application crashes. It follow a modular structure to separate core logic, persistence, and UI.

## 2. Module Decomposition

| Module                     | Responsibility                                                  | Primary Technologies               |
| :------------------------- | :-------------------------------------------------------------- | :--------------------------------- |
| `:app`                     | Demo application showing library integration.                   | Compose, Retrofit, Koin            |
| `:WormaCeptor`             | Core engine, interceptors, and library UI.                      | OkHttp, Compose, Koin, WorkManager |
| `:WormaCeptor-persistence` | Room-based implementation of data storage.                      | Room, SQLite, Paging 2             |
| `:WormaCeptor-imdb`        | In-memory database implementation (alternative to persistence). | Kotlin Collections                 |
| `:WormaCeptor-no-op`       | Empty implementation for release builds to minimize overhead.   | Kotlin                             |

## 3. Component Interaction Model

### Initialization
1.  **Host App** calls `WormaCeptor.init()` in `Application.onCreate()`.
2.  Provide a `WormaCeptorStorage` instance (constructed in `:app` using `:WormaCeptor-persistence`).
3.  `WormaCeptor` registers the storage DAO into a local **Koin** instance.
4.  Optionally configures crash logging by overriding the default `UncaughtExceptionHandler`.

### Data Interception (Network)
1.  `WormaCeptorInterceptor` is added to the host's `OkHttpClient`.
2.  On every request/response, the interceptor creates a `NetworkTransaction`.
3.  Data is persisted asynchronously via `TransactionDao`.
4.  A system notification is triggered to notify the user of new activity.

### Data Interception (Crash)
1.  `Thread.setDefaultUncaughtExceptionHandler` is captured.
2.  On crash, stack traces are formatted into a `CrashTransaction`.
3.  The crash is saved to the database before the original handler is invoked.

## 4. Architectural Boundaries

### Public API Boundary
- Located in `com.azikar24.wormaceptor.WormaCeptor`.
- Mediates interaction between the host app and internal components.

### Persistence Boundary
- Defined by `com.azikar24.wormaceptor.internal.data.WormaCeptorStorage`.
- The `:WormaCeptor` module depends only on this interface.
- Concrete implementations (like Room in `:WormaCeptor-persistence`) are injected from the outside.

### UI Boundary
- Internal to the `:WormaCeptor` module under `internal.ui`.
- Uses a `ViewModel` layer that communicates with `TransactionDao`.
- Navigation is fully encapsulated within the module using Jetpack Compose Navigation.

## 5. Identified Coupling & Leakage
- **Package Leakage**: Interfaces defined in `internal.data` packages within `:WormaCeptor` are required by `:WormaCeptor-persistence`, rendering the "internal" modifier effectively public across modules.
- **Dependency Inversion**: Properly implemented for storage, allowing the core to remain agnostic of the underlying database technology.
- **Lifecycle Coupling**: The `ShakeDetector` requires a `ComponentActivity` reference and manually attaches a lifecycle observer, creating a strong link to the Activity lifecycle.

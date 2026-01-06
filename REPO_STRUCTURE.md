# WormaCeptor Repository Structure

This document defines the final module and folder layout for WormaCeptor. The structure is designed to communicate intent, enforce architectural boundaries, and ensure AI-agent safety.

## 1. Directory Layout

```text
/
├── api/                    # Public entry points and host-app contracts
│   └── client/             # :api:client - The only module host apps should depend on
├── core/                   # The "Brain" - Pure business logic
│   └── engine/             # :core:engine - Lifecycle, filtering, search, and interception logic
├── domain/                 # The "Language" - Agnostic models and contracts
│   ├── entities/           # :domain:entities - Canonical data models (Transaction, Request, Response)
│   └── contracts/          # :domain:contracts - Interfaces for Infrastructure (Storage, Parsers)
├── features/               # The "Face" - UI and User Interaction
│   ├── viewer/             # :feature:viewer - Network list and detail screens
│   ├── settings/           # :feature:settings - Configuration UI
│   └── sharing/            # :feature:sharing - Export and reporting UI
├── infra/                  # The "Hands" - Implementation details
│   ├── persistence/        # Storage implementations
│   │   └── sqlite/         # :infra:persistence:sqlite - Room-based storage
│   └── networking/         # Network-specific logic
│       ├── okhttp/         # :infra:networking:okhttp - Interceptor implementation
│       └── parsers/        # Content-type specific parsers
│           ├── json/       # :infra:parser:json
│           └── protobuf/   # :infra:parser:protobuf
└── platform/               # Environment-specific glue
    └── android/            # :platform:android - Notifications, Shake Detector, LifeCycle integration
```

---

## 2. Module Ownership & Roles

| Group        | Responsibility                                               | Constraint                                         |
| :----------- | :----------------------------------------------------------- | :------------------------------------------------- |
| **Api**      | Defines how the host app talks to WormaCeptor.               | Must NOT contain implementation logic.             |
| **Core**     | Orchestrates data flow, manages state, and applies policies. | Must be 100% agnostic of UI and DB frameworks.     |
| **Domain**   | Contains the bedrock entities used by all layers.            | Zero internal dependencies.                        |
| **Features** | Jetpack Compose UI and ViewModels.                           | Only depends on `Core` and `Domain`. No DB access. |
| **Infra**    | Concrete implementations of `Domain` contracts.              | Swappable. Core doesn't know these exist.          |
| **Platform** | Bridges the library to the OS/Runtime.                       | Isolated to prevent leakage of OS APIs into Core.  |

---

## 3. Boundary Enforcement (The "Steel Threads")

### A. Gradle Dependency Rules
We use Gradle's `api` vs `implementation` to control transitive leakage.
- `domain:entities` is exposed via `api` in `core:engine`.
- `infra` modules are never exposed to `features`.

### B. ArchUnit Integration
Automated tests in the `:test:architecture` module (not shown in layout) enforce:
- "Core must not depend on Android Framework."
- "Features must not depend on Infra."
- "Internal visibility check: No `public` modifier on internal-only bridge classes."

### C. Logic Isolation (No Generic Folders)
- Instead of `utils`, we use context-specific modules or internal classes within the module they serve.
- Example: JSON parsing logic lives in `infra:parser:json`, not a global `JsonUtils`.

### D. AI-Agent Sandboxing
The `core:engine` contains the `PolicyEngine`. This allows us to restrict AI agents by providing a custom `PolicyProvider` at the `API` layer, effectively disabling "destructive" modules in the `Infra` or `Feature` layers at runtime.

---

## 4. Rationale for Existence

- **Why separate `Domain` and `Core`?** To allow the `Domain` (models) to be shared with `Infra` without pulling in complex `Core` business logic.
- **Why `Platform` module?** To wrap Android-specific APIs (Notifications, Shake) so they can be mocked or replaced for other platforms (e.g., KMP support) without touching the `Core`.
- **Why no `common`?** Generic folders attract "junk code". If a piece of logic is shared, it must be elevated to a named contract in `Domain` or a specific capability in `Core`.

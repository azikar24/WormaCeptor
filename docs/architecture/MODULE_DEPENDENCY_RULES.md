# WormaCeptor v2: Module Dependency Rules

To ensure a sustainable and AI-safe architecture, the following dependency rules must be strictly enforced.

## 1. Dependency Graph (The North Star)

Rules for `compileTime` and `implementation` dependencies:

| Module Type           | Can Depend On                     | Must NOT Depend On                         |
| :-------------------- | :-------------------------------- | :----------------------------------------- |
| **Feature (UI)**      | Core, Domain-Entities, API        | Infrastructure, Internal Data Sources      |
| **Core (Logic)**      | Domain-Entities, Infra-Contracts  | Feature (UI), Framework-Specific SDKs      |
| **Infra-Persistence** | Domain-Entities, Infra-Contracts  | Core (Logic), Feature (UI)                 |
| **Infra-Parsers**     | Domain-Entities, Parser-Contracts | Core (Logic), Feature (UI), Persistence    |
| **App (Host)**        | API, [Optional] Debug-UI          | Core (Internal), Infrastructure (Internal) |

## 2. Zero-Trust Enforcement
- **No Shared State:** No global singletons across modules.
- **Verification at Boundry:** The `Core` module must validate all data received from `API` or `Infrastructure` before processing.
- **Error Propagation:** Modules must not leak internal exceptions. Wrap them in Domain-specific Result types.

## 3. Visibility & API Surface
- **Internal by Default:** Use the `internal` modifier for all classes/functions not explicitly part of the module's public contract.
- **Contract Modules:** Use small modules containing only interfaces (e.g., `Infra-Contracts`) to allow multiple implementations without circular dependencies.
- **Explicit Mappings:** Every boundary MUST have a mapper.
    - Example: `OkHttp.Response` -> `Domain.Transaction` (ACL).

## 4. AI-Agent Safety Rules
- **Capability Isolation:** Features must be granular enough that an AI agent restricted to "View Only" cannot even see the code for "Modify Transaction".
- **Intervention Hooks:** All "destructive" or "mutative" actions in Core must be protected by a `UserApprovalProvider` interface, allowing the host app to block agent-led changes.

## 5. Build System Enforcement (Gradle)
- Use `api` only for shared contracts.
- Use `implementation` for everything else.
- Use `unused-dep-analysis` or similar tools to prevent dependency bloat.

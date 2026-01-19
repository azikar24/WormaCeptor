# WormaCeptor Refactor Roadmap: Strangler Fig Strategy

This document outlines the step-by-step evolution of WormaCeptor from its legacy monolith to the modular, AI-safe v2 architecture.

## 1. Core Rules
- **Strangler Fig Pattern:** No big-bang rewrite. We replace components incrementally.
- **Dependency Direction:** New modules **must not** depend on legacy modules. Legacy modules may depend on adapters to the new world.
- **Continuous Deployment:** The system must remain buildable and functional at every step.
- **Verification First:** Each phase requires proof of correctness (Tests/Walkthroughs).

---

## 2. Refactor Phases

### Phase 1: Foundation & Guardrails
Establish the "To-Be" structure without touching legacy logic.
- **Action:** Initialize new module skeletons: `:domain`, `:core`, `:api`, `:infra-persistence`, `:infra-parsers`.
- **Action:** Implement `Domain-Entities` (canonical models).
- **Guardrail:** Add Gradle dependency rules to prevent `:domain` or `:core` from importing `:WormaCeptor`.
- **Status:** Empty shells ready for implementation.

### Phase 2: The API Adapter (Decoupling the Host)
Abstract the host application from the legacy implementation.
- **Action:** Implement `com.azikar24.wormaceptor.api.WormaCeptorApi`.
- **Action:** Create `CoreRedirector` in `:api` that maps calls to the new Core logic.
- **Action:** Update the `:app` module to depend only on `:api`.
- **Status:** Host app is decoupled; logic is being redirected to new modules.

### Phase 3: Core Logic & Dual-Write
Migrate processing logic and implement the data bridge.
- **Action:** Implement `Core-Logic` with new search/filtering and threading management (Coroutines).
- **Action:** Implement a one-time data migration script to move legacy data to V2 schema.
- **Action:** Switch the interceptor to use the new Core capture engine exclusively.
- **Status:** New logic is fully handling data interception.

### Phase 4: Feature Extraction (The UI Refactor)
Replace the legacy UI with the modularized viewer.
- **Action:** Build `:feature-viewer` using Compose and the new `Core-Logic` (Reactive Flow).
- **Action:** Extract `ShakeDetector` into a standalone utility module, removing `ComponentActivity` coupling.
- **Status:** New UI is functional.

### Phase 5: Final Cut-over & Decommissioning
Kill the legacy "strangler" and achieve canonical purity.
- **Action:** Redirect all `WormaCeptorApi` calls to the new Core.
- **Action:** Complete Data Migration Phase 3 (Drop legacy tables).
- **Action:** Delete `:WormaCeptor` and `:WormaCeptor-persistence` modules.
- **Status:** Refactor complete.

---

## 3. Guardrails & Quality Controls

| Check                     | Tool                         | Enforcement                                           |
| :------------------------ | :--------------------------- | :---------------------------------------------------- |
| **Dependency Violations** | `MODULE_DEPENDENCY_RULES.md` | Gradle `implementation` scopes + ArchUnit             |
| **Memory Leaks**          | LeakCanary                   | Verified in Phase 4 during UI extraction              |
| **Data Integrity**        | Migration Tests              | Automated SQL validation between legacy and V2 tables |
| **AI Safety**             | `AgentPolicyInterceptor`     | Core-level check in Phase 3                           |

---

## 4. Rollback Strategies

### Level 1: Immediate Hotfix
If the new UI or logic crashes, emergency fixes must be applied to the V2 modules. There is no fallback to legacy code.

### Level 2: Data Recovery
In case of migration failure, use the JSON export snapshots created before Phase 3 to restore state into the V2 schema.

### Level 3: Git Tagging
Each phase will be tagged. If Phase 3 introduces severe performance regressions (e.g., ARCH-002), the team can roll back the `Interceptor` code while keeping the Foundation modules.

---

## 5. Next Steps
1. Approve this roadmap.
2. Initialize Phase 1 modules.
3. Establish the `ModuleDependencyRules` enforcement.

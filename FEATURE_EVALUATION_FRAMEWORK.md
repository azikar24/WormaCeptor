# WormaCeptor Feature Evaluation Framework

This framework is the authoritative gatekeeper for all new feature proposals in WormaCeptor. It ensures that every addition aligns with the core mission: **On-device Network Inspection & Debugging.**

## 1. Submission Requirements
Every feature proposal MUST include:
- **Problem Statement:** What specific friction point does this solve?
- **User Value:** How does this improve the developer's debugging experience?
- **Architectural Impact:** Which modules are affected? Does it introduce new dependencies?
- **AI Leverage Potential:** Can an AI Agent meaningfully use or interact with this feature?
- **Maintenance Cost:** Long-term effort to maintain, test, and support.

---

## 2. Evaluation Rubric

Features are scored from 1-5 across four dimensions. A feature must score **at least 12/20** to be considered for implementation.

| Dimension         | 1 (Poor)                       | 3 (Average)                    | 5 (Excellent)                |
| :---------------- | :----------------------------- | :----------------------------- | :--------------------------- |
| **Composability** | Monolithic, tightly coupled.   | Reasonable modularity.         | Can be a standalone plugin.  |
| **Mission Fit**   | Outside core inspection scope. | Adjacent to network debugging. | Core to network inspection.  |
| **AI Leverage**   | UI-only, opaque to agents.     | Scriptable/exposed.            | Agent-first API design.      |
| **Efficiency**    | Heavy, increases binary size.  | Moderate footprint.            | Zero-dependency/Empty No-Op. |

---

## 3. Immediate Rejection Triggers (Red Lines)
A feature is **REJECTED** if any of the following are true:
1.  **Vague Concept:** "Make the UI better" or "Add support for more things."
2.  **Boundary Bleed:** Features belonging to Crash Reporting, APM, or Database Inspection (see [PRODUCT_BOUNDARIES.md](PRODUCT_BOUNDARIES.md)).
3.  **Feature Creep:** Adding functionality that is better handled by specialized external tools (e.g., Jira integration in the core).
4.  **Implicit Authority:** Features that grant AI agents or the library itself implicit permissions without user override.

---

## 4. Architectural Principles for New Features
- **Plugin First:** If it can be a plugin, it **must** be a plugin.
- **Contract Driven:** New features must define their IO contracts before implementation.
- **No-Op Safety:** Every feature module must have a corresponding `-no-op` version that is truly empty.

---

## 5. Decision Log Process
1.  **Draft:** Create a PR with a `FEATURE_PROPOSAL.md`.
2.  **Evaluate:** The Gatekeeper (Lead Architect) applies this rubric.
3.  **Outcome:** 
    - **Approve:** Added to `FUTURE_FEATURES_PLAN.md`.
    - **Refactor:** Returned for modularization/composability improvements.
    - **Reject:** Archive proposal with a reference to the specific Red Line triggered.

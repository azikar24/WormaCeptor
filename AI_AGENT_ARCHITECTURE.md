# AI Agent Orchestration Architecture (WormaCeptor v2)

This document defines the governance and operational model for AI Agents within the WormaCeptor ecosystem. It ensures that agents empower developers without compromising security, performance, or system integrity.

## 1. Core Philosophy: Zero-Implicit Authority
Agents in WormaCeptor operate under a "Sandboxed Capability" model. 
- **No Global Access:** Agents have no access to the host app's memory or code by default.
- **Explicit Grant:** Every capability (Reading transactions, Mocking, Exporting) must be explicitly granted via the `SecurityKernel`.
- **Immutable Provenance:** Every change made by an agent is tagged with an `Agent-ID` and cannot be masked as a human action.

---

## 2. Agent Roles & Taxonomies

| Role                 | Responsibility                                                                   | Permission Level     | Scope                 |
| :------------------- | :------------------------------------------------------------------------------- | :------------------- | :-------------------- |
| **Inspector**        | Analyzes traffic for bugs, PII leaks, or performance bottlenecks.                | READ-ONLY            | Current Session       |
| **Debugger**         | Suggests fixes, modifies payloads for testing, and manages simple mocks.         | READ/WRITE (Limited) | Selected Transactions |
| **Architect**        | Observes patterns over time, suggests architectural shifts or API optimizations. | READ-ONLY            | Continuous/Historical |
| **Security Officer** | Automatically redacts PII and checks for common vulnerabilities (OWASP).         | PRIVILEGED (System)  | Global                |

---

## 3. Sandboxing & Permission Model
Agents run in a virtualized sub-context within the `Core-Logic` layer.

### 3.1 The Policy Engine
All agent requests are mediated by a `PolicyEngine` that checks:
1. **Scope:** Is the agent allowed to see this specific host/path?
2. **Action:** Is the agent allowed to perform this verb (e.g., `REPLACE_BODY`)?
3. **Budget:** Has the agent exceeded its token/compute quota for this session?

### 3.2 Capability Tokens
Instead of raw API access, agents are issued short-lived `CapabilityTokens` bound to specific transactions.
- **Example:** An Inspector gets a `ReadToken` for `Transaction-123` valid for 5 minutes.

---

## 4. Memory Scopes
To prevent context leakage and ensure privacy, memory is strictly tiered.

- **4.1 Transient Phase (L1):** Local context for the current turn. Wiped after the specific task (e.g., "Analyze this 404").
- **4.2 Session Memory (L2):** Shared across multiple turns in a single debugging session. Allows for correlation (e.g., "Why did the login fail after that 500?").
- **4.3 Global Knowledge Base (L3 - Approved):** Persistent patterns approved by the user. Contains project-specific rules (e.g., "Always redact the 'Auth' header").

---

## 5. Failure Handling & Resilience

### 5.1 Hallucination Detection
The `Core` module validates agent-generated output against strict schemas.
- If an agent generates an invalid JSON mock, the `ACL` (Anti-Corruption Layer) rejects it before it touches the host app.

### 5.2 Circuit Breakers
- **Recursion Guard:** Terminates agents that trigger themselves in a loop.
- **Resource Limits:** Hard caps on memory/CPU usage per agent invocation.

---

## 6. Human-in-the-Loop (HITL) & Overrides

### 6.1 Action Tiers
- **Tier 1 (Auto):** Pure read actions (Insights, Metrics).
- **Tier 2 (Proactive):** PII Redaction. Requires one-time opt-in.
- **Tier 3 (Supervised):** Modifying requests/responses. **Requires explicit user tap per action.**

### 6.2 The "Red Button"
A global UI toggle that instantly revokes all active `CapabilityTokens` and puts WormaCeptor into a hard `No-Op` mode for agents.

---

## 7. Auditability & Observability
Everything is logged in the `AuditManager`:
- **Traceability:** `Source: Agent(Debugger-V1) -> Action: MockResponse -> Payload: { ... } -> Target: /api/v1/user`
- **Verification:** Users can view a "Diff" of any change proposed or made by an agent.
- **Forensics:** The `AuditLog` is immutable and can be exported as part of a bug report.

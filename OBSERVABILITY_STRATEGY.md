# WormaCeptor Observability Strategy

This document defines the standards for monitorability, auditability, and performance tracking across the WormaCeptor ecosystem for humans, AI agents, and security auditors.

## 1. Structured Logging Standards
All modules must emit structured logs in JSON format to support automated analysis by AI Agents and security tools.

### 1.1 Base Schema
```json
{
  "timestamp": "ISO-8601",
  "level": "INFO | WARN | ERROR | AUDIT",
  "module": "core | ui | agent-kernel",
  "trace_id": "UUID",
  "actor": {
    "id": "string",
    "type": "HUMAN | AGENT | SYSTEM"
  },
  "message": "string",
  "context": {}
}
```

### 1.2 Audit Level
Logs with `level: AUDIT` are immutable and sent to a secure, write-only buffer. They are triggered by:
- Policy Engine decisions (Allow/Deny).
- State-changing actions (Mocking, Deleting logs).
- Permission grants to AI Agents.

---

## 2. Metrics & Health
We track three categories of metrics to ensure system stability and performance.

| Category             | Key Metrics                                           | Threshold         |
| :------------------- | :---------------------------------------------------- | :---------------- |
| **Performance**      | `interception_latency_ms`, `db_write_latency_ms`      | < 5ms per req     |
| **Agent Efficiency** | `agent_token_usage`, `agent_cpu_seconds`              | Per-session quota |
| **Security**         | `policy_violation_count`, `unauthorized_api_attempts` | Alert on > 0      |

---

## 3. Distributed Tracing
WormaCeptor uses a simplified Opentelemetry-compatible tracing model to map the lifecycle of a network transaction.

### 3.1 Trace Flow
1. **Host App**: `Interceptor` creates root span.
2. **Core**: `TransactionManager` adds child span with metadata.
3. **Infrastructure**: `StorageEngine` adds persistence span.
4. **Agent (Optional)**: If an agent analyzes the transaction, a `sub-trace` is attached showing the agent's reasoning steps.

---

## 4. Decision & Action Audits
To ensure AI-safety, every "Decision" made by the system or an agent must be reconstructible.

### 4.1 Decision Record Schema
Every `PolicyEngine` outcome is recorded:
- **Input**: The proposed action (e.g., `MOCK_RESPONSE`).
- **Policy Applied**: The rule ID that was evaluated.
- **Outcome**: `APPROVED` | `REJECTED`.
- **Rationale**: Human-readable explanation (e.g., "MOCK_RESPONSE requires Tier 3 HITL approval").

### 4.2 Agent Provenance
Every mutation in the system must contain a `provenance` field:
- `original_value`: Hash of data before change.
- `modified_by`: Agent ID.
- `approval_id`: Reference to the user's manual "OK" tap (for Tier 3 actions).

---

## 5. Visibility for Agents
Observability data is exposed to AI agents via the `Observability-API` (`/v1/metrics` and `/v1/traces`) to allow them to:
1. **Self-Correct**: Agents monitor their own latency and error rates.
2. **Contextualize**: Agents use traces to understand dependencies between network calls.

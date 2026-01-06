# WormaCeptor Agent API Contracts (v1)

This document defines the machine-reasonable, agent-first API contracts for WormaCeptor. These APIs are designed for AI agents to interact with network traffic data safely and predictably.

## 1. Design Principles

- **Agent-First Schema**: Strictly defined inputs/outputs using JSON Schema. No ambiguous formats.
- **Idempotency**: All state-changing operations require a client-generated `idempotencyKey` (UUID).
- **Safe Retries**: Responses include retry hints and detailed error codes to prevent recursive failure loops.
- **Explicit Context**: Every request must include an `Agent-Context` header describing the agent's intent.

---

## 2. API Reference

### 2.1 Search Transactions
**Endpoint**: `GET /v1/transactions`
**Description**: Query the `TransactionSummary` list with filters.

**Input Schema**:
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "host": { "type": "string", "description": "Filter by host (e.g., api.example.com)" },
    "method": { "enum": ["GET", "POST", "PUT", "DELETE", "PATCH"], "description": "Filter by HTTP method" },
    "statusCode": { "type": "integer", "description": "Filter by exact HTTP status code" },
    "pathPrefix": { "type": "string", "description": "Filter by path prefix" },
    "limit": { "type": "integer", "default": 20, "maximum": 100 },
    "offset": { "type": "integer", "default": 0 }
  }
}
```

**Output Schema**:
```json
{
  "type": "object",
  "properties": {
    "items": {
      "type": "array",
      "items": { "$ref": "#/definitions/TransactionSummary" }
    },
    "totalCount": { "type": "integer" }
  },
  "definitions": {
    "TransactionSummary": {
      "type": "object",
      "properties": {
        "id": { "type": "string", "format": "uuid" },
        "method": { "type": "string" },
        "host": { "type": "string" },
        "path": { "type": "string" },
        "code": { "type": ["integer", "null"] },
        "tookMs": { "type": ["integer", "null"] },
        "timestamp": { "type": "integer", "description": "Epoch Millis" }
      }
    }
  }
}
```

---

### 2.2 Get Transaction Details
**Endpoint**: `GET /v1/transactions/{id}`
**Description**: Retrieve full Request and Response metadata for a specific transaction.

**Output Schema**:
```json
{
  "type": "object",
  "properties": {
    "request": {
      "type": "object",
      "properties": {
        "url": { "type": "string" },
        "method": { "type": "string" },
        "headers": { "type": "object", "additionalProperties": { "type": "array", "items": { "type": "string" } } },
        "hasBody": { "type": "boolean" }
      }
    },
    "response": {
      "type": "object",
      "properties": {
        "code": { "type": "integer" },
        "headers": { "type": "object", "additionalProperties": { "type": "array", "items": { "type": "string" } } },
        "hasBody": { "type": "boolean" },
        "error": { "type": ["string", "null"] }
      }
    }
  }
}
```

---

### 2.3 Clear Session History
**Endpoint**: `POST /v1/session/clear`
**Description**: Deletes all captured transactions. **Idempotent.**

**Input Schema**:
```json
{
  "type": "object",
  "required": ["idempotencyKey"],
  "properties": {
    "idempotencyKey": { "type": "string", "format": "uuid", "description": "Unique key to prevent duplicate clears." }
  }
}
```

---

## 3. Error Codes & Handling

Agents must handle the following standard error body:

```json
{
  "errorCode": "string",
  "message": "string",
  "retryable": "boolean",
  "suggestedDelayMs": "integer"
}
```

| Code                  | Meaning                             | Agent Action                    |
| :-------------------- | :---------------------------------- | :------------------------------ |
| `RATE_LIMIT_EXCEEDED` | Too many requests from agent.       | Wait for `suggestedDelayMs`.    |
| `IDEMPOTENCY_CONFLIC` | Re-using key with different params. | Generate new key or fix params. |
| `STORAGE_REDACTED`    | Data exists but is PII-redacted.    | Log warning, do not retry.      |
| `NOT_FOUND`           | Transaction ID does not exist.      | Verify ID and search again.     |

---

## 4. Safety Constraints

1. **Read-Only by Default**: Agents can only search and inspect. Modification of `Request`/`Response` bodies during interception requires a specific `WormaCeptor-Allow-Mutate` capability token.
2. **Payload Capping**: Large bodies (>1MB) are not returned via API. Agents must use the `Blob` download endpoint with explicit intent.
3. **Audit Trail**: Every API access is logged against the `Agent-Context` provided.

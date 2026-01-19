# WormaCeptor Data Model V2 Spec

This document defines the framework-agnostic data model for WormaCeptor V2, focusing on modularity, storage efficiency, and search performance.

## 1. Design Principles
- **Framework Agnostic:** Defined via logical schemas (applicable to Room, Realm, or raw JSON).
- **Separation of Concerns:** Distinct Write Model (Capture) and Read Model (Presentation).
- **Blob Isolation:** Heavy data (bodies) is stored separately from headers and metadata to optimize list scanning.
- **Explicit Schema:** Avoids nullable soup; uses specific types for known states.

## 2. Canonical Entities

### 2.1 NetworkTransaction (The Root)
The primary aggregator for a single HTTP exchange.

| Field        | Type                              | Description                         |
| :----------- | :-------------------------------- | :---------------------------------- |
| `id`         | `UUID`                            | Unique identifier.                  |
| `timestamp`  | `EpochMillis`                     | When the transaction started.       |
| `durationMs` | `Long?`                           | Total time taken (null if pending). |
| `status`     | `Enum[ACTIVE, COMPLETED, FAILED]` | Lifecycle state.                    |

### 2.2 Request (Write-Only Metadata)
Immutable captured state of the outgoing request.

| Field     | Type                        | Description                     |
| :-------- | :-------------------------- | :------------------------------ |
| `url`     | `String`                    | Full target URL.                |
| `method`  | `Enum[GET, POST, ...]`      | HTTP Method.                    |
| `headers` | `Map[String, List[String]]` | Captured request headers.       |
| `bodyRef` | `BlobID?`                   | Pointer to stored request body. |

### 2.3 Response (Write-Only Metadata)
Immutable captured state of the incoming response.

| Field     | Type                        | Description                                              |
| :-------- | :-------------------------- | :------------------------------------------------------- |
| `code`    | `Int`                       | HTTP status code.                                        |
| `message` | `String`                    | Status message.                                          |
| `headers` | `Map[String, List[String]]` | Captured response headers.                               |
| `bodyRef` | `BlobID?`                   | Pointer to stored response body.                         |
| `error`   | `String?`                   | Error details if the request failed (e.g., IOException). |

### 2.4 TransactionSummary (Read Model)
Optimized for listing and searching.

| Field             | Type      | Description                         |
| :---------------- | :-------- | :---------------------------------- |
| `id`              | `UUID`    | Matches Root ID.                    |
| `method`          | `String`  | Summarized method.                  |
| `host`            | `String`  | Extracted host for quick filtering. |
| `path`            | `String`  | Extracted path.                     |
| `code`            | `Int?`    | Response code.                      |
| `tookMs`          | `Long?`   | Duration.                           |
| `hasRequestBody`  | `Boolean` | Flag to show "Body" icon in UI.     |
| `hasResponseBody` | `Boolean` | Flag to show "Body" icon in UI.     |

---

## 3. Read vs. Write Models

### Write Model (Capture Path)
The Interceptor creates a `NetworkTransaction` and a `Request`. When the response arrives, it populates the `Response` and `durationMs`. Heavy bodies are streamed directly to a `BlobStore` using the `BlobID`.

### Read Model (Query Path)
The UI queries `TransactionSummary`. This avoids loading heavy `headers` or `bodies` into memory while scrolling through hundreds of items. Only when a specific item is selected does the UI load the `Request`/`Response` metadata and the corresponding `Blob`.

## 4. Storage Extensions
The model supports an `Extension` map on the Root entity for non-core metadata (e.g., Protobuf message names, GraphQL operation names) as defined in [PRODUCT_BOUNDARIES.md](PRODUCT_BOUNDARIES.md).

# WormaCeptor Data Migration Plan

This plan outlines the strategy for migrating legacy data from the flat Room-based schema (V1) to the new canonical data model (V2).

## 1. Migration Goals
- **Maintain Continuity:** Ensure existing debugging sessions are not lost.
- **Incremental Efficiency:** Enable the new performance-focused Read Model immediately for legacy data.
- **Transactional Integrity:** Prevent data corruption during the schema split.

## 2. Phase 1: Pre-Migration Snapshot
Before any structural changes, we perform a mandatory export.
- **Action:** Perform a one-time JSON export of the current legacy database.
- **Benefit:** Provides a safety net for manual recovery if the direct migration fails.

## 3. Phase 2: Schema Decoupling (The "Split" Phase)
Physical separation of metadata and blobs.

### Step A: Metadata Migration
1. Create new `NetworkTransaction`, `RequestMetadata`, and `ResponseMetadata` tables.
2. Run a background migration job to:
    - Insert a record into `NetworkTransaction` for every legacy row.
    - Extract and normalize headers into the new metadata tables.
    - Reference the legacy primary key as a `legacy_id` for rollback support.

### Step B: Blob Migration
1. For each transaction with a body, move the `requestBody` and `responseBody` strings into separate files or a dedicated `RequestBlob`/`ResponseBlob` table.
2. Update the `bodyRef` in the metadata tables to point to the new location.
3. **Optimistic Cleanup:** Delete the original body columns in the legacy table to free up space (SQLite `VACUUM`).

## 4. Phase 3: Canonical Pureness
Once migration is verified across a representative set of users:
1. Drop the `PersistentNetworkTransaction` table.
2. Remove any "legacy mapping" logic from the DAOs.
3. Switch all capture logic to use the modular V2 entities directly.

## 5. Rollback Strategy
- **Restoration from Snapshot:** If Phase 2 fails or corrupts data, wipe the database and restore the state using the Phase 1 JSON export into the V2 schema.
- **Idempotency:** The migration script must be re-runnable; check for existence of V2 IDs before inserting records.

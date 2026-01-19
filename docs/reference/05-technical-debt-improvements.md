# WormaCeptor V2 - Technical Debt & Improvements

This document identifies technical debt items, architectural weaknesses, and improvement opportunities in WormaCeptor V2.

## Overview

**Current Technical Debt Status**:
- Critical: 3 items
- High: 6 items
- Medium: 6 items
- Low: 4 items
- **Total**: 19 items

## Critical Priority

### 1. SEC-001: Plaintext Storage of Sensitive Data

**Current State**: Request/response headers and bodies stored unencrypted in SQLite and filesystem.

**Problem**: Security vulnerability - device compromise exposes all captured traffic.

**Proposed Solution**: Implement SQLCipher for encrypted database, encrypt blobs before storage.

**Estimated Effort**: 3-5 days

---

### 2. PERF-001: Full Body Read into Memory

**Current State**: OkHttp interceptor reads entire response body into memory.

**Problem**: Large responses cause memory pressure and potential OOM.

**Proposed Solution**: Stream bodies directly to disk without loading into memory.

**Estimated Effort**: 5-8 days

---

### 3. ARCH-002: Threading Violation in Interceptor

**Current State**: `ServiceProviderImpl` uses `runBlocking` in OkHttp interceptor.

**Problem**: Blocks network thread pool, reducing concurrency, can cause timeouts.

**Proposed Solution**: Use fire-and-forget coroutine scope without blocking.

**Estimated Effort**: 2-3 days

---

## High Priority

### 4. Replace CoreHolder with Proper Dependency Injection

**Current State**: Global mutable singleton with force unwraps.

**Problem**: Hard to test, hidden dependencies, NPE risk.

**Proposed Solution**: Integrate Hilt for constructor injection.

**Estimated Effort**: 5-7 days

---

### 5-7. Testing: Add Unit, Integration, and UI Tests

**Current State**: Only 1 test file (1.5% coverage).

**Problem**: Refactoring is dangerous, regressions go undetected.

**Proposed Solution**: Comprehensive test suite for business logic, repositories, and UI.

**Estimated Effort**: 3-4 weeks total

---

### 8. Fix CrashReporter Race Condition

**Current State**: Uses `Thread.sleep(500)` hack for DB write.

**Problem**: Unreliable crash persistence.

**Proposed Solution**: Synchronous write with `runBlocking` (acceptable during crash).

**Estimated Effort**: 1 day

---

### 9. Implement Proper Logging Framework

**Current State**: Uses `printStackTrace()` for error handling.

**Problem**: No structured logging, no log levels.

**Proposed Solution**: Integrate Timber for structured logging.

**Estimated Effort**: 2 days

---

## Medium Priority

### 10. Eliminate Code Duplication: NotificationHelper

**Problem**: Duplicated in persistence and imdb modules.

**Solution**: Move to shared `:platform:android:notifications` module.

---

### 11. Extract URL Parsing Utilities

**Problem**: URL parsing repeated in multiple files.

**Solution**: Create `URLUtils` extension functions.

---

### 12. Entity Mapping Library (Optional)

**Problem**: Manual `toDomain()` methods are verbose.

**Solution**: Consider MapStruct or keep manual (acceptable at current scale).

---

### 13. Add Pagination for Transaction Lists

**Problem**: Room queries return full lists, causing performance issues at scale.

**Solution**: Integrate Paging 3 library.

**Estimated Effort**: 3-5 days

---

### 14. Optimize Search Performance

**Problem**: `LIKE '%query%'` causes full table scans.

**Solution**: Add Full-Text Search (FTS) table.

**Estimated Effort**: 2-3 days

---

### 15. Externalize Hard-Coded Values

**Problem**: Magic numbers throughout codebase.

**Solution**: Create `WormaCeptorConfig` object with named constants.

---

## Low Priority

### 16. Extract UI Strings to strings.xml

**Problem**: Hard-coded strings, no i18n support.

---

### 17. Replace Magic Numbers with Named Constants

**Problem**: Time periods calculated inline.

---

### 18. Reduce lateinit vars

**Problem**: Risk of `UninitializedPropertyAccessException`.

---

### 19. Thread Safety for RedactionConfig

**Problem**: Mutable sets accessed from multiple threads.

**Solution**: Use `ConcurrentHashMap.newKeySet()`.

---

## Summary and Recommendations

### Immediate Actions (Next 2 Weeks)
1. **SEC-001**: Implement database encryption
2. **ARCH-002**: Remove runBlocking from interceptor
3. **Fix CrashReporter**: Replace Thread.sleep

### Short-Term (1-2 Months)
4. **PERF-001**: Implement streaming blob storage
5. **Replace CoreHolder**: Integrate Hilt
6. **Add Unit Tests**: Start with critical business logic

### Medium-Term (3-6 Months)
7. **Add Integration Tests**: End-to-end flows
8. **Add UI Tests**: Compose screen testing
9. **Pagination**: Paging 3 integration
10. **FTS Search**: Full-text search optimization

### Metrics to Track

**Code Quality**: Test coverage >70%, code duplication <5%

**Performance**: Memory <50MB, intercept latency <5ms, query time <100ms for 10k records

**Security**: 0 critical vulnerabilities, 0 plaintext sensitive data

## Conclusion

WormaCeptor V2 has a solid architectural foundation but carries technical debt in three critical areas:
1. **Security**: Plaintext storage needs encryption
2. **Performance**: Threading violations and memory issues
3. **Testing**: Insufficient test coverage

Addressing Critical and High priority items will significantly improve the codebase quality, security, and maintainability.

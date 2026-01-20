# Code Review Progress Tracker

Last updated: 2026-01-20

---

## Summary

| Status | Count | Percentage |
|--------|-------|------------|
| Completed | 14 | ~17% |
| In Progress | 0 | 0% |
| Remaining | 68+ | ~83% |

**Total Issues from Review:** 82+

---

## Completed Items

| Date | Category | Issue | Source |
|------|----------|-------|--------|
| 2026-01-20 | Duplicate Code | `quickactions/` directory removed (~2000 lines) | 01-DUPLICATE-CODE.md |
| 2026-01-20 | Duplicate Code | `WormaCeptorNotificationHelper` consolidated to `api/common/` (~82 lines saved) | 01-DUPLICATE-CODE.md |
| 2026-01-20 | Build Config | Enable parallel builds and caching in gradle.properties | 07-GRADLE-CONFIG.md |
| 2026-01-20 | Thread Safety | CoreHolder atomic initialization with AtomicReference | 02-BAD-PRACTICES.md |
| 2026-01-20 | Thread Safety | ServiceProviderImpl TOCTOU race condition fixed (imdb + persistence) | 02-BAD-PRACTICES.md |
| 2026-01-20 | Bad Practices | `!!` null assertions replaced with requireNotNull in ViewerActivity | 02-BAD-PRACTICES.md |
| 2026-01-20 | Duplicate Code | WormaCeptorDesignSystem deduplicated (app module now uses viewer's) | 01-DUPLICATE-CODE.md |
| 2026-01-20 | Compose Stability | Added @Immutable to ShortcutKey, ComposeSyntaxColors | 05-COMPOSE-STABILITY.md |
| 2026-01-20 | Compose Stability | Added @Stable to KeyboardShortcutCallbacks | 05-COMPOSE-STABILITY.md |
| 2026-01-20 | Performance | O(n^2) search highlighting replaced with Regex.findAll O(n) | 03-CODE-QUALITY-ISSUES.md |
| 2026-01-20 | Accessibility | contentDescription added to high priority icons (8 files) | 06-ACCESSIBILITY.md |
| 2026-01-20 | Accessibility | Checkbox semantics added to SelectableTransactionItem | 06-ACCESSIBILITY.md |
| 2026-01-20 | ANR Fix | ViewerActivity suspend functions wrapped in coroutine scopes | 02-BAD-PRACTICES.md |
| 2026-01-20 | ANR Fix | ViewerViewModel filtering moved to Dispatchers.Default with flowOn | 03-CODE-QUALITY-ISSUES.md |

---

## In Progress

None currently.

---

## Remaining Items by Priority

### Immediate Priority (Critical)

| # | Issue | File | Source |
|---|-------|------|--------|
| 1 | `compileSdk` (34) < `targetSdk` (36) | gradle/libs.versions.toml | 07-GRADLE-CONFIG.md |
| 2 | `runBlocking` in production code (ANR risk) | ServiceProviderImpl.kt (2 files) | 02-BAD-PRACTICES.md |
| 3 | Memory leak - StateListener not removed | WormaCeptorToolWindowPanel.kt | 04-ANDROID-STUDIO-PLUGIN.md |
| 4 | EDT blocking - synchronous ADB calls | ClearTransactionsAction.kt, OpenViewerAction.kt | 04-ANDROID-STUDIO-PLUGIN.md |
| 5 | Touch target < 48dp | HomeScreen.kt:451 | 06-ACCESSIBILITY.md |
| 6 | Process timeout ineffective (ADB hang risk) | WormaCeptorServiceImpl.kt | 04-ANDROID-STUDIO-PLUGIN.md |
| 7 | Missing dispose hook for timer | WormaCeptorToolWindowPanel.kt | 04-ANDROID-STUDIO-PLUGIN.md |

### High Priority

| # | Issue | File | Source |
|---|-------|------|--------|
| 1 | ServiceProviderImpl 95% duplicate (~110 lines) | api/impl/imdb, api/impl/persistence | 01-DUPLICATE-CODE.md |

### Medium Priority

| # | Issue | File | Source |
|---|-------|------|--------|
| 1 | formatBytes() - 3 variations | TransactionDetailScreen.kt, LoadingStates.kt | 01-DUPLICATE-CODE.md |
| 2 | copyToClipboard() - 4+ occurrences | Multiple files | 01-DUPLICATE-CODE.md |
| 3 | shareText() - 4+ occurrences | Multiple files | 01-DUPLICATE-CODE.md |
| 4 | URL parsing pattern - 6+ occurrences | Multiple files | 01-DUPLICATE-CODE.md |
| 5 | Parser empty body handling - 7+ occurrences | infra/parser/*/ | 01-DUPLICATE-CODE.md |
| 6 | Status color logic - 3+ occurrences | Multiple files | 01-DUPLICATE-CODE.md |
| 7 | Magic numbers in 28 files | Multiple files | 02-BAD-PRACTICES.md |
| 8 | Hardcoded magic bytes | WormaCeptorInterceptor.kt | 02-BAD-PRACTICES.md |
| 9 | Silent error handling | WormaCeptorInterceptor.kt | 02-BAD-PRACTICES.md |
| 10 | Hardcoded strings (i18n) | HomeScreen.kt, plugin files | 02-BAD-PRACTICES.md |
| 11 | Unstable List/Map parameters | HomeScreen.kt | 05-COMPOSE-STABILITY.md |
| 12 | Missing ProGuard/R8 rules | app/build.gradle.kts | 07-GRADLE-CONFIG.md |
| 13 | Hardcoded versions in plugin | plugins/android-studio/build.gradle.kts | 07-GRADLE-CONFIG.md |
| 14 | Kotlin version mismatch | libs.versions.toml vs plugin | 07-GRADLE-CONFIG.md |
| 15 | Mixed responsibilities in ServiceProviderImpl | ServiceProviderImpl.kt | 03-CODE-QUALITY-ISSUES.md |
| 16 | Filter chip semantics missing | HomeScreen.kt | 06-ACCESSIBILITY.md |
| 17 | Filter card state announcements missing | FilterBottomSheetContent.kt | 06-ACCESSIBILITY.md |
| 18 | Missing contentDescription (remaining icons) | Multiple files | 06-ACCESSIBILITY.md |

### Low Priority

| # | Issue | File | Source |
|---|-------|------|--------|
| 1 | Unused CompositionLocal subscriptions | TextWithStartEllipsis.kt, TransactionDetailScreen.kt | 03-CODE-QUALITY-ISSUES.md |
| 2 | Dead code - unused functions | SwipeRefreshWrapper.kt, GestureNavigationComponents.kt | 03-CODE-QUALITY-ISSUES.md |
| 3 | Unused variables in MultipartBodyParser | MultipartBodyParser.kt | 03-CODE-QUALITY-ISSUES.md |
| 4 | Non-idiomatic Kotlin (Math.sqrt) | ShakeDetector.kt | 02-BAD-PRACTICES.md |
| 5 | Large function (375 lines) | TransactionDetailScreen.kt ResponseTab | 02-BAD-PRACTICES.md |
| 6 | Resource leak - InputStream not closed | WormaCeptorInterceptor.kt | 02-BAD-PRACTICES.md |
| 7 | Jetifier still enabled | gradle.properties | 07-GRADLE-CONFIG.md |
| 8 | Android deps in domain modules | domain/entities, domain/contracts | 07-GRADLE-CONFIG.md |
| 9 | Missing test dependencies | All module build files | 07-GRADLE-CONFIG.md |
| 10 | Shared mutable state in plugin | WormaCeptorServiceImpl.kt | 04-ANDROID-STUDIO-PLUGIN.md |
| 11 | Missing heading semantics | MetricsCard.kt | 06-ACCESSIBILITY.md |
| 12 | Unused rememberCoroutineScope | SwipeRefreshWrapper.kt | 05-COMPOSE-STABILITY.md |
| 13 | Missing derivedStateOf | HomeScreen.kt | 05-COMPOSE-STABILITY.md |

---

## Quick Reference: Source Files

| File | Description | Issues |
|------|-------------|--------|
| [01-DUPLICATE-CODE.md](01-DUPLICATE-CODE.md) | Duplicate code patterns | 9 patterns |
| [02-BAD-PRACTICES.md](02-BAD-PRACTICES.md) | Anti-patterns and bad practices | 15 issues |
| [03-CODE-QUALITY-ISSUES.md](03-CODE-QUALITY-ISSUES.md) | Performance, architecture, dead code | 14 issues |
| [04-ANDROID-STUDIO-PLUGIN.md](04-ANDROID-STUDIO-PLUGIN.md) | IntelliJ plugin issues | 7 issues |
| [05-COMPOSE-STABILITY.md](05-COMPOSE-STABILITY.md) | Recomposition and stability | 7 issues |
| [06-ACCESSIBILITY.md](06-ACCESSIBILITY.md) | Screen reader and touch target issues | 20+ issues |
| [07-GRADLE-CONFIG.md](07-GRADLE-CONFIG.md) | Build configuration issues | 10 issues |
| [08-DEPENDENCIES.md](08-DEPENDENCIES.md) | Dependency vulnerabilities | 0 issues |

---

## Quick Wins (Can be fixed quickly)

1. **Fix compileSdk** - Change 34 to 36 in libs.versions.toml
2. ~~**Enable parallel builds** - Uncomment line in gradle.properties~~ DONE
3. ~~**Add @Immutable annotations** - 3 data classes~~ DONE
4. **Remove unused code** - 8+ unused functions
5. ~~**Add contentDescription** - 15+ null icons~~ PARTIAL (high priority done)

---

## Notes

- Issue counts reflect original code review findings
- Completed items reduce the total lines of duplicate code
- Some issues have multiple occurrences counted as one pattern

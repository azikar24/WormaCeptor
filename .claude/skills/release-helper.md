---
name: release-helper
description: Guide through WormaCeptor version bumps, changelog generation, and release process. Use when preparing a new release, updating version numbers, or publishing to JitPack.
---

# Release Helper

Guide through version bumps and the release process for WormaCeptor.

## When to Use

- Preparing a new release
- Bumping version numbers
- Generating changelog
- Publishing to JitPack
- Creating release notes

## Release Process Overview

```
1. Verify ready for release
2. Update version numbers
3. Generate changelog
4. Create release commit
5. Tag the release
6. Push to trigger JitPack build
7. Create GitHub release
8. Verify JitPack publication
```

## Step 1: Pre-Release Checklist

### Code Quality

```bash
# Run full validation suite
./gradlew clean build
./gradlew detekt
./gradlew spotlessCheck
./gradlew lint
./gradlew :test:architecture:test
./gradlew test
```

All must pass with zero errors.

### Feature Completeness

- [ ] All planned features for this release implemented
- [ ] All blocking bugs fixed
- [ ] Breaking changes documented
- [ ] Demo app updated to showcase new features

### Documentation

- [ ] `CLAUDE.md` updated with new patterns
- [ ] `docs/reference/02-feature-inventory.md` updated
- [ ] API changes documented
- [ ] Migration guide for breaking changes (if any)

## Step 2: Version Bump

### Semantic Versioning

| Change Type | Version Bump | Example |
|-------------|--------------|---------|
| Breaking API changes | MAJOR | 1.0.0 → 2.0.0 |
| New features (backward compatible) | MINOR | 1.0.0 → 1.1.0 |
| Bug fixes only | PATCH | 1.0.0 → 1.0.1 |

### Update Version in gradle.properties

```properties
# gradle.properties
VERSION_NAME=1.2.0
VERSION_CODE=12
```

Or in root `build.gradle.kts`:

```kotlin
// build.gradle.kts
val libraryVersion = "1.2.0"
```

### Verify Version Propagation

```bash
# Check version is picked up by all modules
./gradlew printVersion
# Or check a specific module
./gradlew :api:client:properties | grep version
```

## Step 3: Generate Changelog

### Gather Changes Since Last Release

```bash
# Get last release tag
git describe --tags --abbrev=0

# List commits since last release
git log $(git describe --tags --abbrev=0)..HEAD --oneline

# List commits with more detail
git log $(git describe --tags --abbrev=0)..HEAD --pretty=format:"- %s (%h)"
```

### Changelog Format

```markdown
## [1.2.0] - 2026-01-29

### Added
- FPS monitoring with real-time overlay (#123)
- Memory threshold alerts (#125)
- WebSocket frame inspection (#130)

### Changed
- Improved transaction list performance (#127)
- Updated Compose BOM to 2024.10.01 (#128)

### Fixed
- Crash when parsing malformed JSON (#124)
- Memory leak in transaction detail view (#126)

### Breaking Changes
- `WormaCeptorApi.init()` now requires `features` parameter
- Removed deprecated `setMaxContentLength()` - use `maxContentLength()` instead

### Migration Guide
```kotlin
// Before
WormaCeptorApi.init(context)

// After
WormaCeptorApi.init(context, features = Feature.ALL)
```
```

### Update CHANGELOG.md

Add new version section at the top of `CHANGELOG.md`.

## Step 4: Create Release Commit

```bash
# Stage version and changelog changes
git add gradle.properties CHANGELOG.md

# Create release commit
git commit -m "chore: release v1.2.0"
```

## Step 5: Tag the Release

```bash
# Create annotated tag
git tag -a v1.2.0 -m "Release v1.2.0"

# Verify tag
git show v1.2.0
```

### Tag Naming Convention

- Always prefix with `v`: `v1.2.0`
- No build metadata in tags
- Match exactly what's in gradle.properties

## Step 6: Push to Remote

```bash
# Push commit
git push origin master

# Push tag (triggers JitPack build)
git push origin v1.2.0
```

## Step 7: Create GitHub Release

### Via CLI

```bash
gh release create v1.2.0 \
  --title "WormaCeptor v1.2.0" \
  --notes-file RELEASE_NOTES.md
```

### Release Notes Template

```markdown
# WormaCeptor v1.2.0

## Highlights

- **FPS Monitoring**: Real-time frame rate tracking with customizable overlay
- **Memory Alerts**: Get notified when memory usage exceeds thresholds
- **WebSocket Support**: Inspect WebSocket frames in real-time

## Installation

```kotlin
// API client (required)
implementation("com.github.azikar24.WormaCeptor:api-client:1.2.0")

// Debug implementation (choose one)
debugImplementation("com.github.azikar24.WormaCeptor:api-impl-persistence:1.2.0")
// or
debugImplementation("com.github.azikar24.WormaCeptor:api-impl-imdb:1.2.0")
```

## What's Changed

### New Features
- FPS monitoring with overlay (#123)
- Memory threshold alerts (#125)
- WebSocket frame inspection (#130)

### Improvements
- 40% faster transaction list rendering (#127)

### Bug Fixes
- Fixed crash on malformed JSON (#124)
- Fixed memory leak in detail view (#126)

### Breaking Changes
See [Migration Guide](docs/migration/v1.2.0.md) for details.

## Contributors
@azikar24, @contributor1, @contributor2

**Full Changelog**: https://github.com/azikar24/WormaCeptor/compare/v1.1.0...v1.2.0
```

## Step 8: Verify JitPack Publication

### Check Build Status

1. Go to: `https://jitpack.io/#azikar24/WormaCeptor`
2. Find version `1.2.0` in the list
3. Check build log if status is not green

### Test Integration

Create a test project and verify:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.github.azikar24.WormaCeptor:api-client:1.2.0")
    debugImplementation("com.github.azikar24.WormaCeptor:api-impl-persistence:1.2.0")
}
```

### Common JitPack Issues

| Issue | Solution |
|-------|----------|
| Build fails | Check `jitpack.yml` config, ensure `./gradlew build` works |
| Module not found | Verify module is in `settings.gradle.kts` |
| Version not appearing | Wait 5-10 minutes, or trigger manual build |
| Wrong artifact name | Check `publishing` block in build.gradle.kts |

## Hotfix Release Process

For urgent fixes on released versions:

```bash
# Create hotfix branch from tag
git checkout -b hotfix/1.2.1 v1.2.0

# Make fix
# ... changes ...

# Commit and tag
git commit -m "fix: critical crash in parser"
git tag -a v1.2.1 -m "Hotfix v1.2.1"

# Push
git push origin hotfix/1.2.1
git push origin v1.2.1

# Merge back to master
git checkout master
git merge hotfix/1.2.1
git push origin master
```

## Release Cadence Recommendations

| Type | Frequency | Scope |
|------|-----------|-------|
| Major | When needed | Breaking changes, major rewrites |
| Minor | Monthly | New features, enhancements |
| Patch | As needed | Bug fixes, security patches |

## Post-Release Tasks

- [ ] Announce on relevant channels
- [ ] Update demo app on Play Store (if applicable)
- [ ] Close related GitHub milestones
- [ ] Update project boards
- [ ] Monitor for early bug reports

# WormaCeptor Skills Guide

Quick reference for using project-specific skills effectively.

## Available Skills

| Skill | Command | Purpose |
|-------|---------|---------|
| Feature Builder | `/feature-builder` | Scaffold new feature modules |
| Architecture Guardian | `/architecture-guardian` | Validate boundaries and conventions |
| Contributor Guide | `/contributor-guide` | Navigate codebase, understand patterns |
| Integration Helper | `/integration-helper` | Setup and troubleshoot host app integration |
| Debug UI Designer | `/debug-ui-designer` | Design responsive, accessible UIs |
| PR Reviewer | `/pr-reviewer` | Review code against project standards |
| Release Helper | `/release-helper` | Version bumps and release process |
| Perf Analyzer | `/perf-analyzer` | Optimize performance, measure overhead |

---

## Feature Builder

**Use when:** Creating new debugging tools, inspectors, or screens.

### Examples

```
/feature-builder
> Create a battery monitor that shows charge level, temperature, and charging state
```

```
/feature-builder
> Add a ContentProvider browser to inspect app content providers
```

```
/feature-builder
> Extend the memory feature with a heap dump export option
```

### Tips

- Describe the data the feature will display
- Mention if it needs a background engine
- Specify if it should appear in Tools or as a main tab
- Reference similar existing features for context

### Workflow

```
1. /feature-builder → scaffold module
2. Implement business logic
3. /architecture-guardian → validate before PR
4. /pr-reviewer → final check
```

---

## Architecture Guardian

**Use when:** Validating changes before commit, reviewing PRs, or checking if a dependency is allowed.

### Examples

```
/architecture-guardian
> Check my changes on feature/battery-monitor branch
```

```
/architecture-guardian
> Can features/viewer depend on infra/parser/json?
```

```
/architecture-guardian
> Review the transaction module for anti-patterns
```

### Tips

- Run before every PR to catch issues early
- Use when unsure about dependency rules
- Helpful after large refactors
- Catches naming convention violations

### Quick Commands

```bash
# Full validation (what the skill runs)
./gradlew detekt spotlessCheck lint :test:architecture:test

# Quick check
./gradlew detekt :test:architecture:test
```

---

## Contributor Guide

**Use when:** New to the codebase, trying to understand how something works, or finding where to make changes.

### Examples

```
/contributor-guide
> How does the network interceptor capture HTTPS traffic?
```

```
/contributor-guide
> Where should I add a new body parser for Protocol Buffers?
```

```
/contributor-guide
> Explain the data flow from request capture to UI display
```

```
/contributor-guide
> I want to add a feature - where do I start?
```

### Tips

- Ask "where" questions to find the right module
- Ask "how" questions to understand data flow
- Great for onboarding new contributors
- Use before diving into unfamiliar code

---

## Integration Helper

**Use when:** Setting up WormaCeptor in an app, configuring options, or debugging "why isn't it working?"

### Examples

```
/integration-helper
> Set up WormaCeptor in my Kotlin app with Retrofit and Hilt
```

```
/integration-helper
> No transactions are appearing - help me debug
```

```
/integration-helper
> How do I redact sensitive headers and body fields?
```

```
/integration-helper
> Configure WormaCeptor to only capture API calls, not image requests
```

### Tips

- Include your tech stack (Retrofit, Ktor, Hilt, etc.)
- Mention error messages or symptoms
- Share relevant code snippets
- Specify debug vs release build issues

---

## Debug UI Designer

**Use when:** Designing new screens, improving layouts, ensuring accessibility, or handling different screen sizes.

### Examples

```
/debug-ui-designer
> Design the battery monitor screen with real-time chart and stats
```

```
/debug-ui-designer
> Make the transaction list responsive for tablets
```

```
/debug-ui-designer
> Review the memory screen for accessibility compliance
```

```
/debug-ui-designer
> Create a filter bottom sheet for the crash list
```

### Tips

- Describe the information hierarchy
- Mention if it needs to work on tablets/foldables
- Reference existing screens for consistency
- Specify any custom interactions needed

### Design System Quick Reference

```kotlin
// Spacing
Spacing.xs = 4.dp   // Tight
Spacing.sm = 8.dp   // Small gaps
Spacing.md = 12.dp  // Default
Spacing.lg = 16.dp  // Section gaps

// Status Colors
StatusGreen  // Success, enabled
StatusAmber  // Warning, pending
StatusRed    // Error, critical
StatusBlue   // Info, selected

// Touch targets: minimum 48x48dp
// Contrast ratio: minimum 4.5:1
```

---

## PR Reviewer

**Use when:** Reviewing contributor PRs, self-reviewing before merge, or auditing code quality.

### Examples

```
/pr-reviewer
> Review PR #42
```

```
/pr-reviewer
> Check my changes on feature/websocket-monitor for merge readiness
```

```
/pr-reviewer
> Audit the features/database module for code quality
```

### Tips

- Provide PR number or branch name
- Mention specific concerns if any
- Use for self-review before requesting others
- Catches common issues automatically

### Review Checklist Preview

- Layer dependencies valid
- Naming conventions followed
- No anti-patterns (runBlocking, !!, bare Exception)
- Compose patterns correct
- Accessibility requirements met

---

## Release Helper

**Use when:** Preparing a release, bumping versions, generating changelog, or publishing.

### Examples

```
/release-helper
> Prepare release 1.3.0 with the new battery and WebSocket features
```

```
/release-helper
> Create a hotfix release 1.2.1 for the JSON parsing crash
```

```
/release-helper
> Generate changelog since v1.2.0
```

```
/release-helper
> Verify the JitPack build for v1.3.0
```

### Tips

- Decide version bump type: major/minor/patch
- List key features for release notes
- Run full validation before releasing
- Test JitPack integration after publishing

### Version Bump Rules

| Change | Bump | Example |
|--------|------|---------|
| Breaking API | Major | 1.0.0 → 2.0.0 |
| New features | Minor | 1.0.0 → 1.1.0 |
| Bug fixes | Patch | 1.0.0 → 1.0.1 |

---

## Perf Analyzer

**Use when:** Investigating slowness, optimizing critical paths, or measuring WormaCeptor's overhead.

### Examples

```
/perf-analyzer
> The transaction list is laggy with 1000+ items
```

```
/perf-analyzer
> Measure WormaCeptor's initialization overhead
```

```
/perf-analyzer
> Optimize the JSON parsing for large response bodies
```

```
/perf-analyzer
> Profile memory usage during heavy network traffic
```

### Tips

- Describe the symptom (slow, janky, high memory)
- Mention data volume (100 items vs 10000)
- Specify device if relevant (low-end vs flagship)
- Include reproduction steps

### Performance Targets

| Metric | Target |
|--------|--------|
| Init time | < 50ms |
| Memory (idle) | < 5MB |
| Per-request overhead | < 1ms |
| UI frame time | < 16ms |

---

## Skill Chaining

Combine skills for complete workflows:

### New Feature Workflow

```
1. /feature-builder      → Scaffold the module
2. [implement logic]
3. /debug-ui-designer    → Design the UI
4. [implement UI]
5. /architecture-guardian → Validate architecture
6. /pr-reviewer          → Final review
```

### Performance Fix Workflow

```
1. /perf-analyzer        → Identify bottleneck
2. [implement fix]
3. /perf-analyzer        → Verify improvement
4. /architecture-guardian → Check no violations
5. /pr-reviewer          → Review changes
```

### Release Workflow

```
1. /architecture-guardian → Ensure codebase is clean
2. /pr-reviewer          → Review pending changes
3. /release-helper       → Prepare and publish
```

---

## Tips for Effective Use

### Be Specific

```
# Less effective
/feature-builder
> Add a new feature

# More effective
/feature-builder
> Create a Bluetooth device scanner that lists paired devices,
> shows connection status, and allows toggling connections
```

### Provide Context

```
# Less effective
/perf-analyzer
> It's slow

# More effective
/perf-analyzer
> Transaction list drops to 30fps when scrolling with 500+ items.
> Tested on Pixel 4a, debug build. Compose layout inspector shows
> frequent recompositions on TransactionRow.
```

### Reference Existing Code

```
/debug-ui-designer
> Design the Bluetooth scanner UI similar to the WiFi networks
> screen in features/network-info, but with connection toggle buttons
```

### Chain Related Skills

Don't try to do everything in one skill - use the right tool for each step:

```
# Instead of asking feature-builder to also review:
/feature-builder → create module
/architecture-guardian → validate it
```

---

## Troubleshooting

### Skill Not Found

Ensure you're in the WormaCeptor project directory. Skills are loaded from `.claude/skills/`.

### Skill Seems Outdated

Skills are read fresh each time. If you updated a skill file, just invoke it again.

### Too Much Output

Be more specific in your request to get focused guidance.

### Need Something Not Covered

1. Check if another skill partially covers it
2. Combine multiple skills
3. Consider creating a new skill with `/skill-creator`

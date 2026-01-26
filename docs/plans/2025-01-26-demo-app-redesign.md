# Demo App Redesign

## Overview

Redesign the WormaCeptor demo app from a cluttered utility screen to a polished showcase that conveys professionalism and the power of the toolkit.

**Design direction:** Minimal and refined, inspired by Linear and Notion. Whitespace-driven, subtle animations, premium feel.

## Structure

Two-tab layout using a segmented control:

| Tab | Purpose |
|-----|---------|
| Showcase | Hero presentation with feature highlights and launch CTA |
| Test Tools | Organized list of testing triggers for developers |

## Color Palette

| Token | Light | Dark | Usage |
|-------|-------|------|-------|
| `background` | `#FFFFFF` | `#0A0A0A` | Page background |
| `surface` | `#FAFAFA` | `#141414` | Segmented control, subtle sections |
| `textPrimary` | `#0A0A0A` | `#FAFAFA` | Headlines, labels |
| `textSecondary` | `#6B6B6B` | `#8A8A8A` | Descriptions, hints |
| `textTertiary` | `#9CA3AF` | `#525252` | Footer, version |
| `accent` | `#7C3AED` | `#A78BFA` | Primary button, selected indicator |
| `accentSubtle` | `#7C3AED12` | `#A78BFA15` | Button hover/pressed states |
| `destructive` | `#DC2626` | `#F87171` | Crash/leak action text only |

## Typography

- System fonts only (San Francisco/Roboto)
- Headlines: `600` weight
- Body: `400` weight
- Labels: `500` weight

## Spacing

- Base unit: `8dp`
- Content margins: `24dp`
- Section gaps: `32dp`
- List item gaps: `4dp`

## Components

### Segmented Control

- Full width minus margins
- Height: `40dp`
- Corner radius: `8dp`
- Background: `surface`
- Selected: White/dark with subtle shadow
- Animation: Smooth slide, `250ms`

### Showcase Tab

**Hero section:**
- Logo: `64dp` width, centered
- App name: `24sp`, `600` weight
- Tagline: `14sp`, `textSecondary`

**Feature grid:**
- 3 items in a row
- Each: outlined icon (`20dp`) + label (`12sp`)
- Labels: "Network", "Crashes", "Tools"
- No backgrounds or borders
- Tappable (launches inspector)

**Primary CTA:**
- Pill shape (fully rounded)
- Background: `accent`
- Text: White, `500` weight, `16sp`
- Padding: `16dp` vertical, `32dp` horizontal
- Scale animation on press (`0.98`)

**Footer:**
- Version number + GitHub link
- `textTertiary` color
- Subtle, unobtrusive

### Test Tools Tab

**Section headers:**
- All caps, `12sp`, `500` weight
- `textTertiary` color
- Letter spacing: `0.5sp`

**List items:**
- Height: `48dp`
- Icon (`20dp`) + Label + optional chevron
- No dividers, whitespace separation
- Standard ripple effect

**Groups:**

```
NETWORK
  Run API Tests
  WebSocket Test

DEBUG TRIGGERS
  Trigger Crash          [destructive]
  Trigger Memory Leak    [destructive]
  Thread Violation       [destructive]

FEATURE TESTS
  Location Simulator     >
  Cookie Inspector       >
  WebView Monitor        >
  Secure Storage         >
  Compose Render         >
```

Items with `>` navigate to separate screens.

## Animations

| Element | Animation | Duration |
|---------|-----------|----------|
| Tab switch | Content crossfade | `200ms` |
| Button press | Scale to `0.98` | `100ms` |
| Button release | Scale to `1.0` | `150ms` |
| Screen entrance | Staggered fade-in | `300ms` |

No parallax, bouncy effects, or celebration animations. Quietly confident.

## What to Remove

- All card borders and backgrounds
- Info banner (shake instruction)
- Redundant descriptions on buttons
- Filled icons (use outlined)
- Saturated purple backgrounds
- WarningActionCard red backgrounds
- FeatureActionCard colored backgrounds

## Files to Modify

| File | Changes |
|------|---------|
| `app/.../MainActivity.kt` | Replace all composables with new design |
| `app/.../theme/Color.kt` | Update color palette |
| `app/.../theme/Theme.kt` | Simplify theme, remove dynamic colors or keep as option |

## Success Criteria

- First impression feels premium and professional
- Clear visual hierarchy without borders everywhere
- Test tools accessible but not dominant
- Consistent with minimal/refined aesthetic
- Maintains all existing functionality

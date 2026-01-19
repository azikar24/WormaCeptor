# Wormaceptor Design System

This document defines the visual language and scalability principles for the Wormaceptor design system. It is built to support a premium, developer-focused network inspection experience on Android.

## Core Principles

1.  **Precision & Information Density:** As a debugging tool, clarity and density are prioritized. Typography and spacing must ensure that complex network data is readable.
2.  **Context-Aware:** Semantic tokens ensure that debugging states (Success, Error, Pending) are instantly recognizable.
3.  **Adaptive:** Built-in support for Light and Dark modes with specialized palettes for high-contrast debugging environments.
4.  **Premium Feel:** Subtle motion and refined shadows create a high-quality "interceptor" aesthetic.

## 1. Tokens Overview

The system uses a three-tier token architecture (see [DESIGN_TOKENS.json](file:///c:/Users/kroos/Documents/Dev/Projects/WormaCeptor/DESIGN_TOKENS.json)):
- **Global:** Raw primitive values (colors, spacing, etc).
- **Semantic:** Intent-based aliases (Light/Dark mode mapping).
- **Component (Future):** Specific implementation values.

## 2. Accessibility Baseline

Wormaceptor adheres to **WCAG 2.1 AA** standards at a minimum.

### Color Contrast
- **Text:** All primary text must maintain a contrast ratio of at least 4.5:1 against its background.
- **Interactive Elements:** Buttons and form fields must maintain a 3:1 ratio for visual identification.
- **Color Blindness:** Critical information (e.g., Request Error) must be conveyed via both color (Red) and iconography (Warning Icon).

### Touch Targets
- **Minimum Size:** All interactive elements must be at least **48x48dp** to ensure reliable interaction on mobile devices.
- **Clearance:** Elements should have at least 8dp of spacing between them.

## 3. Dark/Light Strategy

Wormaceptor uses a **Semantic Switch** approach:
- Logic in the application should refer to `semantic.text.primary`, not `global.color.neutral.900`.
- Dark mode is not just inverted light mode; backgrounds use a deeper, less-saturated neutral palette to reduce eye strain during long debugging sessions.

## 4. Motion Rules

Motion in Wormaceptor should be functional, not decorative.

| Intent      | Duration         | Easing       | Example                              |
| :---------- | :--------------- | :----------- | :----------------------------------- |
| Interaction | `fast` (150ms)   | `standard`   | Button press, hover state.           |
| Structural  | `normal` (250ms) | `decelerate` | List item expansion, drawer opening. |
| Narrative   | `slow` (400ms)   | `standard`   | Full-screen transitions.             |

- **Avoid:** Excessive bouncing or overshoot.
- **Prioritize:** Continuity of state (e.g., a "Loading" spinner fading out as "Success" state appears).

## 5. Typography Scale

Scale is based on a **Major Third (1.25)** ratio for hierarchy, optimized for mobile screens.

- **Display:** Large headers for overview screens (`30px`).
- **Body:** Standard reading text (`14px` or `16px`).
- **Detail:** Metadata and tech labels (`12px`, Mono).

## 6. Layout & Spacing

Based on a **4px baseline grid**.
- **Page Padding:** `16px` (global.spacing.4).
- **Vertical Spacing:** `8px` or `12px` between list items.
- **Gutter:** `16px` for grid layouts.

# Wormaceptor UX Redesign Plan

This document outlines a redesign of the Wormaceptor UX, shifting from a technical data viewer to a task-oriented debugging companion.

## 1. Current UX Friction Points

| Friction Point           | Description                                                                    | Cognitive Loading |
| :----------------------- | :----------------------------------------------------------------------------- | :---------------- |
| **Information Overload** | Flat list of all requests (200s, 404s, 500s) makes finding "the needle" hard.  | High              |
| **Mechanical Triage**    | Status codes and methods are visual noise rather than semantic indicators.     | Medium            |
| **Context Switching**    | Moving between app -> notification -> detail screen lacks flow continuity.     | High              |
| **Deep Diving**          | Inspecting large JSON payloads requires manual scrolling and mental mapping.   | Very High         |
| **Export Complexity**    | Sharing a specific request requires several taps and "Send" intent selections. | Medium            |

---

## 2. User Personas & Intent

### A. The "Bug Hunter" (Triage)
*   **Intent:** "Why did my last action fail?"
*   **Need:** Immediate access to the most recent error or specific endpoint.

### B. The "Integrator" (Verification)
*   **Intent:** "Is the payload I sent/received exactly what I expect?"
*   **Need:** Detailed comparison, syntax highlighting, and content-aware formatting.

### C. The "DevOps/Backend Collaborator" (Sharing)
*   **Intent:** "Here is the exact state of the failure for the backend dev."
*   **Need:** Zero-friction exporting (cURL, HAR) and session snapshots.

---

## 3. New Task-Based Flows

### Flow A: Instant Triage (Notification to Detail)
1.  **Event:** A network error occurs in the background.
2.  **Intent-Driven UI:** Notification shows specific error details (401 Unauthorized) instead of generic "New Transaction".
3.  **Action:** Tapping notification opens the **Transaction Detail** directly with the "Response" tab pre-selected and "Headers" highlighted (where the error likely lies).

### Flow B: Comparison & Verification
1.  **Intent:** Verify if a fix worked.
2.  **Action:** Long-press a transaction in the list to "Pin".
3.  **Action:** Drag-and-drop a new transaction onto a pinned one to trigger a "Diff Viewer".
4.  **Outcome:** Side-by-side comparison of Request/Response bodies.

### Flow C: Rapid Collaboration
1.  **Action:** Swipe left on any transaction in the list.
2.  **Action:** Select "Copy cURL" or "Share to Slack/Jira" (via Plugin).
3.  **Outcome:** The intent is fulfilled without leaving the list context.

---

## 4. Reducing Cognitive Load

### Semantic Visual Hierarchy
*   **Status-Driven Coloring:** Background subtle tinting based on status (Red: 5xx/4xx, Green: 2xx).
*   **Timeline Compression:** Grouping repeated identical requests (e.g., polling) into a single "Cluster" with a counter.
*   **Contextual Previews:**
    *   **JSON:** Automatic folding of large blocks.
    *   **Images:** Small thumbnails in the list view.
    *   **Auth:** Dedicated "Auth Header" badge if a token is present.

### Smart Search & Filtering
*   **Dynamic Chips:** "Errors Only", "POST/PUT Only", "Last 5 Minutes".
*   **Search suggestions:** Autocomplete based on known domains/paths.

---

## 5. Power User Features

### Command Palette (for Emulator/Physical Keyboard)
*   `Ctrl + F`: Global search.
*   `Ctrl + D`: Clear history.
*   `Arrow Keys`: Navigate list/tabs.
*   `Ctrl + C`: Copy cURL of focused item.

### Regex & Advanced Filtering
*   Support for regex in search bars.
*   "Watchlists": Highlighting specific URLs or headers when they appear.

### Custom Export Templates
*   Ability to define custom formats for sharing (e.g., "Standard Bug Report Format").

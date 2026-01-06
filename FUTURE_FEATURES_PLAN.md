# WormaCeptor Future Features Roadmap

This document outlines high-level features and functional additions planned for future releases of WormaCeptor.

## 1. Request & Response Inspection
- [ ] **Payload Formatting:** 
    - [ ] Auto-detect and pretty-print JSON, XML, and HTML.
    - [ ] Support for **Protobuf** and **FlatBuffers** decoding.
- [ ] **Image & Media Preview:** Built-in viewer for images, PDFs, and video metadata returned in responses.
- [ ] **Advanced Search:** Global search across URL, headers, and body content with Regex support.

## 2. Testing & Mocking
- [ ] **Response Mocking:** A built-in UI to define "Mock Rules" (e.g., if URL contains `X`, return status `500` and JSON `Y`).
- [ ] **Request Throttling:** Simulate slow network conditions (3G, Edge) specifically for the intercepted traffic.
- [ ] **Header Modification:** On-the-fly modification of request headers (e.g., swapping Auth tokens) without rebuilding the app.

## 3. Crash & Error Reporting
- [ ] **Integrated Logcat:** Attach the last 200 lines of Logcat to every crash report automatically.
- [ ] **Device Snapshot:** Include detailed device state (Battery level, RAM usage, Connectivity type, Disk space) at the time of crash.
- [ ] **Grouping:** Intelligent grouping of identical crashes to reduce noise in the UI.

## 4. Collaboration & Integration
- [ ] **One-Click Share:** Export transactions as `.har` files or formatted text directly to Slack, Jira, or Email via the system share sheet.
- [ ] **Web Dashboard:** An optional desktop-based web UI (via a local server on the device) for viewing logs on a larger screen during development.
- [ ] **Custom Backend Upload:** Ability to sync logs to a self-hosted WormaCeptor server for remote debugging on QA builds.

## 5. UI & Accessibility
- [ ] **Floating Bubble UI:** An optional overlay bubble (like Facebook Chat Heads) for quick access to logs without leaving the app.
- [ ] **Custom Themes:** Support for user-defined color schemes and font sizes for better readability.
- [ ] **Landscape Support:** Optimized layouts for tablets and landscape mode.

## 6. Multi-Platform & Extensibility
- [ ] **Ktor Interceptor:** Dedicated module for Ktor Multiplatform clients.
- [ ] **KMP Support:** Move core logic to Kotlin Multiplatform to support iOS/Desktop in the future.
- [ ] **Plugin System:** Allow third-party developers to write "Parsers" for custom binary protocols.

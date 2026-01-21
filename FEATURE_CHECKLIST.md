# WormaCeptor Feature Checklist

## Completed Features

| Feature | Category |
|---------|----------|
| Main API & Entry Point (builder, setup/show/hide) | Core Infrastructure |
| Window Management (overlay, navigation stack) | Core Infrastructure |
| Interception Framework (HTTP) | Core Infrastructure |
| HTTP Request Interception (full capture with headers/body) | Network Monitoring |
| Dark Mode Toggle (theme system) | UI Inspection |
| Package Manager (Gradle/Maven) | Build System |
| Manual Integration | Build System |
| CI/CD Integration (debug build config) | Build System |

## Partial

| Feature | Category | Notes |
|---------|----------|-------|
| Feature Toggle System | Core Infrastructure | Filters exist, no tab hiding |
| Custom Extensions | App Utilities | Extensions map exists |

---

## P0 - Must Have (Core Value)

| Feature | Category | Rationale |
|---------|----------|-----------|
| Floating UI System (draggable button, edge snap) | Core Infrastructure | Essential UX - developers need easy access |
| Feature Toggle System (complete tab hiding) | Core Infrastructure | Allows customization of what's visible |
| SharedPreferences Inspector | Resource Inspection | Most common debugging need |
| Console Log Capture | App Utilities | Fundamental debugging tool |
| Device Information Display | App Utilities | Basic context for debugging |

## P1 - Should Have (High Value)

| Feature | Category | Rationale |
|---------|----------|-----------|
| SQLite Database Browser | Resource Inspection | Very common debugging need |
| File Browser | Resource Inspection | Frequently needed for data inspection |
| Memory Monitoring | Performance | Catches common issues |
| WebSocket Monitoring | Network Monitoring | Increasingly common in apps |
| HTTP Cookies Manager | Resource Inspection | Often needed for auth debugging |

## P2 - Good to Have (Differentiators)

| Feature | Category | Rationale |
|---------|----------|-----------|
| FPS Monitoring | Performance | Useful for UI performance |
| CPU Monitoring | Performance | Helpful for optimization |
| Touch Visualization | UI Inspection | Good for QA/demos |
| Colorized View Borders | UI Inspection | Layout debugging |
| Location Simulation | App Utilities | Useful for location-based apps |
| Push Notification Simulator | App Utilities | Helps test notification flows |

## P3 - Nice to Have (Advanced)

| Feature | Category | Rationale |
|---------|----------|-----------|
| 3D View Hierarchy Inspector | UI Inspection | Power user feature |
| Memory Leak Detection | Performance | Complex to implement well |
| Main Thread Violation Detection | Performance | StrictMode already exists |
| WebView Network Monitoring | Network Monitoring | Niche use case |
| Response Encryption/Decryption | Network Monitoring | Specialized need |
| Grid Overlay System | UI Inspection | Design-focused |
| UI Measurement Tool | UI Inspection | Design-focused |
| Secure Storage Viewer | Resource Inspection | Security implications |
| Compose Render Tracking | UI Inspection | Compose-specific |
| Network Rate Limiting | Network Monitoring | Edge case testing |
| Push Token Management | App Utilities | Specialized |
| Loaded Libraries Inspector | App Utilities | Niche debugging |
| Interception Framework (views, touch, location) | Core Infrastructure | Advanced interception |

---

## Recommended Implementation Phases

**Phase 1**: Floating UI + SharedPreferences + Console Logs + Device Info
**Phase 2**: Feature Toggles (complete) + SQLite + File Browser
**Phase 3**: Memory/FPS Monitoring + WebSocket + Cookies
**Phase 4**: UI Inspection tools + Location/Push simulation
**Phase 5**: Advanced features as needed

---

## Summary

| Priority | Count |
|----------|-------|
| Completed | 8 |
| Partial | 2 |
| P0 - Must Have | 5 |
| P1 - Should Have | 5 |
| P2 - Good to Have | 6 |
| P3 - Nice to Have | 13 |
| **Total Pending** | **29** |

# WormaCeptor Features

## Network Inspection

### Network Transaction Viewer
View HTTP/HTTPS requests in real-time with filtering, search, and detailed inspection.
- **Test**: Make API calls from host app, open WormaCeptor, verify transactions appear in list with correct method/status

### WebSocket Monitoring
Monitor WebSocket connections and messages.
- **Test**: Connect to a WebSocket endpoint, check connection appears in WebSocket tab, send/receive messages

### Request/Response Body Parsing
JSON/XML tree view with syntax highlighting, multipart form data, images, PDFs.
- **Test**: Make a JSON API call, tap transaction, verify JSON is syntax-highlighted and expandable

---

## Performance Monitoring

### FPS Monitor
Real-time FPS tracking with history.
- **Test**: Tools > FPS Monitor > Start, scroll in host app, verify FPS values update

### Memory Monitor
Heap memory tracking with charts and GC monitoring.
- **Test**: Tools > Memory Monitor, observe memory usage, trigger GC from host app

### CPU Monitor
CPU usage tracking per core.
- **Test**: Tools > CPU Monitor, run intensive operation, verify CPU usage increases

### Compose Render Tracker
Track Compose recomposition counts and identify excessive recompositions.
- **Test**: Tools > Compose Render, interact with Compose UI, check recomposition stats

### Leak Detection
Memory leak detection and analysis.
- **Test**: Tools > Leak Detection, navigate host app, check for detected leaks

### Thread Violation Detection
Detect main thread violations and ANR warnings.
- **Test**: Tools > Thread Violations, perform disk I/O on main thread, verify violation appears

### Performance Overlay
Draggable overlay showing FPS/memory/CPU in real-time.
- **Test**: Tools > toggle Performance Overlay, verify overlay appears and can be dragged

---

## Data & Storage Inspection

### SharedPreferences Inspector
Browse and edit SharedPreferences.
- **Test**: Tools > Preferences, verify prefs files listed, tap to view key-values

### Database Browser
SQLite database inspection with query execution.
- **Test**: Tools > Databases, select DB, browse tables, run custom SQL query

### File Browser
Navigate app file system with file viewing.
- **Test**: Tools > File Browser, navigate directories, open text/image files

### Secure Storage Inspector
View encrypted SharedPreferences.
- **Test**: Tools > Secure Storage, verify encrypted prefs are decrypted and displayed

### Cookies Manager
View and manage HTTP cookies.
- **Test**: Tools > Cookies, make authenticated request, verify cookies appear

### WebView Monitor
Monitor WebView activity and JS bridges.
- **Test**: Load WebView in host app, check WebView Monitor for activity

---

## Simulation & Testing

### Location Simulator
Mock device GPS location.
- **Test**: Tools > Location, set custom lat/lng, verify host app receives mocked location

### Push Notification Simulator
Send test push notifications.
- **Test**: Tools > Push Simulator, configure notification, send, verify notification appears

### Push Token Manager
View and manage push tokens.
- **Test**: Tools > Push Token, view current token, request refresh

---

## Network Control

### Rate Limiter
Control request throttling.
- **Test**: Tools > Rate Limit, enable with delay, make API calls, verify delayed responses

### Interception Framework
Create rules to block, mock, delay, or modify requests/responses.
- **Test**: Tools > Interception, create rule to mock response, make matching request, verify mocked data

---

## Visual Debugging

### Touch Visualization
Display touch points with ripple effects.
- **Test**: Tools > Touch Visualization > Enable, tap screen, verify touch indicators appear

### View Borders
Highlight view boundaries with colored borders.
- **Test**: Tools > View Borders > Enable, verify all views show border outlines

### Grid Overlay
Display configurable grid for layout alignment.
- **Test**: Tools > Grid Overlay, enable grid, adjust size/opacity

### Measurement Tool
Measure distances in dp/px with angle display.
- **Test**: Tools > Measurement, draw line between elements, verify distance shown

### View Hierarchy Inspector
Navigate and inspect view tree.
- **Test**: Tools > View Hierarchy, verify tree displays, tap nodes to see properties

---

## Utility & Analysis

### Console Logs
View app logs with level filtering.
- **Test**: Tools > Logs, filter by log level, search for specific text

### Device Info
Display device details (model, OS, screen).
- **Test**: Tools > Device Info, verify accurate device information

### Loaded Libraries
List all loaded native libraries.
- **Test**: Tools > Loaded Libraries, verify .so files listed

### Crypto Tool
Encrypt/decrypt with multiple algorithms (AES, RSA).
- **Test**: Tools > Crypto, select algorithm, input text, encrypt, then decrypt to verify

### Crash Reporting
Capture and display app crashes with stack traces.
- **Test**: Trigger crash in host app, open WormaCeptor, verify crash appears with full trace

---

## Core Features

### Shake-to-Open
Shake device to launch WormaCeptor.
- **Test**: Enable shake detection, shake device, verify WormaCeptor opens

### Favorites System
Pin frequently used tools for quick access.
- **Test**: Long-press a tool, add to favorites, verify appears in favorites strip

### Export/Share
Export transactions and crash data.
- **Test**: Select transaction(s), tap export, verify file generated

### Bulk Actions
Multi-select transactions for delete/export.
- **Test**: Long-press transaction, select multiple, use bulk action bar

### Search & Filtering
Filter transactions by method, status, URL.
- **Test**: Use filter bar, apply method filter, verify list updates

### Keyboard Shortcuts
Navigate with keyboard (external keyboard/desktop).
- **Test**: Connect keyboard, use Ctrl+F for search, arrow keys for navigation

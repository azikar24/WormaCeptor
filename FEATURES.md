# WormaCeptor Features - Manual Test Checklist

## Network Inspection

### Network Transaction Viewer
View HTTP/HTTPS requests in real-time with filtering, search, and detailed inspection.
- [ ] Make API calls from host app
- [ ] Open WormaCeptor and verify transactions appear in list
- [ ] Verify correct method/status displayed

### WebSocket Monitoring
Monitor WebSocket connections and messages.
- [ ] Connect to a WebSocket endpoint
- [ ] Check connection appears in WebSocket tab
- [ ] Send/receive messages and verify they appear

### Request/Response Body Parsing
JSON/XML tree view with syntax highlighting, multipart form data, images, PDFs.
- [ ] Make a JSON API call
- [ ] Tap transaction and verify JSON is syntax-highlighted
- [ ] Verify JSON is expandable/collapsible
- [ ] Test XML response parsing
- [ ] Test image response display
- [ ] Test PDF response display

---

## Performance Monitoring

### FPS Monitor
Real-time FPS tracking with history.
- [ ] Navigate to Tools > FPS Monitor
- [ ] Start monitoring
- [ ] Scroll in host app
- [ ] Verify FPS values update in real-time

### Memory Monitor
Heap memory tracking with charts and GC monitoring.
- [ ] Navigate to Tools > Memory Monitor
- [ ] Observe memory usage chart
- [ ] Trigger GC from host app
- [ ] Verify GC event is detected

### CPU Monitor
CPU usage tracking per core.
- [ ] Navigate to Tools > CPU Monitor
- [ ] Run intensive operation in host app
- [ ] Verify CPU usage increases

### Compose Render Tracker
Track Compose recomposition counts and identify excessive recompositions.
- [ ] Navigate to Tools > Compose Render
- [ ] Interact with Compose UI in host app
- [ ] Check recomposition stats are recorded

### Leak Detection
Memory leak detection and analysis.
- [ ] Navigate to Tools > Leak Detection
- [ ] Navigate around host app to potentially create leaks
- [ ] Check for detected leaks

### Thread Violation Detection
Detect main thread violations and ANR warnings.
- [ ] Navigate to Tools > Thread Violations
- [ ] Perform disk I/O on main thread in host app
- [ ] Verify violation appears in list

### Performance Overlay
Draggable overlay showing FPS/memory/CPU in real-time.
- [ ] Navigate to Tools > toggle Performance Overlay
- [ ] Verify overlay appears on screen
- [ ] Drag overlay to different positions
- [ ] Verify metrics update in real-time

---

## Data & Storage Inspection

### SharedPreferences Inspector
Browse and edit SharedPreferences.
- [ ] Navigate to Tools > Preferences
- [ ] Verify prefs files are listed
- [ ] Tap to view key-values
- [ ] Edit a value and verify it persists

### Database Browser
SQLite database inspection with query execution.
- [ ] Navigate to Tools > Databases
- [ ] Select a database
- [ ] Browse tables
- [ ] Run custom SQL query

### File Browser
Navigate app file system with file viewing.
- [ ] Navigate to Tools > File Browser
- [ ] Navigate through directories
- [ ] Open a text file
- [ ] Open an image file

### Secure Storage Inspector
View encrypted SharedPreferences.
- [ ] Navigate to Tools > Secure Storage
- [ ] Verify encrypted prefs are listed
- [ ] Verify values are decrypted and displayed

### Cookies Manager
View and manage HTTP cookies.
- [ ] Navigate to Tools > Cookies
- [ ] Make authenticated request from host app
- [ ] Verify cookies appear in manager

### WebView Monitor
Monitor WebView activity and JS bridges.
- [ ] Load WebView in host app
- [ ] Check WebView Monitor for activity
- [ ] Verify JS bridge calls are logged

---

## Simulation & Testing

### Location Simulator
Mock device GPS location.
- [ ] Navigate to Tools > Location
- [ ] Set custom latitude/longitude
- [ ] Verify host app receives mocked location

### Push Notification Simulator
Send test push notifications.
- [ ] Navigate to Tools > Push Simulator
- [ ] Configure notification content
- [ ] Send notification
- [ ] Verify notification appears on device

### Push Token Manager
View and manage push tokens.
- [ ] Navigate to Tools > Push Token
- [ ] View current token
- [ ] Request token refresh

---

## Network Control

### Rate Limiter
Control request throttling.
- [ ] Navigate to Tools > Rate Limit
- [ ] Enable with delay value
- [ ] Make API calls from host app
- [ ] Verify responses are delayed

### Interception Framework
Create rules to block, mock, delay, or modify requests/responses.
- [ ] Navigate to Tools > Interception
- [ ] Create rule to mock a response
- [ ] Make matching request from host app
- [ ] Verify mocked data is returned
- [ ] Test blocking a request
- [ ] Test delaying a request
- [ ] Test modifying request headers

---

## Visual Debugging

### Touch Visualization
Display touch points with ripple effects.
- [ ] Navigate to Tools > Touch Visualization
- [ ] Enable touch visualization
- [ ] Tap screen in various places
- [ ] Verify touch indicators appear with ripple

### View Borders
Highlight view boundaries with colored borders.
- [ ] Navigate to Tools > View Borders
- [ ] Enable view borders
- [ ] Verify all views show border outlines

### Grid Overlay
Display configurable grid for layout alignment.
- [ ] Navigate to Tools > Grid Overlay
- [ ] Enable grid
- [ ] Adjust grid size
- [ ] Adjust grid opacity

### Measurement Tool
Measure distances in dp/px with angle display.
- [ ] Navigate to Tools > Measurement
- [ ] Draw line between two elements
- [ ] Verify distance is shown in dp and px
- [ ] Verify angle is displayed

### View Hierarchy Inspector
Navigate and inspect view tree.
- [ ] Navigate to Tools > View Hierarchy
- [ ] Verify tree displays correctly
- [ ] Tap nodes to see properties
- [ ] Verify properties are accurate

---

## Utility & Analysis

### Console Logs
View app logs with level filtering.
- [ ] Navigate to Tools > Logs
- [ ] Filter by log level (DEBUG, INFO, WARN, ERROR)
- [ ] Search for specific text
- [ ] Verify logs update in real-time

### Device Info
Display device details (model, OS, screen).
- [ ] Navigate to Tools > Device Info
- [ ] Verify device model is correct
- [ ] Verify OS version is correct
- [ ] Verify screen info is accurate

### Loaded Libraries
List all loaded native libraries.
- [ ] Navigate to Tools > Loaded Libraries
- [ ] Verify .so files are listed
- [ ] Check library paths are displayed

### Crypto Tool
Encrypt/decrypt with multiple algorithms (AES, RSA).
- [ ] Navigate to Tools > Crypto
- [ ] Select AES algorithm
- [ ] Input text and encrypt
- [ ] Decrypt and verify original text
- [ ] Repeat with RSA algorithm

### Crash Reporting
Capture and display app crashes with stack traces.
- [ ] Trigger crash in host app
- [ ] Open WormaCeptor
- [ ] Verify crash appears in list
- [ ] Verify full stack trace is displayed

---

## Core Features

### Shake-to-Open
Shake device to launch WormaCeptor.
- [ ] Enable shake detection in settings
- [ ] Shake device
- [ ] Verify WormaCeptor opens

### Favorites System
Pin frequently used tools for quick access.
- [ ] Long-press a tool
- [ ] Add to favorites
- [ ] Verify tool appears in favorites strip
- [ ] Remove from favorites

### Export/Share
Export transactions and crash data.
- [ ] Select transaction(s)
- [ ] Tap export
- [ ] Verify file is generated
- [ ] Share exported file

### Bulk Actions
Multi-select transactions for delete/export.
- [ ] Long-press a transaction to enter selection mode
- [ ] Select multiple transactions
- [ ] Use bulk delete action
- [ ] Use bulk export action

### Search & Filtering
Filter transactions by method, status, URL.
- [ ] Use filter bar
- [ ] Apply method filter (GET, POST, etc.)
- [ ] Apply status filter (2xx, 4xx, 5xx)
- [ ] Search by URL
- [ ] Verify list updates correctly

### Keyboard Shortcuts
Navigate with keyboard (external keyboard/desktop).
- [ ] Connect external keyboard
- [ ] Use Ctrl+F for search
- [ ] Use arrow keys for navigation
- [ ] Test other shortcuts

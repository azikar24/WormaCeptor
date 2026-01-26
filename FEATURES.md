# WormaCeptor Features - Manual Test Checklist

### Compose Render Tracker
Track Compose recomposition counts and identify excessive recompositions.
- [ ] Navigate to Tools > Compose Render
- [ ] Interact with Compose UI in host app
- [ ] Check recomposition stats are recorded

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

### View Hierarchy Inspector
Navigate and inspect view tree.
- [ ] Navigate to Tools > View Hierarchy
- [ ] Verify tree displays correctly
- [ ] Tap nodes to see properties
- [ ] Verify properties are accurate

---

## Utility & Analysis
### Dependencies Inspector
Detect Java/Kotlin libraries with version detection.
- [ ] Navigate to Tools > Dependencies
- [ ] Verify common libraries are detected (OkHttp, Retrofit, Koin, etc.)
- [ ] Check versions are displayed when detectable
- [ ] Filter by category (Networking, DI, UI, etc.)
- [ ] Toggle "Show only versioned" filter
- [ ] Search for a specific library
- [ ] Tap a library to view details (Maven coordinate, detection method)
- [ ] Verify website link opens in browser

### Crypto Tool
Encrypt/decrypt with multiple algorithms (AES, RSA).
- fix design

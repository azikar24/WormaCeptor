# WormaCeptor Improvement Plan

This document outlines suggested improvements for the WormaCeptor library to enhance performance, maintainability, and user experience.

## 2. Performance & Reliability
- [ ] **R8/ProGuard Rules:** Bundle `consumer-rules.pro` to ensure the library works seamlessly in minified production builds without manual configuration from the user.
- [ ] **Network Latency Breakdown:** Provide detailed metrics for DNS lookup, TLS handshake, and Time to First Byte (TTFB) for every request.
- [ ] **Multi-Process Support:** Ensure correct data capture and UI synchronization in apps that utilize multiple Android processes.

## 3. UI/UX Enhancements (Jetpack Compose)
- [ ] **Search & Filter:** Add robust searching and filtering capabilities for network requests (by status code, method, domain, etc.).
- [ ] **Data Export:** Implement "Export as HAR" or "Export as Text" functionality for easy sharing of network logs.
- [ ] **Dark Mode & Material 3:** Ensure full compatibility with Material 3, including support for Dynamic Color (Monet) on Android 12+.
- [ ] **Notification Permissions:** Refine the permission request flow for notifications on Android 13+ to ensure the interceptor can show its status.
- [ ] **Multi-Language Support (i18n):** Localize the library UI into multiple languages for broader developer adoption.
- [ ] **Tablet & Foldable Optimization:** Implement a list-detail (split-pane) layout specifically for larger screens and foldable devices.

## 4. Feature Additions
- [ ] **Support for Other Clients:** Create modules or adapters for other popular networking libraries like Ktor or Cronet.
- [ ] **GraphQL Support:** Add specialized parsing and display for GraphQL queries and mutations (pretty-printing JSON within the `query` field).
- [ ] **Image Preview:** Add a specialized view for image-based network responses to view thumbnails directly in the log.
- [ ] **Mocking Engine:** Implement a basic mocking mechanism to allow developers to override specific network responses for testing edge cases.
- [ ] **App Metadata Dashboard:** Provide a quick-view screen for package info, versioning, build signatures, and active permissions.
- [ ] **Database & SharedPreferences Inspector:** Allow developers to browse and edit local storage (Room, SQLite, SharedPreferences) directly from the UI.
- [ ] **Deep Link Tester:** Integrate a tool to manually fire and test internal deep links without leaving the inspector.
- [ ] **Binary Hex Viewer:** Add a specialized hex viewer for debugging binary payloads that aren't recognized as text or media.

## 5. Security & Privacy
- [ ] **Improved Redaction:** Expand header redaction to include body field redaction (e.g., masking "password" or "token" fields in JSON payloads).
- [ ] **Encrypted Storage:** Provide an option to use SQLCipher with Room for projects that require encrypted local logs.

## 6. Engineering Excellence
- [ ] **Unit Testing:** Increase test coverage for `WormaCeptorInterceptor` logic, especially the byte-buffer parsing and charset handling.
- [ ] **Benchmarking:** Add Macrobenchmark tests to measure the library's impact on host app startup time and frame drops.
- [ ] **API Documentation:** Add KDoc to all public-facing methods and classes.
- [ ] **CI/CD:** Set up GitHub Actions for automated linting, unit tests, and snapshot publishing.
- [ ] **No-op Validation:** Rigorously test the `:WormaCeptor-no-op` module to ensure zero overhead in production builds.
- [ ] **Accessibility Compliance:** Ensure the UI meets WCAG standards, specifically for TalkBack screen reader support and high-contrast color ratios.

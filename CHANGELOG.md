# Changelog

All notable changes to WormaCeptor will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.2.1] - 2025-03-24

### Added
- MCP (Model Context Protocol) device server for AI-agent access to runtime debugging data
- MCP bridge CLI for connecting AI tools to Android devices via ADB
- 32 MCP tools covering network, diagnostics, performance, storage, and actions
- Real-time streaming support via WebSocket for live debugging data
- Ktor client plugin for native Ktor HTTP client support
- WebSocket monitoring with frame inspection
- WebView monitoring for page loads and sub-resources
- Performance overlay with real-time FPS, Memory, and CPU metrics
- SQLite database browser with custom query execution
- SharedPreferences inspector and editor
- EncryptedSharedPreferences inspector
- File system browser
- Leak detection for Activities and Fragments (LeakCanary integration)
- Thread violation detection (StrictMode)
- Crash reporting with stack traces
- Push notification simulator
- Location simulator for GPS mocking
- Rate limiter with network presets (2G, 3G, 4G, WiFi)
- Crypto toolkit (AES, RSA, hashing, encoding)
- Console logs viewer
- Device info inspector
- Loaded libraries inspector
- Dependencies inspector
- Feature toggle system with 20 toggleable features
- Deep link navigation to every tool (wormaceptor:// scheme)
- Extension provider system for custom metadata
- Data redaction (headers, JSON values, XML values, regex patterns)

### Changed
- Complete rewrite with Clean Architecture (50+ modules)
- 100% Kotlin + Jetpack Compose UI
- Koin dependency injection
- ArchUnit-enforced architecture boundaries

## [1.0.0] - Initial Release

### Added
- Basic HTTP/HTTPS network inspection
- OkHttp interceptor
- Transaction list and detail views

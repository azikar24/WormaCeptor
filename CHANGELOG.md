# Changelog

All notable changes to WormaCeptor will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.3.2](https://github.com/azikar24/WormaCeptor/compare/2.3.1...v2.3.2) (2026-10-06)


### Features

* **app:** add a Seed DataStore test tool ([10a574c](https://github.com/azikar24/WormaCeptor/commit/10a574ca12277017048157fcc60faaf80a75e30a))
* **demo:** add sample services and stress tools to exercise inspectors ([93b21f6](https://github.com/azikar24/WormaCeptor/commit/93b21f656c3f9f63718e548b2b6195c3a2ca6a1a))
* neutral-first redesign, host-safety fixes, Copy URL and Add to Mock ([cba1cb1](https://github.com/azikar24/WormaCeptor/commit/cba1cb1eef24f0f9e4e0ab1d54731d2ee039f442))
* **preferences:** add a read-only DataStore inspector tool ([9401b12](https://github.com/azikar24/WormaCeptor/commit/9401b128caee46d91969814ee76d4c33a640a5b2)), closes [#16](https://github.com/azikar24/WormaCeptor/issues/16)
* **ui:** neutral-first design system refresh ([0db162e](https://github.com/azikar24/WormaCeptor/commit/0db162e5db705ac7923847d6fc242feadbfaa9fa))
* **viewer:** copy URL, add to mock, and transaction viewer fixes ([93a3d8f](https://github.com/azikar24/WormaCeptor/commit/93a3d8f532d40bc023b973cde3b5a541cf04a069))


### Bug Fixes

* **api:** address pre-merge review of response capture and engines ([a3c5676](https://github.com/azikar24/WormaCeptor/commit/a3c56764086052539bdf19628ac6f5d917e79e46))
* **api:** stream response capture and keep debug parts out of release ([f76a5bf](https://github.com/azikar24/WormaCeptor/commit/f76a5bf3f505ddb6e814cc6bde5fb73369eb7c39))
* **app:** demo app permissions, insets and test screens ([9325bd7](https://github.com/azikar24/WormaCeptor/commit/9325bd7167e4ae8ffa85447cfede9128dfe9c490))
* **engine:** correct capture, monitoring and storage engine bugs ([80c3e4d](https://github.com/azikar24/WormaCeptor/commit/80c3e4dce9d9e6a11af83e3e7a6d2db9437e8318))
* **features:** tool screen bug fixes and color cleanup ([249a57d](https://github.com/azikar24/WormaCeptor/commit/249a57dea77b98c19c23d3c94529bcfb5b36f96f))
* **mockrules:** stop double keyboard padding in the rule editor ([6e618d5](https://github.com/azikar24/WormaCeptor/commit/6e618d5c0a7939e9af8f080386d4894840a9adbc))
* **preferences:** load lists without waiting for the search debounce ([26eefe7](https://github.com/azikar24/WormaCeptor/commit/26eefe7703db7ca0c8fb9ea1bad20af30e24324f))
* **release:** let release-please bump the published VERSION_NAME ([deab6be](https://github.com/azikar24/WormaCeptor/commit/deab6beda69e3b286c81ef5bc94af5243ca98def))
* **ui:** replace ExposedDropdownMenuBox to survive Material3 1.4 ([8d922d7](https://github.com/azikar24/WormaCeptor/commit/8d922d730e0210b497221c1521047c06ccfe09bf)), closes [#17](https://github.com/azikar24/WormaCeptor/issues/17)
* **viewer:** address pre-merge review of viewer, PDF and mock editor ([4187789](https://github.com/azikar24/WormaCeptor/commit/41877892c120dcba1428336d68512b570447d02f))
* **viewer:** respect side and bottom system bar insets ([445fdde](https://github.com/azikar24/WormaCeptor/commit/445fddeb725c6a89eaa1d4ecea235dff89cc8d55)), closes [#18](https://github.com/azikar24/WormaCeptor/issues/18)


### CI/CD

* add dependabot config ([8da5925](https://github.com/azikar24/WormaCeptor/commit/8da59251d3493dbe0b6d03cb458673f38023ec05))
* auto-release pipeline on merge to master ([027592a](https://github.com/azikar24/WormaCeptor/commit/027592a124a6a84c658e56d8fa83aeb7a9de22c9))
* auto-release pipeline on merge to master ([683bba0](https://github.com/azikar24/WormaCeptor/commit/683bba0d5ec7f26ab470aad22e4e51f62acdf7d9))
* **coverage:** drop koverHtmlReport from CI to avoid empty-module crash ([9ffd0aa](https://github.com/azikar24/WormaCeptor/commit/9ffd0aa9be78311c16d7ea977382c6cf55983a8f))


### Chores

* release 2.3.2 ([26f192f](https://github.com/azikar24/WormaCeptor/commit/26f192ffa33d7e7b4c7ad85042000c9610af8044))

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

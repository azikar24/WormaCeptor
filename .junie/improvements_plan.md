# WormaCeptor Improvements Plan

This document outlines potential improvements and feature additions for the WormaCeptor library.

## 1. Technical Debt & Quality Assurance
- **Modernizing Concurrency** [DONE]: 
    - Replaced `ThreadPoolExecutor` usages in `WormaCeptor`, `NetworkTransactionViewModel`, and `CrashTransactionViewModel` with Kotlin Coroutines for better structured concurrency and consistency.
- **Dependency Injection** [DONE]: 
    - Introduced **Koin** for dependency injection to make components more testable and decoupled.
- **Unit Testing** [IN PROGRESS]:
    - Adding comprehensive unit tests for `WormaCeptorInterceptor`, `RetentionManager`, and DAOs.

## 2. Feature Enhancements
- **Search & Filtering Improvements**:
    - Added search and delete for crashes list.
    - [TODO] Add filtering by HTTP method (GET, POST, etc.) and response status code (e.g., 2xx, 4xx, 5xx).
    - [TODO] Support regex search in request/response bodies.
- **Payload Visualization**:
    - [DONE] Optimized search in `PayloadScreen` for better performance and UI state management.
    - [TODO] Better formatting/pretty-print for JSON, XML, and HTML bodies in `PayloadScreen`.
    - [TODO] Image preview for image-based responses.
- **Exporting Data**:
    - [TODO] Add functionality to export transactions as HAR (HTTP Archive) files or cURL commands.
    - [TODO] Allow sharing full crash logs as text files.
- **Crash Logging Enhancements**:
    - [TODO] Capture more device metadata (Device model, Android version, Screen density) along with crashes.
    - [TODO] Support for custom log messages that can be attached to the next crash.

## 3. UI/UX Improvements
- **Theme Support**: 
    - [TODO] Better support for Dynamic Color (Material You) on Android 12+.
    - [TODO] Ensure full accessibility support (TalkBack, sufficient contrast).
- **Navigation**:
    - [DONE] Improved transitions between screens in the Compose UI (Sliding transitions).
    - [DONE] Fixed toolbar staying static for all screens by moving it to `HomeScreen`'s `Scaffold`.
    - [TODO] Add a "Go to top" button for long lists.
- **Performance**:
    - [DONE] Optimized list rendering for very large datasets in `NetworkList` and `CrashesList` using Paging3 keys and lazy item access.

## 4. Documentation & Developer Experience
- **API Documentation**: 
    - [TODO] Generate KDoc for all public APIs.
- **Sample App**: 
    - [TODO] Expand the `app` module to demonstrate more complex scenarios (e.g., multiple OkHttp clients, custom redaction rules).
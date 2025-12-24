# Project Guidelines

WormaCeptor is an Android library designed for debugging and monitoring network requests and application crashes. It provides a user interface to inspect network traffic and crash logs directly on the device.

## Project Structure

The project is organized into several modules:

- **WormaCeptor**: The main module containing the core logic, including the `WormaCeptorInterceptor` for OkHttp and the Jetpack Compose-based UI for viewing transactions.
- **WormaCeptor-imdb**: Provides an in-memory database implementation for storing transactions during a single session.
- **WormaCeptor-persistence**: Provides a Room-based persistent storage implementation for transactions.
- **WormaCeptor-no-op**: A no-op (no operation) version of the library to be used in release builds to minimize binary size and ensure no debugging code is included.
- **app**: A sample Android application that demonstrates how to integrate and use WormaCeptor.

## Key Features

- **Network Interception**: Automatically logs OkHttp requests and responses, including headers and bodies.
- **Crash Logging**: Captures uncaught exceptions and stores stack traces for later inspection.
- **On-Device UI**: A built-in UI to browse and search through network and crash logs.
- **Shake to Open**: Option to open the WormaCeptor UI by shaking the device.
- **Notifications**: Optional notifications for new network transactions.

## Development Guidelines

### Code Style
- Follow standard Kotlin coding conventions.
- Use Jetpack Compose for all new UI components.
- Maintain consistency with existing naming conventions (e.g., `WormaCeptor` prefix for core classes).

### Testing
- While automated tests are not strictly required for every change, ensure that any changes to the interceptor or storage logic are verified using the `app` module.
- Run the `app` to manually verify UI changes and integration.

### Build
- Ensure all modules compile before submitting changes.
- Use the Gradle wrapper: `./gradlew assembleDebug`.

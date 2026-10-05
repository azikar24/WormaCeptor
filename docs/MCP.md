# WormaCeptor MCP Integration

WormaCeptor exposes a [Model Context Protocol](https://modelcontextprotocol.io/) (MCP) server that lets AI agents and development tools inspect a running Android app in real time. The system has two parts: a **device server** embedded in your debug build and a **bridge CLI** that translates MCP requests into HTTP calls over ADB.

```
┌─────────────────────┐    stdin/stdout     ┌─────────────────────┐
│   AI Agent / IDE    │◄──── MCP JSON-RPC ──►│   Bridge CLI (JVM)  │
└─────────────────────┘                      └──────────┬──────────┘
                                                        │ HTTP via ADB
                                                        │ port forward
                                             ┌──────────▼──────────┐
                                             │  Device Server      │
                                             │  (Ktor, port 8999)  │
                                             │  inside debug app   │
                                             └─────────────────────┘
```

## Quick Start

### 1. Add the dependency

The device server is already included in the demo app. For your own app:

```kotlin
// app/build.gradle.kts
debugImplementation("com.github.azikar24.WormaCeptor:mcp-device-server:2.2.1")
```

The server auto-starts via a ContentProvider — no code changes needed.

### 1b. Configure via API (optional)

The server works out of the box with defaults. To customize, call `configureMcpServer` before `init`:

```kotlin
// In Application.onCreate()
WormaCeptorApi.configureMcpServer(
    McpConfig(
        port = 9000,                  // default: 8999
        enableAuth = true,            // default: false
        authToken = "my-debug-token", // required when auth enabled
        maxBodySize = 2_097_152L,     // default: 1 MB
    )
)
WormaCeptorApi.init(context = this)
```

To disable auto-start and control the server manually:

```kotlin
WormaCeptorApi.configureMcpServer(McpConfig(enabled = false))
WormaCeptorApi.init(context = this)

// Later, when ready:
WormaCeptorApi.startMcpServer()

// Check status:
WormaCeptorApi.isMcpServerRunning() // true

// Stop:
WormaCeptorApi.stopMcpServer()
```

### 2. Build the bridge

```bash
./gradlew :mcp:bridge:jar
```

This produces `mcp/bridge/build/libs/bridge.jar`.

### 3. Connect

```bash
# Make sure your device/emulator is connected
adb devices

# Run the bridge (it handles port forwarding automatically)
java -jar mcp/bridge/build/libs/bridge.jar
```

The bridge discovers the device, forwards port 8999, verifies the server is reachable, and starts listening for MCP requests on stdin.

### 4. Configure in Claude Code

Add to your project's `.claude/settings.json`:

```json
{
  "mcpServers": {
    "wormaceptor": {
      "command": "java",
      "args": ["-jar", "/path/to/bridge.jar"]
    }
  }
}
```

## Architecture

### Device Server (`mcp/device-server`)

An embedded Ktor/Netty HTTP server that runs inside the debug build on port 8999. It exposes REST endpoints backed by WormaCeptor's core engines.

- **Auto-initializes** via `WormaCeptorServerInitializer` (a ContentProvider that waits for Koin)
- **Debug-only** — included as `debugImplementation`, zero code in release builds
- **Category-gated routes** — disable endpoint groups via `ServerConfig.enabledCategories`
- **Optional auth** — bearer token validation via `AuthPlugin` when `ServerConfig.enableAuth` is true
- **Header redaction** — sensitive headers (`Authorization`, `Cookie`, `X-Api-Key`, etc.) are replaced with `[REDACTED]` in API responses
- **WebSocket streaming** — real-time engine data at `/api/stream`

### Bridge CLI (`mcp/bridge`)

A standalone JVM application that translates MCP protocol (JSON-RPC 2.0 on stdin/stdout) into HTTP calls to the device server.

- **Auto-discovers** connected devices via ADB
- **Port forwarding** — sets up `tcp:8999 → tcp:8999` automatically
- **Reconnection** — exponential backoff (1s → 30s), max 20 attempts
- **Input validation** — path traversal protection, SQL injection prevention, parameter range checks
- **Verbose mode** — `--verbose` logs all MCP requests and responses to stderr

## Bridge CLI Reference

```
Usage: java -jar bridge.jar [options]

Options:
  --port <port>      Device server port (default: 8999)
  --device, -s <id>  Target device serial (required if multiple devices)
  --adb <path>       Path to adb executable (default: adb)
  --token <token>    Bearer token for authentication
  --verbose, -v      Enable verbose request/response logging
  --version          Print version and exit
  --help, -h         Print help
```

## MCP Tools (32 total)

### Network (8 tools)

| Tool | Parameters | Description |
|------|-----------|-------------|
| `list_transactions` | `query?`, `limit?`, `offset?` | List captured HTTP transactions, paginated |
| `get_transaction` | `id` | Get full details of a specific transaction |
| `get_request_body` | `id` | Get the request body of a transaction |
| `get_response_body` | `id` | Get the response body of a transaction |
| `list_websocket_connections` | — | List active WebSocket connections |
| `list_websocket_messages` | `connection_id?`, `limit?` | List WebSocket messages, optionally filtered by connection |
| `get_rate_limit` | — | Get current rate limiting configuration |
| `set_rate_limit` | `enabled?`, `preset?`, `download_kbps?`, `upload_kbps?`, `latency_ms?`, `packet_loss?` | Configure network throttling. Presets: `WIFI`, `GOOD_3G`, `REGULAR_3G`, `SLOW_3G`, `GOOD_2G`, `SLOW_2G`, `EDGE`, `OFFLINE`. `enabled: false` turns it off; `packet_loss` is a percentage (0-100) |

### Diagnostics (6 tools)

| Tool | Parameters | Description |
|------|-----------|-------------|
| `list_crashes` | — | List all captured crash reports |
| `get_crash` | `id` | Get full crash details with stack trace |
| `tail_logs` | `level?`, `tag?`, `limit?` | Retrieve recent log entries. `level` matches exactly (`VERBOSE`...`ASSERT`); `tag` is a case-insensitive substring |
| `list_leaks` | — | List detected memory leaks (LeakCanary integration) |
| `list_violations` | — | List StrictMode and thread policy violations |
| `get_device_info` | — | Get device model, Android version, app info, and system properties |

### Performance (4 tools)

| Tool | Parameters | Description |
|------|-----------|-------------|
| `get_cpu_stats` | `include_history?` | Get CPU usage with optional history |
| `get_memory_stats` | — | Get current memory usage |
| `get_fps_stats` | — | Get current frame rate stats |
| `get_performance_snapshot` | — | Get a combined snapshot of CPU, memory, and FPS |

### Storage (8 tools)

| Tool | Parameters | Description |
|------|-----------|-------------|
| `list_preferences` | — | List all SharedPreferences files and entries |
| `list_databases` | — | List all SQLite databases |
| `query_database` | `database`, `query` | Execute a SELECT query (read-only, no DROP/DELETE/INSERT/UPDATE/ALTER) |
| `list_files` | `path?` | Browse app file system directory |
| `read_file` | `path` | Read a text file (JSON/XML pretty-printed). Binary files, images, and PDFs return a one-line summary |
| `browse_secure_storage` | — | List secure/encrypted storage entries |
| `list_dependencies` | — | List app dependencies |
| `list_loaded_libraries` | — | List loaded native libraries |

### Actions (6 tools)

| Tool | Parameters | Description |
|------|-----------|-------------|
| `clear_transactions` | — | Clear all captured network transactions |
| `clear_crashes` | — | Clear all crash reports |
| `clear_logs` | — | Clear all log entries |
| `simulate_location` | `latitude`, `longitude`, `altitude?`, `name?` | Set a mock GPS location (lat: -90..90, lng: -180..180) |
| `stop_location_simulation` | — | Stop mock location |
| `send_push_notification` | `title`, `body`, `channel_id?`, `priority?` | Send a simulated push notification |

## REST API Endpoints

All endpoints are prefixed with `/api`. Responses follow a standard envelope:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": 1774224835921,
  "meta": {
    "total": 42,
    "limit": 50,
    "offset": 0,
    "truncated": false
  }
}
```

### Health

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/health` | No | Server health check |

### Network

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/transactions` | List transactions (`?limit=&offset=`) |
| GET | `/api/transactions/{id}` | Transaction details |
| GET | `/api/transactions/{id}/request-body` | Request body (truncated to `maxBodySize`) |
| GET | `/api/transactions/{id}/response-body` | Response body (truncated to `maxBodySize`) |
| GET | `/api/websockets/connections` | WebSocket connections |
| GET | `/api/websockets/messages` | WebSocket messages (`?connection_id=`) |
| GET | `/api/rate-limit` | Current rate limit config |
| POST | `/api/rate-limit` | Update rate limit config |

### Diagnostics

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/crashes` | List crashes (`?limit=&offset=`) |
| GET | `/api/crashes/{id}` | Crash details with stack trace |
| GET | `/api/logs` | Log entries (`?level=&tag=&limit=&offset=`) |
| GET | `/api/leaks` | Memory leaks (`?limit=&offset=`) |
| GET | `/api/violations` | StrictMode violations (`?limit=&offset=`) |
| GET | `/api/device-info` | Device and app information |

### Performance

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/cpu` | CPU stats (`?include_history=true`) |
| GET | `/api/memory` | Memory stats (`?include_history=true`) |
| GET | `/api/fps` | FPS stats (`?include_history=true`) |
| GET | `/api/performance` | Combined performance snapshot |

### Storage

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/preferences` | SharedPreferences files and entries |
| GET | `/api/databases` | SQLite database list |
| POST | `/api/databases/{name}/query` | Execute SELECT query |
| GET | `/api/files/browse` | Browse files (`?path=`) |
| GET | `/api/files/read` | Read file (`?path=`) |
| GET | `/api/secure-storage` | Encrypted storage entries |

### Inspection

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dependencies` | App dependencies (`?category=`) |
| GET | `/api/loaded-libraries` | Native libraries (`?type=&system=`) |

### Actions

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/clear/transactions` | Clear transactions |
| POST | `/api/clear/crashes` | Clear crashes |
| POST | `/api/clear/logs` | Clear logs |
| POST | `/api/location` | Set mock location |
| DELETE | `/api/location` | Stop mock location |
| POST | `/api/push` | Send simulated notification |

### Streaming

| Protocol | Endpoint | Description |
|----------|----------|-------------|
| WebSocket | `/api/stream` | Real-time engine events (CPU, memory, FPS, logs) |

Subscribe by sending:
```json
{ "type": "subscribe", "channels": ["cpu", "memory", "fps", "logs"] }
```

Events arrive as:
```json
{ "channel": "cpu", "event": "update", "timestamp": 1774224835921, "data": { ... } }
```

## Security

The MCP server is designed for **debug builds only**. It should never be included in release builds.

### Authentication

Disabled by default. To enable bearer token auth:

```kotlin
// In your Application class or DI module
val config = ServerConfig(
    enableAuth = true,
    authToken = "your-secret-token"
)
```

Then pass `--token your-secret-token` to the bridge CLI.

The health endpoint (`/api/health`) always bypasses authentication.

### Header Redaction

Sensitive HTTP headers are automatically redacted in API responses. The following headers have their values replaced with `[REDACTED]`:

- `Authorization`
- `Cookie`
- `Set-Cookie`
- `Proxy-Authorization`
- `X-Api-Key`
- `X-Auth-Token`

### Input Validation

The bridge validates all tool inputs before forwarding to the device:

- **Path traversal** — file paths and database names containing `..` are rejected
- **SQL injection** — only `SELECT` queries are allowed; `DROP`, `DELETE`, `INSERT`, `UPDATE`, `ALTER` are blocked
- **Parameter ranges** — latitude (-90..90), longitude (-180..180) are bounds-checked
- **Required fields** — IDs, titles, and bodies are checked for presence and non-emptiness

### Network Exposure

The device server binds to `localhost:8999` on the Android device. It is only accessible through ADB port forwarding — it is not exposed on the device's network interfaces.

## Configuration

### Server Config

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `port` | Int | 8999 | HTTP server port |
| `enableAuth` | Boolean | false | Enable bearer token authentication |
| `authToken` | String? | null | Required token when auth is enabled |
| `maxBodySize` | Long | 1,048,576 (1 MB) | Max transaction body size in responses |
| `enabledCategories` | Set&lt;ApiCategory&gt; | All | Which endpoint groups to register |

### API Categories

Disable endpoint groups you don't need:

| Category | Routes Controlled |
|----------|-------------------|
| `NETWORK` | transactions, websockets, rate-limit |
| `DIAGNOSTICS` | crashes, logs, leaks, violations, device-info, dependencies, libraries |
| `PERFORMANCE` | cpu, memory, fps, performance snapshot |
| `STORAGE` | preferences, databases, files, secure-storage |
| `ACTIONS` | clear, location, push |
| `STREAMING` | WebSocket stream |

## Module Structure

```
mcp/
├── bridge/                          # Host-side JVM CLI
│   ├── build.gradle.kts
│   └── src/main/kotlin/.../bridge/
│       ├── Main.kt                  # Entry point
│       ├── config/
│       │   └── BridgeConfig.kt      # CLI argument parsing
│       ├── adb/
│       │   ├── AdbClient.kt         # ADB command runner
│       │   └── DeviceDiscovery.kt   # Device selection
│       ├── device/
│       │   ├── DeviceConnection.kt  # Connection + reconnection
│       │   ├── DeviceApiClient.kt   # HTTP client to device
│       │   └── ConnectionState.kt   # Connection lifecycle
│       ├── mcp/
│       │   ├── McpServer.kt         # JSON-RPC dispatcher
│       │   ├── McpProtocol.kt       # Protocol constants
│       │   └── McpCapabilities.kt   # Initialize response
│       ├── mcp/tools/
│       │   ├── McpTool.kt           # Abstract tool base
│       │   ├── ToolRegistry.kt      # Tool registration
│       │   ├── NetworkTools.kt      # 8 network tools
│       │   ├── DiagnosticTools.kt   # 6 diagnostic tools
│       │   ├── PerformanceTools.kt  # 4 performance tools
│       │   ├── StorageTools.kt      # 8 storage tools
│       │   └── ActionTools.kt       # 6 action tools
│       └── util/
│           ├── TextFormatter.kt     # Human-readable output
│           └── JsonRpc.kt           # JSON-RPC data classes
│
└── device-server/                   # Android-side embedded server
    ├── build.gradle.kts
    ├── src/main/AndroidManifest.xml
    └── src/main/java/.../server/
        ├── WormaCeptorServer.kt         # Ktor server setup
        ├── WormaCeptorServerInitializer.kt  # ContentProvider auto-start
        ├── ServerConfig.kt              # Configuration
        ├── di/
        │   └── ServerModule.kt          # Koin module
        ├── middleware/
        │   ├── AuthPlugin.kt           # Bearer token auth
        │   └── RedactionPlugin.kt      # Header/body redaction
        ├── routes/
        │   ├── HealthRoutes.kt
        │   ├── TransactionRoutes.kt
        │   ├── CrashRoutes.kt
        │   ├── LogRoutes.kt
        │   ├── PerformanceRoutes.kt
        │   ├── DiagnosticRoutes.kt
        │   ├── StorageRoutes.kt
        │   ├── NetworkRoutes.kt
        │   ├── InspectionRoutes.kt
        │   ├── ActionRoutes.kt
        │   └── DeviceInfoCollector.kt
        ├── streaming/
        │   ├── StreamEvent.kt
        │   ├── EventStreamManager.kt
        │   ├── EngineCollector.kt
        │   └── StreamRoutes.kt
        └── serialization/
            ├── JsonConfig.kt
            ├── EntityMappers.kt
            └── dto/                     # 12 DTO files
```

## Troubleshooting

**Bridge can't find device**
- Run `adb devices` and confirm your device shows as `device` (not `offline` or `unauthorized`)
- If multiple devices are connected, use `--device <serial>` or `-s <serial>`

**Connection refused / timeout**
- Make sure the app is running in the foreground
- Check logcat for `WormaCeptorMCP`: `adb logcat -s WormaCeptorMCP`
- Verify port forwarding: `adb forward tcp:8999 tcp:8999 && curl http://localhost:8999/api/health`

**Server not starting**
- The server needs Koin to be initialized first (waits up to 5 seconds)
- Ensure `WormaCeptorKoin` is set up in your Application class
- Check that `mcp:device-server` is included as `debugImplementation`

**Auth failures**
- Ensure `ServerConfig.enableAuth = true` and `authToken` is set on the device side
- Pass the same token with `--token` on the bridge side
- The health endpoint (`/api/health`) always bypasses auth

**Bridge reconnection exhausted**
- The bridge retries up to 20 times with exponential backoff (1s → 30s)
- If the device disconnects permanently, the bridge exits with an error state
- Restart the bridge after reconnecting the device

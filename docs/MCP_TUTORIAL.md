# Using WormaCeptor MCP with AI Agents

A hands-on guide to connecting your Android app's runtime data to AI tools like Claude Code, Cursor, and other MCP-compatible clients.

## What You'll Get

Once set up, you can ask your AI agent things like:

- "Show me the last 5 network requests"
- "Why did the app crash?"
- "What's in the user's SharedPreferences?"
- "Is there a memory leak?"
- "What's the current CPU and memory usage?"

The AI reads live data from your running app — no copy-pasting logs or screenshots.

---

## Part 1: Setup

### Step 1: Add the dependency

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    // Your existing WormaCeptor dependencies
    debugImplementation("com.github.azikar24.WormaCeptor:api-impl-persistence:2.2.1")

    // Add MCP server (debug only — zero code in release)
    debugImplementation("com.github.azikar24.WormaCeptor:mcp-device-server:2.2.1")
}
```

That's it. The server auto-starts when your debug app launches. No code changes.

### Step 2: Build the bridge

The bridge is a small CLI that sits between your AI tool and the device. Build it once:

```bash
cd /path/to/WormaCeptor
./gradlew :mcp:bridge:jar
```

This creates `mcp/bridge/build/libs/bridge.jar`.

### Step 3: Test the connection

Make sure your emulator or device is running the app:

```bash
# Check device is connected
adb devices
# Should show something like:
# emulator-5554    device

# Run the bridge manually to verify
java -jar mcp/bridge/build/libs/bridge.jar --verbose
```

You should see:

```
WormaCeptor MCP Bridge v1.0.0
Config: port=8999, device=auto
Found device: emulator-5554 (sdk_gphone64_arm64)
Port forwarded: localhost:8999 -> device:8999
Connected to WormaCeptor server
MCP server ready. Listening for requests on stdin...
```

Press `Ctrl+C` to stop. The bridge works.

### Step 4: Configure your AI tool

#### Claude Code

Add to your project's `.claude/settings.local.json`:

```json
{
  "mcpServers": {
    "wormaceptor": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/bridge.jar"]
    }
  }
}
```

Restart Claude Code. You should see WormaCeptor tools available.

#### Cursor

Add to `.cursor/mcp.json`:

```json
{
  "mcpServers": {
    "wormaceptor": {
      "command": "java",
      "args": ["-jar", "/absolute/path/to/bridge.jar"]
    }
  }
}
```

#### Any MCP-compatible client

The bridge speaks standard MCP (JSON-RPC 2.0 on stdin/stdout). Point your client at:

```
command: java -jar /path/to/bridge.jar
```

---

## Part 2: Using It

### Debugging Network Issues

**"What requests is the app making?"**

```
Show me the recent network transactions
```

The AI calls `list_transactions` and returns a table of HTTP requests with method, URL, status code, and duration.

**"Why is this endpoint slow?"**

```
Get the details of transaction 42, including request and response bodies
```

The AI calls `get_transaction`, `get_request_body`, and `get_response_body` to show you headers, timing, and payloads.

**"Show me all the failed requests"**

```
List transactions that returned 5xx status codes
```

The AI filters transactions by status code.

**"What's going over the wire?"**

```
Show me the request body for the last POST request to /api/login
```

**"Are there any WebSocket connections?"**

```
List active WebSocket connections and their recent messages
```

### Investigating Crashes

**"Did the app crash?"**

```
List all crashes
```

**"What caused it?"**

```
Get the full stack trace for crash #1
```

**"Fix the crash"**

```
Get crash #1, find the source file that caused it, and suggest a fix
```

The AI reads the crash, finds `MainActivityViewModel.kt:108` in your codebase, reads the file, and proposes a fix — all in one flow.

### Monitoring Performance

**"Is the app laggy?"**

```
Get the current FPS stats. Are there any dropped frames?
```

**"How's memory?"**

```
Show me CPU, memory, and FPS as a performance snapshot
```

**"Is it getting worse?"**

```
Get memory stats with history. Is there a trend of increasing memory use?
```

### Inspecting Storage

**"What's stored locally?"**

```
List all SharedPreferences files and show me what's in them
```

**"What databases exist?"**

```
List databases, then show me the tables in the main database
```

**"Run a query"**

```
Query the transactions database: SELECT * FROM transactions ORDER BY date DESC LIMIT 10
```

**"What's in the app's files?"**

```
Browse the app's files directory and show me the contents of config.json
```

### Finding Memory Leaks

**"Any leaks?"**

```
List detected memory leaks
```

**"Any thread violations?"**

```
Show me StrictMode violations — are there any disk reads on the main thread?
```

### Taking Actions

**"Clean up test data"**

```
Clear all transactions and crash logs
```

**"Test location features"**

```
Set the device location to the Eiffel Tower (48.8584, 2.2945)
```

**"Test push notifications"**

```
Send a test push notification with title "Order Update" and body "Your order has shipped!"
```

### Combining It All

The real power is combining tools in a single conversation:

```
The app crashed after making a network request. Find the crash, show me
the most recent transactions around the same time, check if there's a
memory issue, and suggest what went wrong.
```

The AI will:
1. Call `list_crashes` to find the crash
2. Call `get_crash` to read the stack trace
3. Call `list_transactions` to see recent network activity
4. Call `get_memory_stats` to check memory pressure
5. Cross-reference the timestamps and give you a diagnosis

---

## Part 3: Configuration Scenarios

### Default (zero config)

Nothing extra needed. The server starts on port 8999 with no auth.

```kotlin
// Application.kt
WormaCeptorApi.init(context = this)
```

Bridge:
```bash
java -jar bridge.jar
```

### Custom Port

Maybe port 8999 conflicts with something else on your machine.

**App side:**

```kotlin
WormaCeptorApi.configureMcpServer(McpConfig(port = 9090))
WormaCeptorApi.init(context = this)
```

**Bridge side:**

```bash
java -jar bridge.jar --port 9090
```

**MCP config:**

```json
{
  "mcpServers": {
    "wormaceptor": {
      "command": "java",
      "args": ["-jar", "/path/to/bridge.jar", "--port", "9090"]
    }
  }
}
```

### With Authentication

Prevent other apps on the device from accessing the debug server.

**App side:**

```kotlin
WormaCeptorApi.configureMcpServer(
    McpConfig(
        enableAuth = true,
        authToken = "my-secret-debug-token"
    )
)
WormaCeptorApi.init(context = this)
```

**Bridge side:**

```bash
java -jar bridge.jar --token my-secret-debug-token
```

**MCP config:**

```json
{
  "mcpServers": {
    "wormaceptor": {
      "command": "java",
      "args": [
        "-jar", "/path/to/bridge.jar",
        "--token", "my-secret-debug-token"
      ]
    }
  }
}
```

### With Auth + Custom Port

```kotlin
WormaCeptorApi.configureMcpServer(
    McpConfig(
        port = 9090,
        enableAuth = true,
        authToken = "my-secret-debug-token"
    )
)
```

```json
{
  "mcpServers": {
    "wormaceptor": {
      "command": "java",
      "args": [
        "-jar", "/path/to/bridge.jar",
        "--port", "9090",
        "--token", "my-secret-debug-token"
      ]
    }
  }
}
```

### Larger Response Bodies

By default, transaction bodies are capped at 1 MB. For APIs with large payloads:

```kotlin
WormaCeptorApi.configureMcpServer(
    McpConfig(maxBodySize = 5_242_880L) // 5 MB
)
```

### Disable Auto-Start

If you only want the MCP server running during specific debug sessions:

```kotlin
WormaCeptorApi.configureMcpServer(McpConfig(enabled = false))
WormaCeptorApi.init(context = this)
```

Then start it from your AI agent or from code when needed:

```kotlin
// From a debug menu button, for example:
WormaCeptorApi.startMcpServer()
```

### Multiple Devices

If you have multiple devices/emulators connected:

```bash
# List devices
adb devices
# emulator-5554    device
# emulator-5556    device

# Target a specific one
java -jar bridge.jar --device emulator-5556
```

**MCP config for a specific device:**

```json
{
  "mcpServers": {
    "wormaceptor": {
      "command": "java",
      "args": [
        "-jar", "/path/to/bridge.jar",
        "-s", "emulator-5556"
      ]
    }
  }
}
```

### Verbose Mode (Debugging the Bridge)

If things aren't working, run with verbose to see every request and response:

```bash
java -jar bridge.jar --verbose
```

This logs all MCP traffic to stderr:

```
<-- {"method":"tools/call","params":{"name":"list_crashes"}}
--> {"result":{"content":[{"type":"text","text":"1 crash(es):\n..."}]}}
```

**MCP config with verbose:**

```json
{
  "mcpServers": {
    "wormaceptor": {
      "command": "java",
      "args": ["-jar", "/path/to/bridge.jar", "--verbose"]
    }
  }
}
```

---

## Part 4: Tips

### Keep the app in the foreground

The MCP server runs inside the app process. If Android kills the app in the background, the server goes with it. Keep the app visible or use `adb shell settings put global background_activity_starts_enabled 1` on emulators.

### Bridge auto-reconnects

If you restart the app, the bridge detects the disconnect and retries with exponential backoff (up to 20 attempts). You don't need to restart the bridge.

### Sensitive data is redacted

Headers like `Authorization`, `Cookie`, and `X-Api-Key` are automatically replaced with `[REDACTED]` in API responses. Your tokens won't leak into AI conversations.

### SQL queries are read-only

The `query_database` tool only allows `SELECT` statements. `DROP`, `DELETE`, `INSERT`, `UPDATE`, and `ALTER` are blocked at both the bridge and server level.

### File access is scoped

File browsing and reading is limited to the app's sandbox. Path traversal (`..`) is rejected.

### Release builds are safe

The `mcp-device-server` module is included as `debugImplementation`. It physically does not exist in your release APK. There is nothing to disable or guard.

---

## Quick Reference

### All Bridge CLI Flags

| Flag | Short | Default | Description |
|------|-------|---------|-------------|
| `--port` | | 8999 | Device server port |
| `--device` | `-s` | auto | Target device serial |
| `--adb` | | `adb` | Path to adb |
| `--token` | | none | Bearer auth token |
| `--verbose` | `-v` | off | Log all requests/responses |
| `--version` | | | Print version |
| `--help` | `-h` | | Print help |

### All McpConfig Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `port` | Int | 8999 | Server port on device |
| `enableAuth` | Boolean | false | Require bearer token |
| `authToken` | String? | null | The token to require |
| `maxBodySize` | Long | 1,048,576 | Max body bytes in responses |
| `enabled` | Boolean | true | Auto-start on app launch |

### All 32 MCP Tools

| Category | Tools |
|----------|-------|
| **Network** | `list_transactions`, `get_transaction`, `get_request_body`, `get_response_body`, `list_websocket_connections`, `list_websocket_messages`, `get_rate_limit`, `set_rate_limit` |
| **Diagnostics** | `list_crashes`, `get_crash`, `tail_logs`, `list_leaks`, `list_violations`, `get_device_info` |
| **Performance** | `get_cpu_stats`, `get_memory_stats`, `get_fps_stats`, `get_performance_snapshot` |
| **Storage** | `list_preferences`, `list_databases`, `query_database`, `list_files`, `read_file`, `browse_secure_storage`, `list_dependencies`, `list_loaded_libraries` |
| **Actions** | `clear_transactions`, `clear_crashes`, `clear_logs`, `simulate_location`, `stop_location_simulation`, `send_push_notification` |

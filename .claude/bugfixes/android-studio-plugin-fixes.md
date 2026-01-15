# Android Studio Plugin Bug Fixes

## Issues Identified

### Issue 1: ViewerActivity Not Exported (CRITICAL)
**File:** `features/viewer/src/main/AndroidManifest.xml:7`

The ViewerActivity has `android:exported="false"`, which prevents it from being launched via ADB from outside the app.

**Fix:** Change to `android:exported="true"` to allow ADB launch.

---

### Issue 2: Incorrect Activity Component Name Format
**File:** `plugins/android-studio/src/main/kotlin/com/azikar24/wormaceptor/studio/service/WormaCeptorServiceImpl.kt:98`

Current code:
```kotlin
"-n", "$pkg/com.azikar24.wormaceptor.feature.viewer.ViewerActivity"
```

The viewer module has namespace `com.azikar24.wormaceptor.feature.viewer`, and the activity is declared as `.ViewerActivity` in the manifest. When merged into the host app, the full class path is:
`com.azikar24.wormaceptor.feature.viewer.ViewerActivity`

The current format is correct, but we need to verify this works after fixing the exported flag.

---

### Issue 3: Package Detection May Iterate All Packages
**File:** `plugins/android-studio/src/main/kotlin/com/azikar24/wormaceptor/studio/service/WormaCeptorServiceImpl.kt:356-390`

The `detectWormaCeptorPackage()` method iterates through ALL packages on the device and tries to query each one's content provider. This is extremely slow (hundreds of packages) and causes timeouts.

**Current logic:**
1. Lists all packages on device (could be 200+)
2. Sorts them (prioritizing packages with "worma")
3. For each package, runs an ADB content query (each takes ~500ms)
4. Returns first one that works

**Fix:** Only try packages containing "worma" in the name, don't iterate all packages:
```kotlin
val wormaCeptorPackages = packages.filter { it.contains("worma", ignoreCase = true) }
for (pkg in wormaCeptorPackages) {
    if (tryContentProvider(device, pkg)) {
        targetPackage = pkg
        return pkg
    }
}
```

---

### Issue 4: Type Cast Bug in Content Provider
**File:** `api/client/src/main/java/com/azikar24/wormaceptor/api/contentprovider/WormaCeptorContentProvider.kt:127`

Current (buggy):
```kotlin
(getProperty(item, "tookMs") ?: getProperty(item, "durationMs") as? Number).toString().toLongOrNull()
```

The `as? Number` cast only applies to `getProperty(item, "durationMs")`, not to the Elvis result. This causes the first property to not be cast, resulting in a potential ClassCastException or incorrect value.

**Fix:**
```kotlin
((getProperty(item, "tookMs") ?: getProperty(item, "durationMs")) as? Number)?.toLong()
```

---

## Files to Modify

1. **`features/viewer/src/main/AndroidManifest.xml`**
   - Change `android:exported="false"` to `android:exported="true"` for ViewerActivity

2. **`plugins/android-studio/.../WormaCeptorServiceImpl.kt`**
   - Optimize package detection to only check packages containing "worma"
   - Add timeout handling for content provider queries

3. **`api/client/.../WormaCeptorContentProvider.kt`**
   - Fix the type cast precedence bug on line 127

---

## Implementation Steps

### Step 1: Fix ViewerActivity Export
```xml
<!-- features/viewer/src/main/AndroidManifest.xml -->
<activity
    android:name=".ViewerActivity"
    android:exported="true"
    android:theme="@android:style/Theme.Material.Light.NoActionBar" />
```

### Step 2: Optimize Package Detection
```kotlin
// WormaCeptorServiceImpl.kt - detectWormaCeptorPackage()
private fun detectWormaCeptorPackage(device: String): String? {
    if (lastPackageDetectionDevice != device) {
        targetPackage = null
        lastPackageDetectionDevice = device
    }
    targetPackage?.let { return it }

    try {
        val packagesOutput = executeAdbCommand("-s", device, "shell", "pm", "list", "packages")
        val packages = packagesOutput.lines()
            .filter { it.startsWith("package:") }
            .map { it.removePrefix("package:").trim() }

        // Only check packages likely to have WormaCeptor
        val wormaCeptorPackages = packages.filter { it.contains("worma", ignoreCase = true) }

        for (pkg in wormaCeptorPackages) {
            if (tryContentProvider(device, pkg)) {
                log.info("Detected WormaCeptor package: $pkg")
                targetPackage = pkg
                return pkg
            }
        }

        log.warn("No WormaCeptor package found. Checked: ${wormaCeptorPackages.joinToString()}")
    } catch (e: Exception) {
        log.warn("Failed to detect WormaCeptor package", e)
    }

    return null
}
```

### Step 3: Fix Content Provider Type Cast
```kotlin
// WormaCeptorContentProvider.kt - addTransactionToCursor()
cursor.addRow(arrayOf(
    getProperty(item, "id")?.toString() ?: return,
    getProperty(item, "method")?.toString() ?: "GET",
    getProperty(item, "host")?.toString() ?: "",
    getProperty(item, "path")?.toString() ?: "/",
    (getProperty(item, "code") as? Number)?.toInt(),
    ((getProperty(item, "tookMs") ?: getProperty(item, "durationMs")) as? Number)?.toLong(),  // FIXED
    getProperty(item, "status")?.toString() ?: TransactionStatus.COMPLETED.name,
    // ... rest unchanged
))
```

---

## Verification

1. **Build and install the demo app:**
   ```bash
   ./gradlew :app:installDebug
   ```

2. **Rebuild the plugin:**
   ```bash
   cd plugins/android-studio && ./gradlew buildPlugin
   ```

3. **Install the plugin in Android Studio**

4. **Test "Open on Device" button:**
   ```bash
   adb shell am start -n com.azikar24.wormaceptorapp/com.azikar24.wormaceptor.feature.viewer.ViewerActivity
   ```
   Should open the viewer activity on device.

5. **Test transaction loading:**
   - Make some HTTP requests in the demo app
   - Click Refresh in the plugin
   - Transactions should appear

6. **Check plugin logs:**
   Help > Diagnostic Tools > Show Log in Finder
   Search for "WormaCeptor" to see detection and query logs.

---

## Version Update

Bump plugin version from `1.0.1` to `1.0.2` in `plugins/android-studio/build.gradle.kts`

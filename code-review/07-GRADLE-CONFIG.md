# Gradle Configuration Issues

This document lists issues found in Gradle build configuration files.

---

## Critical Issues

### 1. compileSdk Lower Than targetSdk

**File:** `gradle/libs.versions.toml:29-31`

**Problem:** You cannot target a higher SDK than you compile against. This causes build failures.

**Bad Code:**
```toml
targetSdk = "36"
compileSdk = "34"
```

**Fix:**
```toml
compileSdk = "36"
targetSdk = "36"
```

---

### 2. Missing Build Optimization Settings

**File:** `gradle.properties:13`

**Problem:** With 27 modules, missing optimizations significantly increases build times.

**Current:**
```properties
# org.gradle.parallel=true
```

**Fix:** Add these settings:
```properties
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
android.enableR8.fullMode=true
```

---

### 3. Hardcoded Versions in Plugin Module

**File:** `plugins/android-studio/build.gradle.kts:6-7,26`

**Problem:** Hardcoded versions bypass centralized version management.

**Bad Code:**
```kotlin
id("org.jetbrains.intellij.platform") version "2.2.1"
kotlin("jvm") version "2.2.0-RC"
implementation("com.google.code.gson:gson:2.10.1")
```

**Fix:** Create version catalog for plugin module:
```toml
# plugins/android-studio/gradle/libs.versions.toml
[versions]
intellij-platform = "2.2.1"
kotlin = "2.2.0-RC"
gson = "2.10.1"

[libraries]
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }

[plugins]
intellij-platform = { id = "org.jetbrains.intellij.platform", version.ref = "intellij-platform" }
```

---

### 4. Hardcoded Version in Test Module

**File:** `test/architecture/build.gradle.kts:15`

**Bad Code:**
```kotlin
implementation("com.tngtech.archunit:archunit:1.0.1")
```

**Fix:** Add to `gradle/libs.versions.toml`:
```toml
[versions]
archunit = "1.0.1"

[libraries]
archunit = { group = "com.tngtech.archunit", name = "archunit", version.ref = "archunit" }
```

Then update build file:
```kotlin
implementation(libs.archunit)
```

---

### 5. Missing ProGuard/R8 Rules

**File:** `app/build.gradle.kts`

**Problem:** No `buildTypes` configuration with ProGuard rules. Critical for production builds using reflection (Room, Retrofit, Gson).

**Fix:** Add to `app/build.gradle.kts`:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
    debug {
        isMinifyEnabled = false
    }
}
```

Create `app/proguard-rules.pro`:
```proguard
# Keep Room entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Retrofit and OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# Keep domain entities for serialization
-keep class com.azikar24.wormaceptor.domain.entities.** { *; }

# Keep parcelize
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
```

---

### 6. Kotlin Version Mismatch

**Files:**
- `gradle/libs.versions.toml:3` - `kotlin = "2.0.21"`
- `plugins/android-studio/build.gradle.kts:7` - `kotlin("jvm") version "2.2.0-RC"`

**Problem:** Different Kotlin versions can lead to binary compatibility issues.

**Recommendation:** Document the mismatch if intentional (IntelliJ compatibility), or align versions.

---

## Important Issues

### 7. Jetifier Still Enabled

**File:** `gradle.properties:18`

**Problem:** Jetifier was for migrating old support libraries. Modern projects don't need it, and it slows builds.

**Current:**
```properties
android.enableJetifier=true
```

**Fix:** Try disabling and rebuild:
```properties
android.enableJetifier=false
```

---

### 8. Unnecessary androidx.core.ktx in Domain Modules

**Files:**
- `domain/entities/build.gradle.kts:24`
- `domain/contracts/build.gradle.kts:25`

**Problem:** Domain modules should be framework-agnostic. Including Android dependencies violates clean architecture.

**Fix:** Remove `androidx.core.ktx` from pure domain modules unless specifically needed.

---

### 9. Missing Test Dependencies

**Files:** All module build files

**Problem:** No test dependencies defined in library modules.

**Fix:** Add to each module:
```kotlin
dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

---

### 10. Missing Dependency Constraints

**File:** `build.gradle.kts` (root)

**Problem:** Risk of version conflicts with transitive dependencies.

**Fix:** Add to root `build.gradle.kts`:
```kotlin
subprojects {
    configurations.all {
        resolutionStrategy {
            force(
                "androidx.core:core-ktx:${libs.versions.coreKtx.get()}",
                "com.squareup.okhttp3:okhttp:${libs.versions.okhttp.get()}"
            )
        }
    }
}
```

---

## Summary

| Issue | Severity | Impact |
|-------|----------|--------|
| compileSdk < targetSdk | Critical | Build failures |
| Missing build optimizations | Critical | Slow builds |
| Hardcoded versions | Important | Version drift |
| Missing ProGuard rules | Critical | Release crashes |
| Kotlin version mismatch | Important | Compatibility |
| Jetifier enabled | Minor | Slow builds |
| Android deps in domain | Important | Architecture violation |
| Missing test deps | Important | No testing |
| Missing constraints | Minor | Version conflicts |

**Total Issues:** 10

## Good Practices Observed

- Consistent use of version catalogs
- KSP instead of deprecated kapt
- Consistent Java/Kotlin version (17)
- Clean module structure
- Proper `api` vs `implementation` usage

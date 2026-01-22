# Tools Tab UI Fixes - TopAppBar and Navigation

## Summary

Fixed screens in the Tools tab that were missing proper TopAppBar with back navigation, causing bad edge-to-edge UI and poor navigation UX.

## Issues Found

### 1. CookiesListScreen

**Location:** `features/cookies/src/main/java/.../ui/CookiesListScreen.kt`

**Problem:** Had Scaffold with TopAppBar but was missing the back navigation icon. Users could not navigate back from this screen using the top bar.

**Fix:**
- Added `onBack: (() -> Unit)? = null` parameter to the composable
- Added `navigationIcon` with back arrow to TopAppBar
- Updated `CookiesFeature.kt` to pass `onNavigateBack` to the screen

### 2. LocationScreen

**Location:** `features/location/src/main/java/.../ui/LocationScreen.kt`

**Problem:** Had NO Scaffold and NO TopAppBar at all. Content was rendered directly in a Box, causing:
- Edge-to-edge content with no proper header
- No way to navigate back
- Inconsistent UI compared to other tool screens

**Fix:**
- Added complete Scaffold with TopAppBar including:
  - Title with Location icon and "Location Simulator" text
  - "ACTIVE" badge when mock location is enabled
  - Back navigation icon
  - Proper surface colors
- Moved SnackbarHost to Scaffold (removed duplicate)
- Added `onBack: (() -> Unit)? = null` parameter
- Updated `LocationFeature.kt` to pass `onNavigateBack` to the screen

### 3. ViewerActivity Navigation

**Location:** `features/viewer/src/main/java/.../ViewerActivity.kt`

**Problem:** Was not passing `onNavigateBack` to CookiesInspector and LocationSimulator routes.

**Fix:**
- Added `onNavigateBack = { navController.popBackStack() }` to:
  - CookiesInspector (line 357)
  - LocationSimulator (line 389)

## Files Changed

1. `features/cookies/src/main/java/com/azikar24/wormaceptor/feature/cookies/ui/CookiesListScreen.kt`
   - Added ArrowBack icon import
   - Added `onBack` parameter
   - Added `navigationIcon` to TopAppBar

2. `features/cookies/src/main/java/com/azikar24/wormaceptor/feature/cookies/CookiesFeature.kt`
   - Updated CookiesListScreen call to pass `onBack = onNavigateBack`

3. `features/location/src/main/java/com/azikar24/wormaceptor/feature/location/ui/LocationScreen.kt`
   - Added ArrowBack icon import
   - Added Scaffold, TopAppBar, TopAppBarDefaults imports
   - Added `@OptIn(ExperimentalMaterial3Api::class)`
   - Added `onBack` parameter
   - Wrapped content in Scaffold with full TopAppBar
   - Moved SnackbarHost to Scaffold

4. `features/location/src/main/java/com/azikar24/wormaceptor/feature/location/LocationFeature.kt`
   - Added `onNavigateBack` parameter to LocationSimulator
   - Updated LocationScreen call to pass `onBack = onNavigateBack`

5. `features/viewer/src/main/java/com/azikar24/wormaceptor/feature/viewer/ViewerActivity.kt`
   - Added `onNavigateBack` to CookiesInspector composable call
   - Added `onNavigateBack` to LocationSimulator composable call

## Verification

All other tool screens were verified to have proper Scaffold + TopAppBar + back navigation:
- CPU Monitor
- Memory Monitor
- FPS Monitor
- View Borders
- SharedPreferences
- Device Information
- Logs
- Touch Visualization
- Crypto Tool
- Grid Overlay
- Measurement Tool
- Secure Storage Viewer
- Compose Render Tracker
- Rate Limiter
- Push Token Manager
- Loaded Libraries Inspector
- And others...

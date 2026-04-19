/** Convention plugin for Android library modules with Jetpack Compose. */
plugins {
    id("wormaceptor.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    val bom = platform("androidx.compose:compose-bom:2025.10.00")
    "implementation"(bom)
    "implementation"("androidx.compose.ui:ui")
    "implementation"("androidx.compose.ui:ui-tooling-preview")
    "implementation"("androidx.compose.material3:material3")
    "implementation"("androidx.compose.material:material-icons-extended")
    "debugImplementation"("androidx.compose.ui:ui-tooling")
}

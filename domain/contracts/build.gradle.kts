/*
 * NOTE: This module requires Android dependencies due to:
 * - SyntaxHighlighter uses Compose AnnotatedString and Color
 * - TransactionRepository uses AndroidX Paging
 * Consider moving SyntaxHighlighter to features/viewer in a future refactor
 * to make this module pure Kotlin.
 */
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.azikar24.wormaceptor.domain.contracts"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":domain:entities"))

    // Required for TransactionRepository pagination support
    implementation(libs.androidx.paging.runtime)

    // Required for SyntaxHighlighter (AnnotatedString, Color)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
}

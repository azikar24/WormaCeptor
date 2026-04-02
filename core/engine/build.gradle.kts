plugins {
    id("wormaceptor.android.library")
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.azikar24.wormaceptor.core.engine"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":domain:entities"))
    implementation(project(":domain:contracts"))
    implementation(project(":core:ui"))
    implementation(libs.androidx.paging.runtime)
    implementation(libs.okhttp)
    implementation(libs.androidx.security.crypto)
    // DI
    api(libs.koin.android)
    // Compose for Performance Overlay UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.azikar24.wormaceptor.api.client"
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
    implementation(libs.androidx.core.ktx)
    // API might depend on domain entities if they are part of the public contract
    implementation(project(":domain:entities"))
    implementation(libs.okhttp)
    implementation(libs.androidx.activity.ktx)

    // Wiring dependencies for Phase 3 & 4
    implementation(project(":core:engine"))
    implementation(project(":infra:persistence:sqlite"))
    implementation(project(":domain:contracts"))
    implementation(libs.androidx.room.runtime)
    implementation(project(":platform:android"))
    implementation(project(":features:viewer"))
}
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.azikar24.wormaceptor.core.engine"
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
    implementation(project(":domain:contracts"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.okhttp)
    // DI
    api(libs.koin.android)
}

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
    api(project(":domain:entities"))
    api(project(":domain:contracts"))
    implementation(project(":core:engine"))
    compileOnly(libs.ktor.client.core)
    implementation(libs.okhttp)
    implementation(libs.androidx.activity.ktx)
    implementation(project(":platform:android"))
}

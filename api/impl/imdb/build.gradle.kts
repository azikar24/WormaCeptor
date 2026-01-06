plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.azikar24.wormaceptor.api.impl.imdb"
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
    implementation(project(":api:client"))
    implementation(project(":core:engine"))
    implementation(project(":infra:persistence:sqlite")) // For InMemory repos hosted there
    implementation(project(":domain:contracts"))
    implementation(project(":platform:android"))
    implementation(project(":features:viewer"))
}

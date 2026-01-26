plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.azikar24.wormaceptor.feature.viewer"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    buildFeatures {
        compose = true
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
    implementation(project(":api:client"))
    implementation(project(":core:engine"))
    implementation(project(":core:ui"))
    implementation(project(":domain:contracts"))
    implementation(project(":domain:entities"))
    implementation(project(":features:preferences"))
    implementation(project(":features:logs"))
    implementation(project(":features:deviceinfo"))
    implementation(project(":features:settings"))
    implementation(project(":features:database"))
    implementation(project(":features:filebrowser"))
    implementation(project(":features:memory"))
    implementation(project(":features:fps"))
    implementation(project(":features:websocket"))
    implementation(project(":features:cookies"))
    implementation(project(":features:cpu"))
    implementation(project(":features:touchvisualization"))
    implementation(project(":features:viewborders"))
    implementation(project(":features:location"))
    implementation(project(":features:pushsimulator"))
    // Phase 5 features
    implementation(project(":features:viewhierarchy"))
    implementation(project(":features:leakdetection"))
    implementation(project(":features:threadviolation"))
    implementation(project(":features:webviewmonitor"))
    implementation(project(":features:crypto"))
    implementation(project(":features:gridoverlay"))
    implementation(project(":features:measurement"))
    implementation(project(":features:securestorage"))
    implementation(project(":features:composerender"))
    implementation(project(":features:ratelimit"))
    implementation(project(":features:pushtoken"))
    implementation(project(":features:loadedlibraries"))
    implementation(project(":features:dependenciesinspector"))
    implementation(project(":features:interception"))

    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.kotlinx.collections.immutable)
    // DI
    implementation(libs.koin.compose)
    debugImplementation(libs.androidx.ui.tooling)
}

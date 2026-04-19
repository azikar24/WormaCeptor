plugins {
    id("wormaceptor.android.library")
    alias(libs.plugins.compose.compiler)
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.api.client"
    buildFeatures {
        compose = true
    }
}

dependencies {
    api(project(":domain:entities"))
    api(project(":domain:contracts"))
    implementation(project(":core:engine"))
    compileOnly(libs.ktor.client.core)
    implementation(libs.okhttp)
    implementation(libs.androidx.activity.ktx)
    implementation(project(":platform:android"))

    // Compose types for Modifier.trackRecomposition — compileOnly so consumers
    // who don't use Compose don't pull it transitively.
    compileOnly(platform(libs.androidx.compose.bom))
    compileOnly(libs.androidx.ui)
}

plugins {
    id("wormaceptor.android.feature")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.webviewmonitor"
}

dependencies {
    implementation(project(":common:presentation"))
    implementation(project(":core:engine"))
    implementation(project(":core:ui"))
    implementation(project(":domain:entities"))
    implementation(libs.androidx.navigation.compose)
}

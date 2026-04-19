plugins {
    id("wormaceptor.android.compose")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.platform.android"
}

dependencies {
    // Provide Android specific implementations or wrappers
    implementation(libs.androidx.startup)
    implementation(libs.androidx.activity.ktx)
    implementation(project(":domain:contracts"))
    implementation(project(":core:ui"))
}

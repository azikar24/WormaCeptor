plugins {
    id("wormaceptor.android.feature")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.deviceinfo"
}

dependencies {
    implementation(project(":common:presentation"))
    implementation(project(":core:ui"))
    implementation(project(":domain:entities"))
}

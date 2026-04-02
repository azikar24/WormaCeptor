plugins {
    id("wormaceptor.android.feature")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.deviceinfo"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":domain:entities"))
}

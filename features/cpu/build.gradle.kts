plugins {
    id("wormaceptor.android.feature")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.cpu"
}

dependencies {
    implementation(project(":core:engine"))
    implementation(project(":core:ui"))
    implementation(project(":domain:entities"))
    implementation(project(":common:presentation"))
}

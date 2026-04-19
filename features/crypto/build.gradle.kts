plugins {
    id("wormaceptor.android.feature")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.crypto"
}

dependencies {
    implementation(project(":core:engine"))
    implementation(project(":core:ui"))
    implementation(project(":common:presentation"))
    implementation(project(":domain:entities"))
}

plugins {
    id("wormaceptor.android.feature")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.pushsimulator"
}

dependencies {
    implementation(project(":core:engine"))
    implementation(project(":core:ui"))
    implementation(project(":common:presentation"))
    implementation(project(":domain:entities"))
    implementation(project(":domain:contracts"))

    implementation(libs.androidx.activity.compose)
}

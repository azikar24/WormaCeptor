plugins {
    id("wormaceptor.android.feature")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.pushtoken"
}

dependencies {
    implementation(project(":domain:entities"))
    implementation(project(":core:engine"))
    implementation(project(":core:ui"))
    implementation(project(":common:presentation"))

    implementation(libs.androidx.navigation.compose)

    // Firebase Messaging - compileOnly to avoid forcing dependency on consumers
    compileOnly(libs.firebase.messaging)
}

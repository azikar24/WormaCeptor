plugins {
    id("wormaceptor.android.feature")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.pushtoken"
}

dependencies {
    implementation(project(":domain:entities"))
    implementation(project(":core:engine"))
    implementation(project(":core:ui"))

    implementation(libs.androidx.navigation.compose)

    // Firebase Messaging - compileOnly to avoid forcing dependency on consumers
    compileOnly(libs.firebase.messaging)
}

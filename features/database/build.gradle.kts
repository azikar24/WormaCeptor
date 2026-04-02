plugins {
    id("wormaceptor.android.feature")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.database"
}

dependencies {
    implementation(project(":common:presentation"))
    implementation(project(":core:ui"))
    implementation(project(":domain:contracts"))
    implementation(project(":domain:entities"))

    implementation(libs.androidx.navigation.compose)
}

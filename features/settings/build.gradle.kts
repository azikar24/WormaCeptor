plugins {
    id("wormaceptor.android.feature")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.settings"
}

dependencies {
    implementation(project(":common:presentation"))
    implementation(project(":domain:contracts"))
    implementation(project(":core:ui"))
    implementation(libs.androidx.datastore.preferences)
}

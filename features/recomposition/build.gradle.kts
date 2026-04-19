plugins {
    id("wormaceptor.android.feature")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.recomposition"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":common:presentation"))
}

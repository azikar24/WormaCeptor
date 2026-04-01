plugins {
    id("wormaceptor.android.feature")
}

android {
    namespace = "com.azikar24.wormaceptor.feature.recomposition"
}

dependencies {
    implementation(project(":core:ui"))
}

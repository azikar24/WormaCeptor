plugins {
    id("wormaceptor.android.compose")
}

android {
    namespace = "com.azikar24.wormaceptor.core.ui"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":domain:contracts"))

    api(libs.androidx.navigation.compose)
}

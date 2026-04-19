plugins {
    id("wormaceptor.android.compose")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.common.presentation"
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}

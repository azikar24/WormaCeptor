plugins {
    id("wormaceptor.android.compose")
}

android {
    namespace = "com.azikar24.wormaceptor.common.presentation"
}

dependencies {
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}

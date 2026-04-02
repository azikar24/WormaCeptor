plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.infra.networking.okhttp"
}

dependencies {
    implementation(project(":domain:contracts"))

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
}

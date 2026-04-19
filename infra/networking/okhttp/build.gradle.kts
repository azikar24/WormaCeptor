plugins {
    id("wormaceptor.android.library")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.infra.networking.okhttp"
}

dependencies {
    implementation(project(":domain:contracts"))

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
}

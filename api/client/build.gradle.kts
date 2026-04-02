plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.api.client"
}

dependencies {
    api(project(":domain:entities"))
    api(project(":domain:contracts"))
    implementation(project(":core:engine"))
    compileOnly(libs.ktor.client.core)
    implementation(libs.okhttp)
    implementation(libs.androidx.activity.ktx)
    implementation(project(":platform:android"))
}

plugins {
    id("wormaceptor.android.library")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.infra.syntax.json"
}

dependencies {
    implementation(project(":domain:contracts"))
}

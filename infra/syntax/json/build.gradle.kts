plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.infra.syntax.json"
}

dependencies {
    implementation(project(":domain:contracts"))
}

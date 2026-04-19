plugins {
    id("wormaceptor.android.library")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.infra.parser.html"
}

dependencies {
    implementation(project(":domain:contracts"))
}

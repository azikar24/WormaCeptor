plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.infra.parser.html"
}

dependencies {
    implementation(project(":domain:contracts"))
}

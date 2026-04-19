plugins {
    id("wormaceptor.android.library")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.infra.syntax.xml"
}

dependencies {
    implementation(project(":domain:contracts"))
}

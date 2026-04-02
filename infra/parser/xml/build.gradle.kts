plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.infra.parser.xml"
}

dependencies {
    implementation(project(":domain:contracts"))
}

plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.infra.parser.json"
}

dependencies {
    implementation(project(":domain:contracts"))

    testImplementation(libs.org.json)
}

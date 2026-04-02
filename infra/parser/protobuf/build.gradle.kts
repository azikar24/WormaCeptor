plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.infra.parser.protobuf"
}

dependencies {
    implementation(project(":domain:contracts"))
    implementation(project(":domain:entities"))
}

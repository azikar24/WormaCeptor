plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.api.common"
}

dependencies {
    api(project(":api:client"))
    api(project(":domain:entities"))
    api(project(":domain:contracts"))
    implementation(project(":core:engine"))
    implementation(project(":features:viewer"))
    implementation(project(":core:ui"))

    // Feature modules + infra (syntax/parsers) assembled via :wiring
    implementation(project(":wiring"))
}

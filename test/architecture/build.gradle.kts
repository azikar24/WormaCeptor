plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.test.architecture"
}

dependencies {
    implementation("com.tngtech.archunit:archunit:1.0.1")
    implementation(libs.junit)

    // Depend on modules to scan
    implementation(project(":domain:entities"))
    implementation(project(":core:engine"))
    implementation(project(":features:viewer"))
    // Add others as needed
}

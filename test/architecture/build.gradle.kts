plugins {
    alias(libs.plugins.kotlin.android) // or JVM plugin, but android project usually implies android
    alias(libs.plugins.android.library) 
}

android {
    namespace = "com.azikar24.wormaceptor.test.architecture"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
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

plugins {
    id("wormaceptor.android.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp) // Needed for Room
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.infra.persistence.sqlite"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(project(":domain:contracts"))
    implementation(project(":domain:entities"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlin.serialization)
    implementation(libs.androidx.paging.runtime)

    ksp(libs.androidx.room.compiler)

    implementation(libs.sqlcipher)
    implementation(libs.sqlite.ktx)
}

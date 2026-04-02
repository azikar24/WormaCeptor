plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.api.impl.persistence"
}

dependencies {
    implementation(project(":api:client"))
    implementation(project(":api:common"))
    implementation(project(":core:engine"))
    implementation(project(":infra:persistence:sqlite"))
    implementation(project(":domain:contracts"))
    implementation(project(":platform:android"))
    api(project(":features:viewer"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.sqlcipher)
    implementation(libs.sqlite.ktx)
}

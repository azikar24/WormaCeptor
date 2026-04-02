plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.api.impl.imdb"
}

dependencies {
    implementation(project(":api:client"))
    implementation(project(":api:common"))
    implementation(project(":core:engine"))
    implementation(project(":infra:persistence:sqlite")) // For InMemory repos hosted there
    implementation(project(":domain:contracts"))
    implementation(project(":platform:android"))
    implementation(project(":features:viewer"))
}

plugins {
    id("wormaceptor.android.library")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.api.impl.noop"
}

dependencies {
    implementation(project(":api:client"))
}

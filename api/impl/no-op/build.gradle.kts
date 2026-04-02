plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.api.impl.noop"
}

dependencies {
    implementation(project(":api:client"))
}

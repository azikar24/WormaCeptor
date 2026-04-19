plugins {
    id("wormaceptor.android.library")
    id("wormaceptor.publishing")
}

android {
    namespace = "com.azikar24.wormaceptor.domain.contracts"
}

dependencies {
    implementation(project(":domain:entities"))
    implementation(libs.androidx.paging.runtime)
}

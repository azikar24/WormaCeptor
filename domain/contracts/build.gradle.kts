plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.domain.contracts"
}

dependencies {
    implementation(project(":domain:entities"))
    implementation(libs.androidx.paging.runtime)
}

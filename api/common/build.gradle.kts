plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.azikar24.wormaceptor.api.common"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    api(project(":api:client"))
    api(project(":domain:entities"))
    api(project(":domain:contracts"))
    api(project(":core:engine"))
    api(project(":features:viewer"))
    implementation(project(":infra:syntax:json"))
    implementation(project(":infra:syntax:xml"))
    implementation(project(":infra:parser:protobuf"))
    implementation(project(":infra:parser:multipart"))
    implementation(project(":infra:parser:form"))
    implementation(project(":infra:parser:xml"))
    implementation(project(":infra:parser:html"))
    implementation(project(":infra:parser:json"))
    implementation(project(":infra:parser:image"))
    implementation(project(":infra:parser:pdf"))
}

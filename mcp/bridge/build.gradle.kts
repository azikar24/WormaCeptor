plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    mainClass.set("com.azikar24.wormaceptor.mcp.bridge.MainKt")
}

// DTOs shared with :mcp:device-server, compiled into both modules (not a module or artifact of its own).
sourceSets.main {
    kotlin.srcDir("../protocol/src/main/kotlin")
}

// Detekt only scans module dirs; the shared DTOs are checked here, once.
detekt {
    source.from("../protocol/src/main/kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "com.azikar24.wormaceptor.mcp.bridge.MainKt"
        attributes["Implementation-Version"] = providers.gradleProperty("VERSION_NAME").get()
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

dependencies {
    implementation(project(":domain:entities"))

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlin.serialization)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

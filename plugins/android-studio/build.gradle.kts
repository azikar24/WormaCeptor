/*
 * Copyright AziKar24 2025.
 */

plugins {
    id("org.jetbrains.intellij") version "1.17.0"
    kotlin("jvm") version "1.9.22"
}

group = "com.azikar24.wormaceptor"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

intellij {
    version.set("2024.1")
    type.set("AI") // Android Studio
    plugins.set(listOf("android", "org.jetbrains.kotlin"))
}

kotlin {
    jvmToolchain(17)
}

tasks {
    patchPluginXml {
        version.set(project.version.toString())
        sinceBuild.set("241")
        untilBuild.set("251.*")
    }

    buildSearchableOptions {
        enabled = false
    }

    runIde {
        // Configure to run with Android Studio
        ideDir.set(file("C:/Program Files/Android/Android Studio"))
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}

// Version constants - keep in sync with gradle/libs.versions.toml
object Versions {
    const val KOTLIN = "2.0.21" // Must match libs.versions.toml kotlin version
    const val INTELLIJ_PLATFORM = "2.2.1" // Must match libs.versions.toml intellijPlatform
    const val GSON = "2.10.1" // Must match libs.versions.toml gson
}

plugins {
    id("org.jetbrains.intellij.platform") version "2.2.1"
    kotlin("jvm") version "2.0.21"
}

group = "com.azikar24.wormaceptor"
version = "1.0.3"

repositories {
    mavenCentral()
    google()
    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.google.code.gson:gson:${Versions.GSON}")

    intellijPlatform {
        local("/Applications/Android Studio Preview.app/Contents")
        bundledPlugin("com.android.tools.idea.smali")
    }
}

intellijPlatform {
    pluginConfiguration {
        version.set(project.version.toString())
        ideaVersion {
            sinceBuild.set("253")
            untilBuild.set("253.*")
        }
    }

    buildSearchableOptions.set(false)
}

// runIde will use the local IDE configured in dependencies

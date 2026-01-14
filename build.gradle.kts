/*
 * Copyright AziKar24 21/12/2025.
 */

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.navigation.safeargs) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.compose.compiler) apply false
}

subprojects {
    // Apply maven-publish to all library modules (exclude app and test modules)
    if (project.path != ":app" && !project.path.startsWith(":test")) {
        plugins.withId("com.android.library") {
            // Configure Android library to expose release component for publishing
            extensions.configure<com.android.build.gradle.LibraryExtension> {
                publishing {
                    singleVariant("release") {
                        withSourcesJar()
                        withJavadocJar()
                    }
                }
            }

            apply(plugin = "maven-publish")

            afterEvaluate {
                extensions.configure<PublishingExtension> {
                    publications {
                        create<MavenPublication>("release") {
                            from(components.findByName("release"))

                            groupId = "com.github.azikar24.WormaCeptor"
                            artifactId = project.path
                                .removePrefix(":")
                                .replace(":", "-")
                            version = findProperty("VERSION_NAME")?.toString() ?: "2.0.0"
                        }
                    }
                }
            }
        }
    }
}

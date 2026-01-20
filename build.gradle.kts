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
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

// Detekt configuration for root project
detekt {
    buildUponDefaultConfig = true
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    baseline = file("$rootDir/config/detekt/baseline.xml")
    parallel = true
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.compose.rules.detekt)
}

// Apply detekt and lint configuration to all subprojects
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        buildUponDefaultConfig = true
        config.setFrom("$rootDir/config/detekt/detekt.yml")
        baseline = file("$projectDir/detekt-baseline.xml")
        parallel = true
    }

    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
        "detektPlugins"(rootProject.libs.compose.rules.detekt)
    }

    // Configure Android Lint for both application and library modules
    plugins.withId("com.android.application") {
        extensions.configure<com.android.build.gradle.internal.dsl.BaseAppModuleExtension> {
            lint {
                baseline = file("$projectDir/lint-baseline.xml")
                lintConfig = file("$rootDir/lint.xml")
                abortOnError = true
                checkAllWarnings = true
                warningsAsErrors = false
            }
        }
    }

    plugins.withId("com.android.library") {
        extensions.configure<com.android.build.gradle.LibraryExtension> {
            lint {
                baseline = file("$projectDir/lint-baseline.xml")
                lintConfig = file("$rootDir/lint.xml")
                abortOnError = true
                checkAllWarnings = true
                warningsAsErrors = false
            }
        }
    }
}

// Spotless configuration
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", "**/generated/**")
        ktlint("1.2.1")
            .setEditorConfigPath("$rootDir/.editorconfig")
            .editorConfigOverride(
                mapOf(
                    // Disable rules that can't be auto-fixed or conflict with project conventions
                    "ktlint_standard_no-wildcard-imports" to "disabled",
                    "ktlint_standard_filename" to "disabled",
                    "ktlint_standard_max-line-length" to "disabled",
                    "ktlint_standard_backing-property-naming" to "disabled",
                    "ktlint_standard_property-naming" to "disabled",
                    "ktlint_standard_function-naming" to "disabled",
                    "ktlint_standard_value-parameter-comment" to "disabled",
                    "ktlint_standard_comment-wrapping" to "disabled",
                    "ktlint_standard_class-naming" to "disabled",
                ),
            )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint("1.2.1")
    }
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
                            artifactId =
                                project.path
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

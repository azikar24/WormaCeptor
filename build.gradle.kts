plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.navigation.safeargs) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
    alias(libs.plugins.owasp.dependency.check)
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.kover)
    alias(libs.plugins.gradle.doctor)
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

// =============================================================================
// OWASP Dependency-Check Configuration (Security Vulnerability Scanning)
// =============================================================================
dependencyCheck {
    failBuildOnCVSS = 7.0f // Fail on HIGH and CRITICAL vulnerabilities
    suppressionFile = "$rootDir/config/owasp/suppressions.xml"
    formats = listOf("HTML", "JSON", "SARIF")
    outputDirectory = "$buildDir/reports/dependency-check"
    nvd.apiKey = System.getenv("NVD_API_KEY") ?: ""
}

// =============================================================================
// Dependency Analysis Configuration (Unused/Misused Dependencies)
// =============================================================================
dependencyAnalysis {
    issues {
        all {
            onUnusedDependencies {
                severity("warn")
            }
            onUsedTransitiveDependencies {
                severity("warn")
            }
            onIncorrectConfiguration {
                severity("warn")
            }
            onRedundantPlugins {
                severity("warn")
            }
        }
        project(":test:architecture") {
            onUnusedDependencies { severity("ignore") }
        }
    }
}

// =============================================================================
// Binary Compatibility Validator Configuration (API Stability)
// =============================================================================
apiValidation {
    ignoredPackages +=
        listOf(
            "com.azikar24.wormaceptor.internal",
        )
    ignoredProjects +=
        listOf(
            "app",
            "test",
            "architecture",
        )
    nonPublicMarkers +=
        listOf(
            "kotlin.internal.InlineOnly",
        )
}

// =============================================================================
// Kover Configuration (Code Coverage)
// =============================================================================
koverReport {
    filters {
        excludes {
            classes(
                "*BuildConfig",
                "*_Factory",
                "*_HiltModules*",
                "*Hilt_*",
                "*_Impl",
                "*.di.*",
                "*.theme.*",
            )
            packages(
                "*.generated.*",
                "*.databinding.*",
            )
        }
    }
    verify {
        rule("Minimum Coverage") {
            minBound(50)
        }
    }
}

// =============================================================================
// Gradle Doctor Configuration (Build Health)
// =============================================================================
doctor {
    disallowMultipleDaemons.set(false)
    GCWarningThreshold.set(0.10f)
    daggerThreshold.set(5000)
    negativeAvoidanceThreshold.set(500)
    warnWhenNotUsingParallelGC.set(false)
    javaHome {
        ensureJavaHomeIsSet.set(true)
        ensureJavaHomeMatches.set(true)
        failOnError.set(false)
    }
}

// =============================================================================
// Git Hooks Installation
// =============================================================================
tasks.register<Copy>("installGitHooks") {
    group = "git"
    description = "Installs Git hooks for pre-commit checks"
    from("$rootDir/config/git-hooks/")
    into("$rootDir/.git/hooks/")
    fileMode = 0b111101101
}

tasks.named("prepareKotlinBuildScriptModel") {
    dependsOn("installGitHooks")
}

// =============================================================================
// Convenience Aggregate Tasks
// =============================================================================
tasks.register("codeQuality") {
    group = "verification"
    description = "Run all code quality checks"
    dependsOn("spotlessCheck", "detekt", "lint")
}

tasks.register("codeFormat") {
    group = "formatting"
    description = "Auto-format all code"
    dependsOn("spotlessApply")
}

tasks.register("securityCheck") {
    group = "verification"
    description = "Run security vulnerability scanning"
    dependsOn("dependencyCheckAnalyze")
}

tasks.register("coverageReport") {
    group = "verification"
    description = "Generate code coverage report"
    dependsOn("koverHtmlReport")
}

tasks.register("fullCheck") {
    group = "verification"
    description = "Run all checks"
    dependsOn("codeQuality", "securityCheck", "koverVerify", "buildHealth")
}

// Apply detekt, lint, and kover configuration to all subprojects
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlinx.kover")

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

        // Also publish pure Kotlin/JVM modules (e.g. domain:entities)
        plugins.withId("org.jetbrains.kotlin.jvm") {
            apply(plugin = "maven-publish")

            extensions.configure<JavaPluginExtension> {
                withSourcesJar()
            }

            afterEvaluate {
                extensions.configure<PublishingExtension> {
                    publications {
                        create<MavenPublication>("release") {
                            from(components.findByName("java"))

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

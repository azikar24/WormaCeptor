/*
 * Copyright AziKar24 21/12/2025.
 */

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "WormaCeptor"
include(":app")
// include(":WormaCeptor") // Decommissioned
// include(":WormaCeptor-imdb") // Decommissioned
// include(":WormaCeptor-no-op") // Decommissioned
// include(":WormaCeptor-persistence") // Decommissioned

// Phase 1: Foundation & Guardrails
include(":api:client")
include(":core:engine")
include(":domain:entities")
include(":domain:contracts")
include(":features:viewer")
include(":features:settings")
include(":features:sharing")
include(":infra:persistence:sqlite")
include(":infra:networking:okhttp")
include(":infra:parser:json")
include(":infra:parser:protobuf")
include(":platform:android")
include(":test:architecture")

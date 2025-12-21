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
include(":WormaCeptor")
include(":WormaCeptor-imdb")
include(":WormaCeptor-no-op")
include(":WormaCeptor-persistence")

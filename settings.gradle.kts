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
include(":api:common")
include(":api:impl:persistence")
include(":api:impl:imdb")
include(":api:impl:no-op")
include(":core:engine")
include(":domain:entities")
include(":domain:contracts")
include(":features:viewer")
include(":features:settings")
include(":features:sharing")
include(":features:logs")
include(":features:deviceinfo")
include(":features:preferences")
include(":features:database")
include(":features:filebrowser")
include(":features:memory")
include(":features:fps")
include(":features:websocket")
include(":features:webviewmonitor")
include(":features:cookies")
include(":features:cpu")
include(":features:touchvisualization")
include(":features:viewborders")
include(":features:location")
include(":features:pushsimulator")
include(":features:crypto")
include(":features:interception")
include(":features:viewhierarchy")
include(":features:leakdetection")
include(":features:threadviolation")
include(":features:gridoverlay")
include(":features:measurement")
include(":features:securestorage")
include(":features:composerender")
include(":features:ratelimit")
include(":features:pushtoken")
include(":features:loadedlibraries")
include(":features:dependenciesinspector")
include(":infra:persistence:sqlite")
include(":infra:networking:okhttp")
include(":infra:parser:form")
include(":infra:parser:html")
include(":infra:parser:image")
include(":infra:parser:json")
include(":infra:parser:multipart")
include(":infra:parser:pdf")
include(":infra:parser:protobuf")
include(":infra:parser:xml")
include(":infra:syntax:json")
include(":infra:syntax:xml")
include(":platform:android")
include(":test:architecture")

// IDE Integration (Android Studio Plugin - built separately with IntelliJ Gradle Plugin)
// include(":plugins:android-studio")

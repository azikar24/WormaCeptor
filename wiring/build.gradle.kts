plugins {
    id("wormaceptor.android.library")
}

android {
    namespace = "com.azikar24.wormaceptor.wiring"
}

dependencies {
    // Feature modules
    api(project(":features:preferences"))
    api(project(":features:logs"))
    api(project(":features:deviceinfo"))
    api(project(":features:settings"))
    api(project(":features:database"))
    api(project(":features:filebrowser"))
    api(project(":features:memory"))
    api(project(":features:fps"))
    api(project(":features:websocket"))
    api(project(":features:cpu"))
    api(project(":features:location"))
    api(project(":features:pushsimulator"))
    api(project(":features:leakdetection"))
    api(project(":features:threadviolation"))
    api(project(":features:webviewmonitor"))
    api(project(":features:crypto"))
    api(project(":features:securestorage"))
    api(project(":features:ratelimit"))
    api(project(":features:pushtoken"))
    api(project(":features:loadedlibraries"))
    api(project(":features:dependenciesinspector"))
    api(project(":features:recomposition"))
    api(project(":features:mockrules"))

    // Infrastructure: syntax highlighters
    api(project(":infra:syntax:json"))
    api(project(":infra:syntax:xml"))

    // Infrastructure: body parsers
    api(project(":infra:parser:protobuf"))
    api(project(":infra:parser:multipart"))
    api(project(":infra:parser:form"))
    api(project(":infra:parser:xml"))
    api(project(":infra:parser:html"))
    api(project(":infra:parser:json"))
    api(project(":infra:parser:image"))
    api(project(":infra:parser:pdf"))
}

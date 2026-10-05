package com.azikar24.wormaceptor.mcp.server.di

import com.azikar24.wormaceptor.mcp.server.ServerConfig
import com.azikar24.wormaceptor.mcp.server.WormaCeptorServer
import com.azikar24.wormaceptor.mcp.server.streaming.EventStreamManager
import org.koin.dsl.module

internal val serverModule = module {
    single { ServerConfig() }
    single { EventStreamManager() }
    single { WormaCeptorServer() }
}

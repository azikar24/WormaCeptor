package com.azikar24.wormaceptor.core.engine.di

import android.content.Context
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class WormaCeptorKoinTest {

    private val appContext = mockk<Context>()
    private val context = mockk<Context> { every { applicationContext } returns appContext }
    private val wormaCeptorModule = module { single { "wormaceptor" } }

    @AfterEach
    fun tearDown() {
        WormaCeptorKoin.cleanup()
        stopKoin()
    }

    @Test
    fun `does not start the global Koin context`() {
        WormaCeptorKoin.start(context, listOf(wormaCeptorModule))

        GlobalContext.getOrNull() shouldBe null
    }

    @Test
    fun `host startKoin after WormaCeptor init does not throw and stays separate`() {
        WormaCeptorKoin.start(context, listOf(wormaCeptorModule))

        startKoin { modules(module { single { 42 } }) }

        WormaCeptorKoin.get(String::class.java) shouldBe "wormaceptor"
        GlobalContext.get().getOrNull<String>() shouldBe null
        WormaCeptorKoin.getKoin().getOrNull<Int>() shouldBe null
    }

    @Test
    fun `provides application context even when host Koin has none`() {
        startKoin { modules(module { single { 42 } }) }

        WormaCeptorKoin.start(context, listOf(wormaCeptorModule))

        WormaCeptorKoin.get(Context::class.java) shouldBeSameInstanceAs appContext
    }

    @Test
    fun `second start is ignored`() {
        WormaCeptorKoin.start(context, listOf(wormaCeptorModule))
        WormaCeptorKoin.start(context, listOf(module { single { "other" } }))

        WormaCeptorKoin.get(String::class.java) shouldBe "wormaceptor"
    }
}

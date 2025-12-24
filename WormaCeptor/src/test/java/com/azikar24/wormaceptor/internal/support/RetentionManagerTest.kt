/*
 * Copyright AziKar24 23/12/2025.
 */

package com.azikar24.wormaceptor.internal.support

import android.content.Context
import android.content.SharedPreferences
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.WormaCeptorInterceptor
import com.azikar24.wormaceptor.internal.data.TransactionDao
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.util.*

class RetentionManagerTest {

    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var storage: WormaCeptorStorage
    private lateinit var dao: TransactionDao
    private lateinit var retentionManager: RetentionManager

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)
        storage = mockk(relaxed = true)
        dao = mockk(relaxed = true)

        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putLong(any(), any()) } returns editor
        
        mockkObject(WormaCeptor)
        every { WormaCeptor.storage } returns storage
        every { storage.transactionDao } returns dao

        retentionManager = RetentionManager(context, WormaCeptorInterceptor.Period.ONE_HOUR)
    }

    @Test
    fun `test doMaintenance deletes old transactions when cleanup is due`() {
        // Last cleanup was 1 hour ago
        val lastCleanup = System.currentTimeMillis() - (60 * 60 * 1000)
        every { sharedPreferences.getLong(any(), any()) } returns lastCleanup
        
        retentionManager.doMaintenance()
        
        verify { dao.deleteTransactionsBefore(any()) }
        verify { editor.putLong("last_cleanup", any()) }
    }

    @Test
    fun `test doMaintenance does nothing when cleanup is not due`() {
        // Last cleanup was 1 minute ago
        val lastCleanup = System.currentTimeMillis() - (60 * 1000)
        every { sharedPreferences.getLong(any(), any()) } returns lastCleanup
        
        retentionManager.doMaintenance()
        
        verify(exactly = 0) { dao.deleteTransactionsBefore(any()) }
    }
}
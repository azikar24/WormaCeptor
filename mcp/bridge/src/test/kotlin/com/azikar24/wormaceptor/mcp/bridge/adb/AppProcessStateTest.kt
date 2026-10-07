package com.azikar24.wormaceptor.mcp.bridge.adb

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppProcessStateTest {

    private val pkg = "com.azikar24.wormaceptorapp"

    // Shape from AOSP ActivityManagerService.dumpProcessesLocked + ProcessCachedOptimizerRecord.dump.
    private fun dumpsys(
        mainFrozen: Boolean,
        remoteFrozen: Boolean = false,
    ) = """
        ACTIVITY MANAGER RUNNING PROCESSES (dumpsys activity processes)
          All known processes:
          *APP* UID 10213 ProcessRecord{4c0e8b1 12345:$pkg/u0a213}
            user #0 uid=10213 gids={50213, 20213, 9997}
            mRequiredAbi=arm64-v8a instructionSet=null
            lastCompactTime=0 lastCompactProfile=0
            hasPendingCompaction=false    isFreezeExempt=false isPendingFreeze=false isFrozen=$mainFrozen
            earliestFreezableTimeMs=-3m2s
          *APP* UID 10213 ProcessRecord{9a7d2c4 12399:$pkg:remote/u0a213}
            user #0 uid=10213 gids={50213, 20213, 9997}
            hasPendingCompaction=false    isFreezeExempt=false isPendingFreeze=false isFrozen=$remoteFrozen
          PID mappings:
            PID #12345: ProcessRecord{4c0e8b1 12345:$pkg/u0a213}
    """.trimIndent()

    @Test
    fun `pidof output gives the first pid`() {
        assertEquals(12_345, parsePid("12345\n"))
        assertEquals(12_345, parsePid("12345 12399"))
        assertNull(parsePid(""))
        assertNull(parsePid("pidof: not found"))
    }

    @Test
    fun `frozen main process is detected`() {
        assertEquals(true, isMainProcessFrozen(dumpsys(mainFrozen = true), pkg))
    }

    @Test
    fun `a frozen secondary process does not count`() {
        assertEquals(false, isMainProcessFrozen(dumpsys(mainFrozen = false, remoteFrozen = true), pkg))
    }

    @Test
    fun `running process is not frozen`() {
        assertEquals(false, isMainProcessFrozen(dumpsys(mainFrozen = false), pkg))
    }

    @Test
    fun `dumpsys without the process says nothing`() {
        assertEquals(false, isMainProcessFrozen("ACTIVITY MANAGER RUNNING PROCESSES (dumpsys activity processes)", pkg))
    }

    @Test
    fun `state maps to an actionable hint`() {
        assertEquals(AppProcessState.Frozen, appProcessState(pid = 12_345, frozen = true))
        assertEquals(AppProcessState.NotRunning, appProcessState(pid = null, frozen = false))
        assertEquals(AppProcessState.Running, appProcessState(pid = 12_345, frozen = false))
        assertEquals(
            "The app is frozen in the background. Bring it to the foreground (or call bring_app_to_front), " +
                "then retry.",
            AppProcessState.Frozen.hint,
        )
        assertEquals(
            "The app isn't running. Launch it (or call bring_app_to_front), then retry.",
            AppProcessState.NotRunning.hint,
        )
        assertNull(AppProcessState.Running.hint)
    }

    @Test
    fun `monkey abort is a launch failure`() {
        assertNull(launchFailure(0, "Events injected: 1\n## Network stats: elapsed time=12ms"))
        assertEquals(
            "No activities found to run, monkey aborted.",
            launchFailure(0, "  bash arg: -p\n** No activities found to run, monkey aborted."),
        )
        assertEquals("error: device offline", launchFailure(1, "error: device offline"))
    }
}

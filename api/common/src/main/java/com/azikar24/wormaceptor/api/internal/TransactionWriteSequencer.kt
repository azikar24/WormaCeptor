package com.azikar24.wormaceptor.api.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Lets callers on any thread start a transaction without blocking: [start] picks the transaction id,
 * returns it at once and runs the insert on [scope]. [complete] waits for that id's insert before
 * running the update, so a completion is never applied before its start row exists.
 */
internal class TransactionWriteSequencer(private val scope: CoroutineScope) {

    // Holds only inserts still running, so transactions that never complete don't accumulate here.
    private val pendingInserts = ConcurrentHashMap<UUID, Job>()

    internal val pendingCount: Int get() = pendingInserts.size

    /** Schedules [insert] for a new transaction id and returns that id. */
    fun start(insert: suspend (id: UUID) -> Unit): UUID {
        val id = UUID.randomUUID()
        val job = scope.launch { insert(id) }
        pendingInserts[id] = job
        job.invokeOnCompletion { pendingInserts.remove(id, job) }
        return id
    }

    /**
     * Runs [update] once the insert for [id] has finished; skipped if that insert is still tracked and
     * failed. An insert that already finished is untracked, so [update] runs directly: it must tolerate a
     * missing row (as CaptureEngine.completeTransaction does) for an insert that failed earlier.
     */
    fun complete(
        id: UUID,
        update: suspend () -> Unit,
    ) {
        // Looked up now: once the insert finishes it is untracked, and a failure must still skip the update.
        val insert = pendingInserts[id]
        scope.launch {
            insert?.join()
            if (insert?.isCancelled == true) return@launch
            update()
        }
    }
}

package com.azikar24.wormaceptor.mcp.protocol

import kotlinx.serialization.Serializable

/** Result of `/api/wait/transaction`: the first new finished match, or null when the wait ran out. */
@Serializable
internal data class WaitForTransactionDto(
    val transaction: TransactionSummaryDto?,
    val waitedMs: Long,
)

/** Result of `/api/wait/crash`: the first crash recorded after the call started, or null on timeout. */
@Serializable
internal data class WaitForCrashDto(
    val crash: CrashDto?,
    val waitedMs: Long,
)

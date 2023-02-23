/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.details.fragments

import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper

interface NetworkTransactionFragment {
    fun transactionUpdated(transactionUIHelper: NetworkTransactionUIHelper?)
}
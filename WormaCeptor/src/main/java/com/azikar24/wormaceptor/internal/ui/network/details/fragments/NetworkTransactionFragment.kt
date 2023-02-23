/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.details.fragments

import com.azikar24.wormaceptor.internal.HttpTransactionUIHelper

interface NetworkTransactionFragment {
    fun transactionUpdated(transactionUIHelper: HttpTransactionUIHelper?)
}
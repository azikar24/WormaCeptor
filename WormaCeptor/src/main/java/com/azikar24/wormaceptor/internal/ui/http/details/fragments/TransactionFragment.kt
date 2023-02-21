/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.http.details.fragments

import com.azikar24.wormaceptor.internal.HttpTransactionUIHelper

interface TransactionFragment {
    fun transactionUpdated(transactionUIHelper: HttpTransactionUIHelper?)
}
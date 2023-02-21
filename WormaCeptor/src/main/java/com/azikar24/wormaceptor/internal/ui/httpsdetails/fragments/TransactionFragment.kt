/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.httpsdetails.fragments

import com.azikar24.wormaceptor.internal.HttpTransactionUIHelper

interface TransactionFragment {
    fun transactionUpdated(transactionUIHelper: HttpTransactionUIHelper?)
}
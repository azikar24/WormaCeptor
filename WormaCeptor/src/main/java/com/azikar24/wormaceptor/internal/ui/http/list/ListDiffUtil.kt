/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.http.list

import androidx.recyclerview.widget.DiffUtil
import com.azikar24.wormaceptor.internal.HttpTransactionUIHelper


class ListDiffUtil : DiffUtil.ItemCallback<HttpTransactionUIHelper>() {

    private var mSearchKey: String? = null

    fun setSearchKey(searchKey: String?) {
        mSearchKey = searchKey
    }

    override fun areItemsTheSame(oldItem: HttpTransactionUIHelper, newItem: HttpTransactionUIHelper): Boolean {
        newItem.searchKey = mSearchKey
        return oldItem.httpTransaction.id == newItem.httpTransaction.id
    }

    override fun areContentsTheSame(oldItem: HttpTransactionUIHelper, newItem: HttpTransactionUIHelper): Boolean {
        return oldItem.httpTransaction.method == newItem.httpTransaction.method
                && oldItem.httpTransaction.path == newItem.httpTransaction.path
                && oldItem.httpTransaction.host == newItem.httpTransaction.host
                && oldItem.getRequestStartTimeString() == newItem.getRequestStartTimeString()
                && oldItem.getStatus() == newItem.getStatus()
                && oldItem.httpTransaction.responseCode == newItem.httpTransaction.responseCode
                && oldItem.isSsl() == newItem.isSsl()
                && oldItem.httpTransaction.responseCode == newItem.httpTransaction.responseCode
                && oldItem.httpTransaction.tookMs == newItem.httpTransaction.tookMs
                && oldItem.getTotalSizeString() == newItem.getTotalSizeString()
                && oldItem.searchKey == newItem.searchKey

    }
}
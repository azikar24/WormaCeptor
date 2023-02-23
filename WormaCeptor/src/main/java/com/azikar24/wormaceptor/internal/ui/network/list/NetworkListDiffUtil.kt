/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.list

import androidx.recyclerview.widget.DiffUtil
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper


class NetworkListDiffUtil : DiffUtil.ItemCallback<NetworkTransactionUIHelper>() {

    private var mSearchKey: String? = null

    fun setSearchKey(searchKey: String?) {
        mSearchKey = searchKey
    }

    override fun areItemsTheSame(oldItem: NetworkTransactionUIHelper, newItem: NetworkTransactionUIHelper): Boolean {
        newItem.searchKey = mSearchKey
        return oldItem.networkTransaction.id == newItem.networkTransaction.id
    }

    override fun areContentsTheSame(oldItem: NetworkTransactionUIHelper, newItem: NetworkTransactionUIHelper): Boolean {
        return oldItem.networkTransaction.method == newItem.networkTransaction.method
                && oldItem.networkTransaction.path == newItem.networkTransaction.path
                && oldItem.networkTransaction.host == newItem.networkTransaction.host
                && oldItem.getRequestStartTimeString() == newItem.getRequestStartTimeString()
                && oldItem.getStatus() == newItem.getStatus()
                && oldItem.networkTransaction.responseCode == newItem.networkTransaction.responseCode
                && oldItem.isSsl() == newItem.isSsl()
                && oldItem.networkTransaction.responseCode == newItem.networkTransaction.responseCode
                && oldItem.networkTransaction.tookMs == newItem.networkTransaction.tookMs
                && oldItem.getTotalSizeString() == newItem.getTotalSizeString()
                && oldItem.searchKey == newItem.searchKey

    }
}
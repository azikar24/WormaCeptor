/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.internal.ui.network.list.viewholders.EmptyNetworkTransactionViewHolder
import com.azikar24.wormaceptor.internal.ui.network.list.viewholders.NetworkTransactionViewHolder

class NetworkTransactionAdapter(val context: Context, networkListDiffUtil: NetworkListDiffUtil, val mListener: Listener?) : PagingDataAdapter<NetworkTransactionUIHelper, RecyclerView.ViewHolder>(networkListDiffUtil) {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val mColorUtil: ColorUtil = ColorUtil.getInstance(context)
    private var mSearchKey: String? = null

    init {
        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                mListener?.onItemsInserted(positionStart)
            }
        })
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) == null) {
            EMPTY_VIEW
        } else {
            TRANSACTION_VIEW
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val transactionUIHelper = getItem(position) ?: return
        val mHolder = holder as NetworkTransactionViewHolder

        mHolder.itemView.setOnClickListener {
            mListener?.onTransactionClicked(transactionUIHelper)
        }

        mHolder.codeTextView.text = getHighlightedText(transactionUIHelper.networkTransaction.responseCode?.toString() ?: "")
        mHolder.pathTextView.text = getHighlightedText("[${transactionUIHelper.networkTransaction.method}] ${transactionUIHelper.networkTransaction.path}")
        mHolder.hostTextView.text = transactionUIHelper.networkTransaction.host?.let { getHighlightedText(it) }
        mHolder.sslImageView.visibility = if (transactionUIHelper.isSsl()) View.VISIBLE else View.GONE

        mHolder.startTextView.text = transactionUIHelper.getRequestStartTimeString()

        if (transactionUIHelper.getStatus() == NetworkTransactionUIHelper.Status.Complete) {
            holder.codeTextView.text = getHighlightedText(java.lang.String.valueOf(transactionUIHelper.networkTransaction.responseCode))
            holder.durationTextView.text = mHolder.codeTextView.context.getString(R.string.duration_ms, transactionUIHelper.networkTransaction.tookMs)
            holder.sizeTextView.text = transactionUIHelper.getTotalSizeString()
        } else {
            holder.codeTextView.text = null
            holder.durationTextView.text = null
            holder.sizeTextView.text = null
        }
        if (transactionUIHelper.getStatus() == NetworkTransactionUIHelper.Status.Failed) {
            holder.codeTextView.text = context.getText(R.string.failed_status)
        }

        val color = mColorUtil.getTransactionColor(transactionUIHelper, true)
        holder.pathTextView.setTextColor(color)
        holder.codeTextView.setTextColor(color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TRANSACTION_VIEW) {
            NetworkTransactionViewHolder(mLayoutInflater.inflate(R.layout.list_item_network_transaction, parent, false))
        } else {
            EmptyNetworkTransactionViewHolder(mLayoutInflater.inflate(R.layout.list_item_empty_network_transaction, parent, false))
        }
    }

    fun setSearchKey(value: String?): NetworkTransactionAdapter = apply {
        mSearchKey = value
    }

    interface Listener {
        fun onTransactionClicked(transactionUIHelper: NetworkTransactionUIHelper?)
        fun onItemsInserted(firstInsertedItemPosition: Int)
    }

    private fun getHighlightedText(text: String): CharSequence? {
        return FormatUtils.formatTextHighlight(context, text, mSearchKey)
    }

    companion object {
        private const val EMPTY_VIEW = 1
        private const val TRANSACTION_VIEW = 2
    }
}
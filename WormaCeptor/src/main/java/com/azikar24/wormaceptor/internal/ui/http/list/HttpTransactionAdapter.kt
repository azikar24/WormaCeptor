/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.http.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.HttpTransactionUIHelper
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.internal.ui.http.list.viewholders.EmptyTransactionViewHolder
import com.azikar24.wormaceptor.internal.ui.http.list.viewholders.TransactionViewHolder

class HttpTransactionAdapter(val context: Context, listDiffUtil: ListDiffUtil, val mListener: Listener?) : PagedListAdapter<HttpTransactionUIHelper, RecyclerView.ViewHolder>(listDiffUtil) {
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
        val mHolder = holder as TransactionViewHolder

        mHolder.itemView.setOnClickListener {
            mListener?.onTransactionClicked(transactionUIHelper)
        }

        mHolder.codeTextView.text = getHighlightedText(transactionUIHelper.httpTransaction.responseCode?.toString() ?: "")
        mHolder.pathTextView.text = getHighlightedText("[${transactionUIHelper.httpTransaction.method}] ${transactionUIHelper.httpTransaction.path}")
        mHolder.hostTextView.text = transactionUIHelper.httpTransaction.host?.let { getHighlightedText(it) }
        mHolder.sslImageView.visibility = if (transactionUIHelper.isSsl()) View.VISIBLE else View.GONE

        mHolder.startTextView.text = transactionUIHelper.getRequestStartTimeString()

        if (transactionUIHelper.getStatus() == HttpTransactionUIHelper.Status.Complete) {
            holder.codeTextView.text = getHighlightedText(java.lang.String.valueOf(transactionUIHelper.httpTransaction.responseCode))
            holder.durationTextView.text = "${transactionUIHelper.httpTransaction.tookMs} ms"
            holder.sizeTextView.text = transactionUIHelper.getTotalSizeString()
        } else {
            holder.codeTextView.text = null
            holder.durationTextView.text = null
            holder.sizeTextView.text = null
        }
        if (transactionUIHelper.getStatus() == HttpTransactionUIHelper.Status.Failed) {
            holder.codeTextView.text = "!!!"
        }

        val color = mColorUtil.getTransactionColor(transactionUIHelper, true)
        holder.pathTextView.setTextColor(color)
        holder.codeTextView.setTextColor(color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TRANSACTION_VIEW) {
            TransactionViewHolder(mLayoutInflater.inflate(R.layout.list_item_transaction, parent, false))
        } else {
            EmptyTransactionViewHolder(mLayoutInflater.inflate(R.layout.list_item_empty_transaction, parent, false))
        }
    }

    fun setSearchKey(value: String?): HttpTransactionAdapter = apply {
        mSearchKey = value
    }

    interface Listener {
        fun onTransactionClicked(transactionUIHelper: HttpTransactionUIHelper?)
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
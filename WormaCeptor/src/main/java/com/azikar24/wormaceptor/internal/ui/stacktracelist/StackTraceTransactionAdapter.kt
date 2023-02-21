/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.stacktracelist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.data.StackTraceTransaction
import com.azikar24.wormaceptor.internal.ui.stacktracelist.viewholders.EmptyStackTraceTransactionViewHolder
import com.azikar24.wormaceptor.internal.ui.stacktracelist.viewholders.TransactionViewHolder

class StackTraceTransactionAdapter(val context: Context, listDiffUtil: ListDiffUtil, val mListener: Listener?) : PagedListAdapter<StackTraceTransaction, RecyclerView.ViewHolder>(listDiffUtil) {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) == null) {
            EMPTY_VIEW
        } else {
            TRANSACTION_VIEW
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val stacktraceTransaction = getItem(position) ?: return
        val mHolder = holder as TransactionViewHolder

        mHolder.itemView.setOnClickListener {
            mListener?.onTransactionClicked(stacktraceTransaction)
        }

        mHolder.errorTypeTextView.text = stacktraceTransaction.throwable
        mHolder.filenameTextView.text = stacktraceTransaction.stackTrace?.getOrNull(0)?.let { "${it.className} (${it.lineNumber})" }
        mHolder.dateTextView.text = stacktraceTransaction.stackTraceDate.toString()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TRANSACTION_VIEW) {
            TransactionViewHolder(mLayoutInflater.inflate(R.layout.list_item_stack_trace_transaction, parent, false))
        } else {
            EmptyStackTraceTransactionViewHolder(mLayoutInflater.inflate(R.layout.list_item_empty_stack_trace_transaction, parent, false))
        }
    }

    interface Listener {
        fun onTransactionClicked(stackTraceTransaction: StackTraceTransaction?)
    }

    companion object {
        private const val EMPTY_VIEW = 1
        private const val TRANSACTION_VIEW = 2
    }
}
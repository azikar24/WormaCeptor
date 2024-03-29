/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.crashes.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.support.formatted
import com.azikar24.wormaceptor.internal.ui.crashes.list.viewholders.CrashTransactionViewHolder
import com.azikar24.wormaceptor.internal.ui.crashes.list.viewholders.EmptyCrashTransactionViewHolder

class CrashTransactionAdapter(
    val context: Context,
    crashListDiffUtil: CrashListDiffUtil,
    private val mListener: Listener?
) : PagingDataAdapter<CrashTransaction, RecyclerView.ViewHolder>(crashListDiffUtil) {
    private val mLayoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) == null) {
            EMPTY_VIEW
        } else {
            TRANSACTION_VIEW
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val crashTransaction = getItem(position) ?: return
        val mHolder = holder as CrashTransactionViewHolder

        mHolder.itemView.setOnClickListener {
            mListener?.onTransactionClicked(crashTransaction)
        }

        mHolder.errorTypeTextView.text = crashTransaction.throwable
        mHolder.filenameTextView.text = crashTransaction.crashList?.getOrNull(0)?.let { "${it.className} (${it.lineNumber})" } ?: context.getString(R.string.unknown)
        mHolder.dateTextView.text = crashTransaction.crashDate?.formatted()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TRANSACTION_VIEW) {
            CrashTransactionViewHolder(mLayoutInflater.inflate(R.layout.row_item_crash_transaction, parent, false))
        } else {
            EmptyCrashTransactionViewHolder(mLayoutInflater.inflate(R.layout.row_item_crash_transaction_empty, parent, false))
        }
    }

    interface Listener {
        fun onTransactionClicked(crashTransaction: CrashTransaction?)
    }

    companion object {
        private const val EMPTY_VIEW = 1
        private const val TRANSACTION_VIEW = 2
    }
}
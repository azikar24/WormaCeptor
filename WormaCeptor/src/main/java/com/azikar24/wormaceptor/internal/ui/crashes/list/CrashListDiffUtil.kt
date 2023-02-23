/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.crashes.list

import androidx.recyclerview.widget.DiffUtil
import com.azikar24.wormaceptor.internal.data.StackTraceTransaction


class CrashListDiffUtil : DiffUtil.ItemCallback<StackTraceTransaction>() {

    override fun areItemsTheSame(oldItem: StackTraceTransaction, newItem: StackTraceTransaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StackTraceTransaction, newItem: StackTraceTransaction): Boolean {
        return oldItem.stackTrace == newItem.stackTrace
                && oldItem.stackTraceDate == newItem.stackTraceDate
    }
}
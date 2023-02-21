/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.stacktracelist

import androidx.recyclerview.widget.DiffUtil
import com.azikar24.wormaceptor.internal.data.StackTraceTransaction


class ListDiffUtil : DiffUtil.ItemCallback<StackTraceTransaction>() {

    override fun areItemsTheSame(oldItem: StackTraceTransaction, newItem: StackTraceTransaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StackTraceTransaction, newItem: StackTraceTransaction): Boolean {
        return oldItem.stackTrace == newItem.stackTrace
                && oldItem.stackTraceDate == newItem.stackTraceDate
    }
}
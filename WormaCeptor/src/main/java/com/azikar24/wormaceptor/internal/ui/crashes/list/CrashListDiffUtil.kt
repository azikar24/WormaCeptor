/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.crashes.list

import androidx.recyclerview.widget.DiffUtil
import com.azikar24.wormaceptor.internal.data.CrashTransaction


class CrashListDiffUtil : DiffUtil.ItemCallback<CrashTransaction>() {

    override fun areItemsTheSame(oldItem: CrashTransaction, newItem: CrashTransaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CrashTransaction, newItem: CrashTransaction): Boolean {
        return oldItem.crashList == newItem.crashList
                && oldItem.crashDate == newItem.crashDate
    }
}
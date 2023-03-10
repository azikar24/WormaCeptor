/*
 * Copyright AziKar24 22/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.crashes.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.RowItemCrashDetailsBinding

class CrashAdapter(val items: List<StackTraceElement>) : RecyclerView.Adapter<CrashAdapter.ViewHolder>() {

    class ViewHolder(val binding: RowItemCrashDetailsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowItemCrashDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentData = items[position]
        val context = holder.binding.root.context
        holder.binding.crashDetails.text = context.getString(
            R.string.stack_trace_string,
            currentData.className,
            currentData.methodName,
            currentData.fileName,
            currentData.lineNumber
        )
    }

    override fun getItemCount() = items.size
}
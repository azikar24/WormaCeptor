/*
 * Copyright AziKar24 22/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.stacktrace.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azikar24.wormaceptor.databinding.RowStackTraceDetailsBinding

class StackTraceAdapter(val items: List<StackTraceElement>) : RecyclerView.Adapter<StackTraceAdapter.ViewHolder>() {

    class ViewHolder(val binding: RowStackTraceDetailsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowStackTraceDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentData = items[position]
        holder.binding.stacktraceDetails.text = "at ${currentData.className}.${currentData.methodName}(${currentData.fileName}:${currentData.lineNumber})"
    }

    override fun getItemCount() = items.size
}
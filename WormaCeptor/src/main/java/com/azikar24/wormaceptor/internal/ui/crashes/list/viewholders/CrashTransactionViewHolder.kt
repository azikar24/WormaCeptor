/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.crashes.list.viewholders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azikar24.wormaceptor.R

class CrashTransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val errorTypeTextView: TextView = view.findViewById(R.id.errorTypeTextView)
    val filenameTextView: TextView = view.findViewById(R.id.filenameTextView)
    val dateTextView: TextView = view.findViewById(R.id.dateTextView)
}
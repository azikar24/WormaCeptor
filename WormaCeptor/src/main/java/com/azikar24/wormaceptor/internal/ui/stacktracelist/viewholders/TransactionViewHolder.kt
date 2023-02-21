/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.stacktracelist.viewholders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azikar24.wormaceptor.R

class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val errorTypeTextView: TextView = view.findViewById(R.id.errorTypeTextView)
    val filenameTextView: TextView = view.findViewById(R.id.filenameTextView)
    val dateTextView: TextView = view.findViewById(R.id.dateTextView)
}
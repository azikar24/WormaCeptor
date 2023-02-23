/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.list.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azikar24.wormaceptor.R

class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val codeTextView: TextView = view.findViewById(R.id.codeTextView)
    val pathTextView: TextView = view.findViewById(R.id.pathTextView)
    val hostTextView: TextView = view.findViewById(R.id.hostTextView)
    val sslImageView: ImageView = view.findViewById(R.id.sslImageView)
    val startTextView: TextView = view.findViewById(R.id.startTextView)
    val durationTextView: TextView = view.findViewById(R.id.durationTextView)
    val sizeTextView: TextView = view.findViewById(R.id.sizeTextView)
}
/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.details.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.FragmentNetworkTransactionOverviewBinding
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
import com.azikar24.wormaceptor.internal.support.formatted

class NetworkNetworkTransactionOverviewFragment : Fragment(), NetworkTransactionFragment {
    private lateinit var binding: FragmentNetworkTransactionOverviewBinding
    private var mTransactionUIHelper: NetworkTransactionUIHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentNetworkTransactionOverviewBinding.inflate(inflater, null, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateUI()
    }

    private fun populateUI() {
        if (!isAdded) return
        val transactionHelper = mTransactionUIHelper ?: return
        binding.detailsUrlTextView.text = transactionHelper.networkTransaction.url
        binding.methodTextView.text = transactionHelper.networkTransaction.method
        binding.protocolTextView.text = transactionHelper.networkTransaction.protocol
        binding.statusTextView.text = transactionHelper.getStatus().toString()
        binding.responseTextView.text = transactionHelper.getResponseSummaryText()
        binding.sslTextView.text = getString(if (transactionHelper.isSsl()) R.string.yes else R.string.no)
        binding.requestTimeTextView.text = transactionHelper.networkTransaction.requestDate?.formatted()
        binding.responseTimeTextView.text = transactionHelper.networkTransaction.responseDate?.formatted()
        binding.durationTextView.text = binding.root.context.getString(R.string.duration_ms, transactionHelper.networkTransaction.tookMs)
        binding.requestSizeTextView.text = transactionHelper.getRequestSizeString()
        binding.responseSizeTextView.text = transactionHelper.getResponseSizeString()
        binding.totalSizeTextView.text = transactionHelper.getTotalSizeString()
    }

    override fun transactionUpdated(transactionUIHelper: NetworkTransactionUIHelper?) {
        mTransactionUIHelper = transactionUIHelper
        populateUI()
    }
}
/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.details.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.FragmentNetworkTransactionOverviewBinding
import com.azikar24.wormaceptor.internal.HttpTransactionUIHelper
import com.azikar24.wormaceptor.internal.support.formatted

class NetworkNetworkTransactionOverviewFragment : Fragment(), NetworkTransactionFragment {
    lateinit var binding: FragmentNetworkTransactionOverviewBinding
    private var mTransactionUIHelper: HttpTransactionUIHelper? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentNetworkTransactionOverviewBinding.inflate(inflater, null, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        popuplateUI()
    }

    private fun popuplateUI() {
        if (!isAdded) return
        val transactionHelper = mTransactionUIHelper ?: return
        binding.detailsUrlTextView.text = transactionHelper.httpTransaction.url
        binding.methodTextView.text = transactionHelper.httpTransaction.method
        binding.protocolTextView.text = transactionHelper.httpTransaction.protocol
        binding.statusTextView.text = transactionHelper.getStatus().toString()
        binding.responseTextView.text = transactionHelper.getResponseSummaryText()
        binding.sslTextView.text = getString(if (transactionHelper.isSsl()) R.string.yes else R.string.no)
        binding.requestTimeTextView.text = transactionHelper.httpTransaction.requestDate?.formatted()
        binding.responseTimeTextView.text = transactionHelper.httpTransaction.responseDate?.formatted()
        binding.durationTextView.text = "${transactionHelper.httpTransaction.tookMs} ms"
        binding.requestSizeTextView.text = transactionHelper.getRequestSizeString()
        binding.responseSizeTextView.text = transactionHelper.getResponseSizeString()
        binding.totalSizeTextView.text = transactionHelper.getTotalSizeString()
    }

    override fun transactionUpdated(transactionUIHelper: HttpTransactionUIHelper?) {
        mTransactionUIHelper = transactionUIHelper
        popuplateUI()
    }

}
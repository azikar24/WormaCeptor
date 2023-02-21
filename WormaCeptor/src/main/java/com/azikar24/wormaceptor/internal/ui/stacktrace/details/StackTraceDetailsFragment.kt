/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.stacktrace.details

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.FragmentStackTraceDetailsBinding
import com.azikar24.wormaceptor.internal.data.StackTraceTransaction
import com.azikar24.wormaceptor.internal.ui.stacktrace.list.StackTraceTransactionViewModel

class StackTraceDetailsFragment : Fragment() {
    lateinit var binding: FragmentStackTraceDetailsBinding
    private val args: StackTraceDetailsFragmentArgs by navArgs()
    private val viewModel: StackTraceTransactionViewModel by viewModels()
    lateinit var currentData: StackTraceTransaction

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentStackTraceDetailsBinding.inflate(inflater, container,false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        val stackTrace = viewModel.getAllStackTraces()
        stackTrace?.observe(viewLifecycleOwner) { pagedListStackTrace ->
            if(pagedListStackTrace.isNullOrEmpty()) return@observe
            val item = pagedListStackTrace.firstOrNull { it.id == args.id }
            if(item == null) {
                findNavController().navigateUp()
                return@observe
            }
            currentData = item
            populateUI()
        }

    }

    private fun populateUI() {
        binding.stackTraceTextView.text =  currentData?.throwable + "\n\n" +currentData.stackTrace?.joinToString { "at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})\n\n" }?.replace(",","")
    }
}
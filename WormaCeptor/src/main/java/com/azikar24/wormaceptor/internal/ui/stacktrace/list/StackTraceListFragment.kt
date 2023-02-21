/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.stacktrace.list

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.FragmentStackTraceListBinding
import com.azikar24.wormaceptor.internal.data.StackTraceTransaction

class StackTraceListFragment : Fragment() {
    lateinit var binding: FragmentStackTraceListBinding
    val viewModel: StackTraceTransactionViewModel by viewModels()

    private lateinit var mStackTraceTransactionAdapter: StackTraceTransactionAdapter

    private val mListDiffUtil: ListDiffUtil = ListDiffUtil()

    private var mCurrentSubscription: LiveData<PagedList<StackTraceTransaction>>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentStackTraceListBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolBar()
        setupList()
        loadResults(viewModel.getAllStackTraces(null))
    }

    private fun setupToolBar() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }
    }

    private fun setupList() {
        mStackTraceTransactionAdapter = StackTraceTransactionAdapter(requireContext(), mListDiffUtil, object : StackTraceTransactionAdapter.Listener {
            override fun onTransactionClicked(stackTraceTransaction: StackTraceTransaction?) {
                if (stackTraceTransaction != null) {
//                    findNavController().navigate(HttpsFragmentDirections.actionHttpsListFragmentToDetailsFragment(transactionUIHelper.httpTransaction.id))
                }
            }
        })

        binding.stackTraceTransactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.stackTraceTransactionRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.stackTraceTransactionRecyclerView.adapter = mStackTraceTransactionAdapter
    }

    private fun loadResults(pagedListLiveData: LiveData<PagedList<StackTraceTransaction>>?) {
        if (mCurrentSubscription?.hasObservers() == true) {
            mCurrentSubscription?.removeObservers(this)
        }

        pagedListLiveData?.let {
            mCurrentSubscription = it
            it.observe(viewLifecycleOwner) { transactionPagedList ->
                mStackTraceTransactionAdapter.submitList(transactionPagedList)
            }
        }
    }
}
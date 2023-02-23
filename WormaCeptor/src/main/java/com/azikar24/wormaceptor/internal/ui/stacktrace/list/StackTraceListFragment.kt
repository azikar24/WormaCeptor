/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.stacktrace.list

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.FragmentStackTraceListBinding
import com.azikar24.wormaceptor.internal.data.StackTraceTransaction
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.getApplicationName

class StackTraceListFragment : Fragment() {
    private lateinit var mColorUtil: ColorUtil
    lateinit var binding: FragmentStackTraceListBinding
    private val viewModel: StackTraceTransactionViewModel by viewModels()

    private lateinit var mStackTraceTransactionAdapter: StackTraceTransactionAdapter

    private val mListDiffUtil: ListDiffUtil = ListDiffUtil()

    private var mCurrentSubscription: LiveData<PagedList<StackTraceTransaction>>? = null

    private val menuProvider
        get() = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) = Unit

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> activity?.let { Navigation.findNavController(it, R.id.navigationView).navigateUp() }
                    else -> {}
                }
                return true
            }

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentStackTraceListBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mColorUtil = ColorUtil.getInstance(requireContext())
        setupToolBar()
        setupList()
        loadResults(viewModel.getAllStackTraces())
    }

    private fun setupToolBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
            this.setBackgroundDrawable(ColorDrawable(mColorUtil.colorPrimary))
            title = getString(R.string.crashes)
            subtitle = activity?.getApplicationName()
            requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
        }
    }

    private fun setupList() {
        mStackTraceTransactionAdapter = StackTraceTransactionAdapter(requireContext(), mListDiffUtil, object : StackTraceTransactionAdapter.Listener {
            override fun onTransactionClicked(stackTraceTransaction: StackTraceTransaction?) {
                if (stackTraceTransaction != null) {
                    findNavController().navigate(StackTraceListFragmentDirections.actionStackTraceListFragment2ToStackTraceDetailsFragment(stackTraceTransaction.id))
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
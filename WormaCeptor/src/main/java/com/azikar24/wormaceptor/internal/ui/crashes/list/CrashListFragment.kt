/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.crashes.list

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.FragmentCrashListBinding
import com.azikar24.wormaceptor.internal.data.StackTraceTransaction
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.getApplicationName

class CrashListFragment : Fragment() {
    private lateinit var mColorUtil: ColorUtil
    lateinit var binding: FragmentCrashListBinding
    private val viewModel: CrashTransactionViewModel by viewModels()

    private lateinit var mCrashTransactionAdapter: CrashTransactionAdapter

    private val mCrashListDiffUtil: CrashListDiffUtil = CrashListDiffUtil()

    private var mCurrentSubscription: LiveData<PagedList<StackTraceTransaction>>? = null

    private val menuProvider
        get() = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) = Unit

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> activity?.finish()
                    else -> {}
                }
                return true
            }

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentCrashListBinding.inflate(inflater, container, false).also {
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
        mCrashTransactionAdapter = CrashTransactionAdapter(requireContext(), mCrashListDiffUtil, object : CrashTransactionAdapter.Listener {
            override fun onTransactionClicked(stackTraceTransaction: StackTraceTransaction?) {
                if (stackTraceTransaction != null) {
                    findNavController().navigate(CrashListFragmentDirections.actionCrashListFragment2ToCrashDetailsFragment(stackTraceTransaction.id))
                }
            }
        })

        binding.stackTraceTransactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.stackTraceTransactionRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.stackTraceTransactionRecyclerView.adapter = mCrashTransactionAdapter
    }

    private fun loadResults(pagedListLiveData: LiveData<PagedList<StackTraceTransaction>>?) {
        if (mCurrentSubscription?.hasObservers() == true) {
            mCurrentSubscription?.removeObservers(this)
        }

        pagedListLiveData?.let {
            mCurrentSubscription = it
            it.observe(viewLifecycleOwner) { transactionPagedList ->
                mCrashTransactionAdapter.submitList(transactionPagedList)
            }
        }
    }
}
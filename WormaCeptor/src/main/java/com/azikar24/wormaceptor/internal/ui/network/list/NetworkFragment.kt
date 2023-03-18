/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.list

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.FragmentNetworkListBinding
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.NotificationHelper
import com.azikar24.wormaceptor.internal.support.event.Callback
import com.azikar24.wormaceptor.internal.support.event.Debouncer
import com.azikar24.wormaceptor.internal.support.event.Sampler
import com.azikar24.wormaceptor.internal.support.getApplicationName
import kotlinx.coroutines.flow.collectLatest

class NetworkFragment : Fragment(), SearchView.OnQueryTextListener {

    lateinit var binding: FragmentNetworkListBinding

    private lateinit var mNetworkTransactionAdapter: NetworkTransactionAdapter

    private val mNetworkListDiffUtil: NetworkListDiffUtil = NetworkListDiffUtil()
    private val mViewModel: NetworkTransactionViewModel by viewModels()
    private var mCurrentSubscription: PagingData<NetworkTransactionUIHelper>? = null
    val mColorUtil: ColorUtil by lazy {
        ColorUtil.getInstance(requireContext())
    }

    private val menuProvider: MenuProvider
        get() = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.network_list_menu, menu)
                menu.findItem(R.id.search)?.let { searchMenuItem ->
                    val searchView = searchMenuItem.actionView as SearchView
                    searchView.setOnQueryTextListener(this@NetworkFragment)
                    val searchEditText: EditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
                    searchEditText.setTextColor(mColorUtil.mColorWhite)
                    searchEditText.setHintTextColor(mColorUtil.mColorWhite)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.clear -> {
                        mViewModel.clearAll()
                        NotificationHelper.clearBuffer()
                        true
                    }
                    android.R.id.home -> {
                        activity?.finish()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }

    private val mTransactionSampler: Sampler<TransactionListWithSearchKeyModel> = Sampler(100, object : Callback<TransactionListWithSearchKeyModel> {
        override fun onEmit(event: TransactionListWithSearchKeyModel) {
            mNetworkListDiffUtil.setSearchKey(event.mSearchKey)
            lifecycleScope.launchWhenStarted{
                mNetworkTransactionAdapter.setSearchKey(event.mSearchKey).submitData(event.pagedList)
            }
        }
    })

    private val mSearchDebouncer: Debouncer<String> = Debouncer(500, object : Callback<String> {
        override fun onEmit(event: String) {
            loadResults(event)
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentNetworkListBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    private fun setupToolBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
            title = getString(R.string.network)
            subtitle = activity?.getApplicationName()
            this.setBackgroundDrawable(ColorDrawable(mColorUtil.colorPrimary))
            requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolBar()
        setupList()
        loadResults()
    }

    private fun setupList() {
        mNetworkTransactionAdapter = NetworkTransactionAdapter(requireContext(), mNetworkListDiffUtil, object : NetworkTransactionAdapter.Listener {
            override fun onTransactionClicked(transactionUIHelper: NetworkTransactionUIHelper?) {
                if (transactionUIHelper != null) {
                    findNavController().navigate(NetworkFragmentDirections.actionNetworkListFragmentToDetailsFragment(transactionUIHelper.networkTransaction.id))
                }
            }

            override fun onItemsInserted(firstInsertedItemPosition: Int) {
                binding.transactionRecyclerView.smoothScrollToPosition(firstInsertedItemPosition)
            }
        })

        binding.transactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.transactionRecyclerView.adapter = mNetworkTransactionAdapter
    }


    private fun loadResults(searchKey: String? = null) {
        mViewModel.fetchData(searchKey)
        lifecycleScope.launchWhenStarted {
            mViewModel.pageEventFlow.collectLatest {
                mCurrentSubscription = it
                mTransactionSampler.consume(TransactionListWithSearchKeyModel(searchKey, it))
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            mSearchDebouncer.consume(newText)
        }
        return true
    }

    internal class TransactionListWithSearchKeyModel(val mSearchKey: String?, val pagedList: PagingData<NetworkTransactionUIHelper>)
}
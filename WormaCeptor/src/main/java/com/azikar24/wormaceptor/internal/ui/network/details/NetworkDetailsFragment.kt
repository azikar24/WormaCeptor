/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.details

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.FragmentNetworkDetailsBinding
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.internal.support.share
import com.azikar24.wormaceptor.internal.ui.network.details.fragments.NetworkNetworkTransactionOverviewFragment
import com.azikar24.wormaceptor.internal.ui.network.details.fragments.NetworkNetworkTransactionPayloadFragment
import com.google.android.material.tabs.TabLayoutMediator

class NetworkDetailsFragment : Fragment() {
    private lateinit var binding: FragmentNetworkDetailsBinding
    private val viewModel: NetworkDetailTransactionViewModel by viewModels()
    private lateinit var mColorUtil: ColorUtil
    private var mAdapter: NetworkDetailsAdapter? = null
    lateinit var currentData: NetworkTransactionUIHelper

    private val args: NetworkDetailsFragmentArgs by navArgs()


    private val menuProvider: MenuProvider
        get() = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.network_details_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.shareText -> {
                        requireContext().share(FormatUtils.getShareText(requireContext(), currentData).toString())
                        true
                    }
                    R.id.shareCurl -> {
                        requireContext().share(FormatUtils.getShareCurlCommand(currentData))
                        true
                    }
                    android.R.id.home -> {
                        findNavController().navigateUp()
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentNetworkDetailsBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mColorUtil = ColorUtil.getInstance(requireContext())

        val transactionUIHelper = viewModel.getTransactionWithId(args.id)
        setupToolbar()

        transactionUIHelper?.observe(viewLifecycleOwner) {
            currentData = it
            populateUI()
        }

        setupViewPager()
    }

    private fun populateUI() {
        (activity as? AppCompatActivity)?.supportActionBar?.run {
            this.setBackgroundDrawable(ColorDrawable(mColorUtil.colorPrimary))
            setBackgroundDrawable(ColorDrawable(mColorUtil.getTransactionColor(currentData)))
            title = "[${currentData.networkTransaction.method}] ${currentData.networkTransaction.path}"
            subtitle = ""
        }

        binding.tabsLayout.setBackgroundColor(mColorUtil.getTransactionColor(currentData))

        mAdapter?.let {
            for (fragment in it.fragments) {
                fragment.transactionUpdated(currentData)

            }
        }
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.supportActionBar?.run {
            setDisplayShowHomeEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
            requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
        }
    }

    private fun setupViewPager() {
        mAdapter = NetworkDetailsAdapter(requireActivity())
        mAdapter?.addFragment(NetworkNetworkTransactionOverviewFragment(), getString(R.string.overview))
        mAdapter?.addFragment(NetworkNetworkTransactionPayloadFragment.newInstance(NetworkNetworkTransactionPayloadFragment.TYPE_REQUEST), getString(R.string.request))
        mAdapter?.addFragment(NetworkNetworkTransactionPayloadFragment.newInstance(NetworkNetworkTransactionPayloadFragment.TYPE_RESPONSE), getString(R.string.response))

        binding.viewpager2.adapter = mAdapter
        binding.viewpager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                SELECTED_TAB_POSITION = position
            }

        })

        binding.viewpager2.currentItem = SELECTED_TAB_POSITION
        TabLayoutMediator(binding.tabsLayout, binding.viewpager2) { tab, position ->
            tab.text = mAdapter?.fragmentTitles?.get(position)
        }.attach()
    }

    companion object {
        private var SELECTED_TAB_POSITION = 0
    }

}
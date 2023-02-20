/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.details

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.FragmentDetailsBinding
import com.azikar24.wormaceptor.internal.HttpTransactionUIHelper
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.internal.support.share
import com.azikar24.wormaceptor.internal.ui.details.fragments.TransactionOverviewFragment
import com.azikar24.wormaceptor.internal.ui.details.fragments.TransactionPayloadFragment
import com.google.android.material.tabs.TabLayoutMediator

class DetailsFragment : Fragment() {
    lateinit var binding: FragmentDetailsBinding
    private val viewModel: TransactionDetailViewModel by viewModels()
    lateinit var mColorUtil: ColorUtil
    private var mAdapter: DetailsAdapter? = null
    lateinit var currentData: HttpTransactionUIHelper

    private val args: DetailsFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentDetailsBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mColorUtil = ColorUtil.getInstance(requireContext())
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.details_menu, menu)
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
                    else -> {
                        false
                    }
                }
            }
        })
        val transactionUIHelper = viewModel.getTransactionWithId(args.id)
        transactionUIHelper?.observe(viewLifecycleOwner) {
            currentData = it
            populateUI()
        }


        setupViewPager()
    }

    private fun populateUI() {
        binding.toolbar.title = "[${currentData.httpTransaction.method}] ${currentData.httpTransaction.path}"
        mAdapter?.let {
            for (fragment in it.fragments) {
                fragment.transactionUpdated(currentData)
            }
        }

        binding.appbar.setBackgroundColor(mColorUtil.getTransactionColor(currentData))

    }

    private fun setupViewPager() {
        mAdapter = DetailsAdapter(requireActivity())
        mAdapter?.addFragment(TransactionOverviewFragment(), getString(R.string.overview))
        mAdapter?.addFragment(TransactionPayloadFragment.newInstance(TransactionPayloadFragment.TYPE_REQUEST), getString(R.string.request))
        mAdapter?.addFragment(TransactionPayloadFragment.newInstance(TransactionPayloadFragment.TYPE_RESPONSE), getString(R.string.response))

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
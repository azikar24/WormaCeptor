/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.details

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.azikar24.wormaceptor.internal.ui.network.details.fragments.NetworkTransactionFragment


class NetworkDetailsAdapter(act: FragmentActivity) : FragmentStateAdapter(act) {
    val fragments: MutableList<NetworkTransactionFragment> = ArrayList()
    val fragmentTitles: MutableList<String> = ArrayList()
    fun addFragment(fragment: NetworkTransactionFragment, title: String) {
        fragments.add(fragment)
        fragmentTitles.add(title)
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position] as Fragment
    }
}

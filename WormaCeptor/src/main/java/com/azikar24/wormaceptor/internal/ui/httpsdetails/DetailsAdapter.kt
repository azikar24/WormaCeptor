/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.httpsdetails

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.azikar24.wormaceptor.internal.ui.httpsdetails.fragments.TransactionFragment


class DetailsAdapter(act: FragmentActivity) : FragmentStateAdapter(act) {
    val fragments: MutableList<TransactionFragment> = ArrayList()
    val fragmentTitles: MutableList<String> = ArrayList()
    fun addFragment(fragment: TransactionFragment, title: String) {
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

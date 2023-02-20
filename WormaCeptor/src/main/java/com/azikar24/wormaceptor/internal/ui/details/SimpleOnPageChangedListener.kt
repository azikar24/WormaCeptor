/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.details

import androidx.viewpager.widget.ViewPager

abstract class SimpleOnPageChangedListener : ViewPager.OnPageChangeListener {
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit
    abstract override fun onPageSelected(position: Int)
    override fun onPageScrollStateChanged(state: Int) = Unit
}
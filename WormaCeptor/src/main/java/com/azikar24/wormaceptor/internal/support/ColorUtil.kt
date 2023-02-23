/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.content.Context
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
import java.util.concurrent.atomic.AtomicBoolean

class ColorUtil(context: Context) {

    val colorPrimary = context.getColorFromRes(R.color.colorPrimary)
    val mSearchHighlightBackgroundColor = context.getColorFromRes(R.color.highlightBackground)
    val mSearchHighlightTextColor = context.getColorFromRes(R.color.highlightText)
    val mSearchHighlightUnderline = false

    val mColorWhite = context.getColorFromRes(R.color.white)


    private val mColorDefault = context.getColorFromRes(R.color.statusDefault)
    private val mColorDefaultTxt = context.getColorFromRes(R.color.statusDefaultTxt)
    private val mColorRequested = context.getColorFromRes(R.color.statusRequested)
    private val mColorError = context.getColorFromRes(R.color.statusError)
    private val mColor500 = context.getColorFromRes(R.color.status500)
    private val mColor400 = context.getColorFromRes(R.color.status400)
    private val mColor300 = context.getColorFromRes(R.color.status300)

    fun getTransactionColor(transactionUIHelper: NetworkTransactionUIHelper, txtColors: Boolean = false): Int {
        val status = transactionUIHelper.getStatus()
        val responseCode: Int = transactionUIHelper.networkTransaction.responseCode ?: 0

        return if (status == NetworkTransactionUIHelper.Status.Failed) {
            mColorError
        } else if (status == NetworkTransactionUIHelper.Status.Requested) {
            mColorRequested
        } else if (responseCode >= 500) {
            mColor500
        } else if (responseCode >= 400) {
            mColor400
        } else if (responseCode >= 300) {
            mColor300
        } else {
            if (txtColors) mColorDefaultTxt else mColorDefault
        }
    }

    companion object {
        private lateinit var INSTANCE: ColorUtil
        private val initialized = AtomicBoolean(true)


        fun getInstance(context: Context): ColorUtil {
            if (initialized.getAndSet(true)) {
                INSTANCE = ColorUtil(context)
            }
            return INSTANCE
        }
    }

}
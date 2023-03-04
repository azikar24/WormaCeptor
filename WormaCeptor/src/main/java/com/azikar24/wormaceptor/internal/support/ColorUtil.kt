/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.content.Context
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
import com.azikar24.wormaceptor.ui.theme.*
import java.util.concurrent.atomic.AtomicBoolean

class ColorUtil(context: Context) {

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

        @Composable
        fun getTransactionColor(transactionUIHelper: NetworkTransactionUIHelper, isText: Boolean = false): Color {
            val status = transactionUIHelper.getStatus()
            val responseCode = transactionUIHelper.networkTransaction.responseCode ?: 0

            return if (status == NetworkTransactionUIHelper.Status.Failed) {
                statusError
            } else if (status == NetworkTransactionUIHelper.Status.Requested) {
                statusRequested
            } else if (responseCode >= 500) {
                status500
            } else if (responseCode >= 400) {
                status400
            } else if (responseCode >= 300) {
                status300
            } else {
                if (isText) MaterialTheme.colors.statusDefaultTxt else MaterialTheme.colors.statusDefault
            }
        }

    }

}
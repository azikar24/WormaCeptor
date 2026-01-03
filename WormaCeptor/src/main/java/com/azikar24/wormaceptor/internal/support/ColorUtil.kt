/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.data.NetworkTransaction.Status.*

data class TransactionColors(
    val text: Color,
    val container: Color,
    val onContainer: Color
)

class ColorUtil(private val context: Context) {

    fun getTransactionColor(
        networkTransaction: NetworkTransaction,
        txtColors: Boolean = false
    ): Int {
        val status = networkTransaction.getStatus()
        val responseCode: Int = networkTransaction.responseCode ?: 0

        return when {
            status == Failed -> context.getColorFromRes(R.color.statusError)
            status == Requested -> context.getColorFromRes(R.color.statusRequested)
            responseCode >= 500 -> context.getColorFromRes(R.color.status500)
            responseCode >= 400 -> context.getColorFromRes(R.color.status400)
            responseCode >= 300 -> context.getColorFromRes(R.color.status300)
            else -> {
                if (txtColors) context.getColorFromRes(R.color.statusDefaultTxt)
                else context.getColorFromRes(R.color.statusDefault)
            }
        }
    }

    companion object {
        @Composable
        fun getTransactionColors(networkTransaction: NetworkTransaction): TransactionColors {
            val status = networkTransaction.getStatus()
            val responseCode = networkTransaction.responseCode ?: 0
            val colorScheme = MaterialTheme.colorScheme

            return when {
                status == Failed -> {
                    TransactionColors(
                        text = Color(0xFF991B1B),
                        container = Color(0xFFFEE2E2),
                        onContainer = Color(0xFF7F1D1D),
                    )
                }

                responseCode >= 500 -> {
                    TransactionColors(
                        text = colorScheme.error,
                        container = colorScheme.errorContainer,
                        onContainer = colorScheme.onErrorContainer
                    )
                }

                status == Requested -> {
                    TransactionColors(
                        text = Color(0xFFB45309),
                        container = Color(0xFFFEF3C7),
                        onContainer = Color(0xFF92400E)
                    )
                }

                responseCode >= 400 -> {
                    TransactionColors(
                        text = Color(0xFFC2410C),
                        container = Color(0xFFFFEDD5),
                        onContainer = Color(0xFF9A3412),
                    )
                }

                responseCode >= 300 -> {
                    TransactionColors(
                        text = Color(0xFF1D4ED8),
                        container = Color(0xFFDBEAFE),
                        onContainer = Color(0xFF1E40AF)
                    )
                }

                else -> {
                    TransactionColors(
                        text = Color(0xFF047857),
                        container = Color(0xFFD1FAE5),
                        onContainer = Color(0xFF065F46)
                    )
                }
            }
        }

        /**
         * Kept for backward compatibility. Use [getTransactionColors] for more options.
         */
        @Composable
        fun getTransactionColor(
            networkTransaction: NetworkTransaction,
            isText: Boolean = false
        ): Color {
            val colors = getTransactionColors(networkTransaction)
            return if (isText) colors.text else colors.container
        }

        @Composable
        fun getTransactionContainerColor(
            networkTransaction: NetworkTransaction,
        ): Color {
            val colors = getTransactionColors(networkTransaction)
            return colors.onContainer
        }
    }
}

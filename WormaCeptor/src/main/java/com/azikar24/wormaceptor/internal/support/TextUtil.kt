/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.os.Build
import android.text.PrecomputedText
import android.widget.TextView
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.concurrent.Executor


object TextUtil {
    fun asyncSetText(bgExecutor: Executor, asyncTextProvider: AsyncTextProvider) {
        val asyncTextProviderReference: Reference<AsyncTextProvider> = WeakReference(asyncTextProvider)
        bgExecutor.execute(Runnable {
            try {
                val textProvider = asyncTextProviderReference.get() ?: return@Runnable
                val longString: CharSequence = textProvider.text ?: ""
                val updateText: CharSequence? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val params: PrecomputedText.Params? = textProvider.textView?.textMetricsParams
                    params?.let { PrecomputedText.create(longString, it) }
                } else {
                    longString
                }
                textProvider.textView?.post {
                    val asyncTextProviderInternal = asyncTextProviderReference.get() ?: return@post
                    val textView: TextView? = asyncTextProviderInternal.textView
                    textView?.setText(updateText, TextView.BufferType.SPANNABLE)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    interface AsyncTextProvider {
        val text: CharSequence?
        val textView: TextView?
    }

    fun isNullOrWhiteSpace(text: CharSequence?): Boolean {
        return text?.trim()?.isEmpty() == true
    }

}
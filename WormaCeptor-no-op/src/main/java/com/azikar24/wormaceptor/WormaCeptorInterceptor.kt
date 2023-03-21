/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor

import android.content.Context
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class WormaCeptorInterceptor(context: Context?) : Interceptor {
    enum class Period {
        ONE_HOUR,
        ONE_DAY,
        ONE_WEEK,
        FOREVER
    }

    fun showNotification(sticky: Boolean): WormaCeptorInterceptor {
        return this
    }

    fun retainDataFor(period: Period?): WormaCeptorInterceptor {
        return this
    }

    fun maxContentLength(max: Long): WormaCeptorInterceptor {
        return this
    }

    fun redactHeader(name: String?): WormaCeptorInterceptor {
        return this
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}

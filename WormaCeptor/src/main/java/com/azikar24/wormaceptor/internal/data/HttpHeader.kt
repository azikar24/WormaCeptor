/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HttpHeader(val name: String? = null, val value: String? = null) : Parcelable
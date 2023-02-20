/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.text.SpannableStringBuilder
import android.text.Spanned
import java.util.*
import java.util.ArrayDeque


class Truss {
    private val builder: SpannableStringBuilder = SpannableStringBuilder()
    private val stack: Deque<Span> = ArrayDeque()

    fun append(value: String?) = apply {
        builder.append(value)
    }

    fun append(value: CharSequence?) = apply {
        builder.append(value)
    }

    fun append(value: Char) = apply {
        builder.append(value.toString())
    }

    fun append(value: Int) = apply {
        builder.append(value.toString())
    }

    fun pushSpan(value: Any) = apply {
        stack.addLast(Span(builder.length, value))
    }

    fun popSpan() = apply {
        val span = stack.removeLast()
        builder.setSpan(span.span, span.start, builder.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    fun build(): SpannableStringBuilder {
        while (!stack.isEmpty()) {
            popSpan()
        }
        return builder
    }

    private class Span(val start: Int, val span: Any)
}
/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import androidx.annotation.ColorInt

class HighlightSpan(
    private val backgroundColor: Int,
    @ColorInt val textColor: Int,
    private val underLineText: Boolean
) : CharacterStyle(), UpdateAppearance {

    override fun updateDrawState(tp: TextPaint?) {
        if (textColor != 0) tp?.color = textColor
        if (backgroundColor != 0) tp?.bgColor = backgroundColor
        tp?.isUnderlineText = underLineText
    }
}
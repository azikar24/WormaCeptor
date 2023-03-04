package com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables

import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables.myiconpack.IcGithub
import kotlin.collections.List as ____KtList

object MyIconPack

private var __AllIcons: ____KtList<ImageVector>? = null

val MyIconPack.AllIcons: ____KtList<ImageVector>
    get() {
        if (__AllIcons != null) {
            return __AllIcons!!
        }
        __AllIcons = listOf(IcGithub)
        return __AllIcons!!
    }

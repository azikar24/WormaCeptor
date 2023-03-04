package com.example.wormaceptor.ui.drawables

import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptor.ui.drawables.myiconpack.*
import kotlin.collections.List as ____KtList

object MyIconPack

private var __AllIcons: ____KtList<ImageVector>? = null

val MyIconPack.AllIcons: ____KtList<ImageVector>
    get() {
        if (__AllIcons != null) {
            return __AllIcons!!
        }
        __AllIcons = listOf(IcStacktrace, IcNetworking, IcSSl, IcBack, IcSearch, IcClose, IcDelete, IcArrowDown, IcArrowUp)
        return __AllIcons!!
    }

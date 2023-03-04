package com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables.myiconpack.IcGithubBuilder
import com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables.myiconpack.icIconFullBuilder

object MyIconPack {
    val IcGithub = IcGithubBuilder.build()

    @Composable
    fun icIconFull(): ImageVector = icIconFullBuilder().build()

//    val AllIcons: ____KtList<ImageVector> = listOf(IcGithub)

}

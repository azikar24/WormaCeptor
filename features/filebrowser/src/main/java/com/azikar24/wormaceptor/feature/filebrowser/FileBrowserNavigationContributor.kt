package com.azikar24.wormaceptor.feature.filebrowser

import android.app.Application
import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.filebrowser.data.FileSystemDataSource
import com.azikar24.wormaceptor.feature.filebrowser.data.FileSystemRepositoryImpl
import com.azikar24.wormaceptor.feature.filebrowser.ui.FileBrowserScreen
import com.azikar24.wormaceptor.feature.filebrowser.ui.FileInfoSheet
import com.azikar24.wormaceptor.feature.filebrowser.ui.FileViewerScreen
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewEffect
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewModel
import com.google.auto.service.AutoService
import kotlinx.coroutines.launch

@AutoService(FeatureNavigationContributor::class)
class FileBrowserNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.FileBrowser.route) {
            FileBrowserDestination(onBack = onBack)
        }
    }
}

@Composable
private fun FileBrowserDestination(onBack: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as? Application
        ?: error("LocalContext is not an Application context")
    val repository = remember {
        val dataSource = FileSystemDataSource(context.applicationContext)
        FileSystemRepositoryImpl(dataSource)
    }
    val factory = remember { FileBrowserViewModelFactory(repository, application) }
    val viewModel: FileBrowserViewModel = viewModel(factory = factory)
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                FileBrowserViewEffect.AtRoot -> onBack()
                FileBrowserViewEffect.NavigatedBack -> { /* Navigation handled via state */ }
                is FileBrowserViewEffect.ShowSnackBar -> {
                    scope.launch { snackBarHostState.showSnackbar(effect.message) }
                }
            }
        },
    ) { state, onEvent ->
        if (state.selectedFile != null && state.fileContent != null) {
            FileViewerScreen(
                state = state,
                onEvent = onEvent,
            )
        } else {
            FileBrowserScreen(
                state = state,
                onEvent = onEvent,
                snackBarHostState = snackBarHostState,
            )
        }

        state.fileInfo?.let { info ->
            FileInfoSheet(
                fileInfo = info,
                onEvent = onEvent,
            )
        }
    }
}

package com.azikar24.wormaceptor.feature.mockrules.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import com.azikar24.wormaceptor.feature.mockrules.R
import com.azikar24.wormaceptor.feature.mockrules.vm.DelayType
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRuleEditorEvent
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRuleEditorState
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRuleEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MockRuleEditorScreen(
    viewModel: MockRuleEditorViewModel,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val editorState by viewModel.uiState.collectAsState()

    MockRuleEditorContent(
        state = editorState,
        onEvent = viewModel::sendEvent,
        onSave = onSave,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MockRuleEditorContent(
    state: MockRuleEditorState,
    onEvent: (MockRuleEditorEvent) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isEditing) {
                                R.string.mock_editor_title_edit
                            } else {
                                R.string.mock_editor_title_new
                            },
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.mock_editor_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (state.isValid) {
                WormaCeptorFAB(
                    onClick = onSave,
                    icon = Icons.Default.Check,
                    contentDescription = stringResource(R.string.mock_editor_save),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            BasicInfoSection(
                name = state.name,
                onNameChange = { onEvent(MockRuleEditorEvent.NameChanged(it)) },
            )

            RequestMatchingSection(
                urlPattern = state.urlPattern,
                matchType = state.matchType,
                method = state.method,
                methodDropdownExpanded = state.methodDropdownExpanded,
                onUrlPatternChange = { onEvent(MockRuleEditorEvent.UrlPatternChanged(it)) },
                onMatchTypeChange = { onEvent(MockRuleEditorEvent.MatchTypeChanged(it)) },
                onMethodChange = { onEvent(MockRuleEditorEvent.MethodChanged(it)) },
                onMethodDropdownExpandedChange = { onEvent(MockRuleEditorEvent.MethodDropdownExpandedChanged(it)) },
            )

            ResponseSection(
                statusCode = state.statusCode,
                statusMessage = state.statusMessage,
                contentType = state.contentType,
                responseBody = state.responseBody,
                onStatusCodeChange = { onEvent(MockRuleEditorEvent.StatusCodeChanged(it)) },
                onStatusMessageChange = { onEvent(MockRuleEditorEvent.StatusMessageChanged(it)) },
                onContentTypeChange = { onEvent(MockRuleEditorEvent.ContentTypeChanged(it)) },
                onResponseBodyChange = { onEvent(MockRuleEditorEvent.ResponseBodyChanged(it)) },
            )

            DelaySection(
                delayType = state.delayType,
                delayMs = state.delayMs,
                delayMinMs = state.delayMinMs,
                delayMaxMs = state.delayMaxMs,
                onDelayTypeChange = { onEvent(MockRuleEditorEvent.DelayTypeChanged(it)) },
                onDelayMsChange = { onEvent(MockRuleEditorEvent.DelayMsChanged(it)) },
                onDelayMinMsChange = { onEvent(MockRuleEditorEvent.DelayMinMsChanged(it)) },
                onDelayMaxMsChange = { onEvent(MockRuleEditorEvent.DelayMaxMsChanged(it)) },
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MockRuleEditorScreenPreview() {
    WormaCeptorTheme {
        MockRuleEditorContent(
            state = MockRuleEditorState(),
            onEvent = {},
            onSave = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MockRuleEditorEditPreview() {
    WormaCeptorTheme {
        MockRuleEditorContent(
            state = MockRuleEditorState(
                name = "Login Error Mock",
                urlPattern = "https://api.example.com/login",
                matchType = UrlMatchType.PREFIX,
                method = "POST",
                statusCode = 500,
                statusMessage = "Internal Server Error",
                responseBody = "{\"error\": \"Something went wrong\"}",
                delayType = DelayType.FIXED,
                delayMs = "2000",
                isEditing = true,
            ),
            onEvent = {},
            onSave = {},
            onBack = {},
        )
    }
}

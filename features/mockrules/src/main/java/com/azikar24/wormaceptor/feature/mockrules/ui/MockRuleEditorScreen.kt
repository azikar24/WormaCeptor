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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import com.azikar24.wormaceptor.feature.mockrules.R
import com.azikar24.wormaceptor.feature.mockrules.vm.DelayType
import com.azikar24.wormaceptor.feature.mockrules.vm.EditorState
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesViewEvent

private val FabSafeAreaHeight = 80.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MockRuleEditorContent(
    state: EditorState,
    onEvent: (MockRulesViewEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            WormaCeptorTopBar(
                title = stringResource(
                    if (state.isEditing) {
                        R.string.mock_editor_title_edit
                    } else {
                        R.string.mock_editor_title_new
                    },
                ),
                onBack = onBack,
                backContentDescription = stringResource(R.string.mock_editor_back),
            )
        },
        floatingActionButton = {
            if (state.isValid) {
                WormaCeptorFAB(
                    onClick = { onEvent(MockRulesViewEvent.Editor.SaveRule) },
                    icon = Icons.Default.Check,
                    contentDescription = stringResource(R.string.mock_editor_save),
                )
            }
        },
    ) { padding ->
        EditorFormBody(
            state = state,
            onEvent = onEvent,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        )
    }
}

@Composable
private fun EditorFormBody(
    state: EditorState,
    onEvent: (MockRulesViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(WormaCeptorTokens.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
    ) {
        BasicInfoSection(
            name = state.name,
            onNameChange = { onEvent(MockRulesViewEvent.Editor.NameChanged(it)) },
        )

        RequestMatchingSection(
            urlPattern = state.urlPattern,
            matchType = state.matchType,
            method = state.method,
            methodDropdownExpanded = state.methodDropdownExpanded,
            onUrlPatternChange = { onEvent(MockRulesViewEvent.Editor.UrlPatternChanged(it)) },
            onMatchTypeChange = { onEvent(MockRulesViewEvent.Editor.MatchTypeChanged(it)) },
            onMethodChange = { onEvent(MockRulesViewEvent.Editor.MethodChanged(it)) },
            onMethodDropdownExpandedChange = { onEvent(MockRulesViewEvent.Editor.MethodDropdownExpandedChanged(it)) },
        )

        ResponseSection(
            statusCode = state.statusCode,
            statusMessage = state.statusMessage,
            contentType = state.contentType,
            responseBody = state.responseBody,
            onStatusCodeChange = { onEvent(MockRulesViewEvent.Editor.StatusCodeChanged(it)) },
            onStatusMessageChange = { onEvent(MockRulesViewEvent.Editor.StatusMessageChanged(it)) },
            onContentTypeChange = { onEvent(MockRulesViewEvent.Editor.ContentTypeChanged(it)) },
            onResponseBodyChange = { onEvent(MockRulesViewEvent.Editor.ResponseBodyChanged(it)) },
        )

        DelaySection(
            delayType = state.delayType,
            delayMs = state.delayMs,
            delayMinMs = state.delayMinMs,
            delayMaxMs = state.delayMaxMs,
            onDelayTypeChange = { onEvent(MockRulesViewEvent.Editor.DelayTypeChanged(it)) },
            onDelayMsChange = { onEvent(MockRulesViewEvent.Editor.DelayMsChanged(it)) },
            onDelayMinMsChange = { onEvent(MockRulesViewEvent.Editor.DelayMinMsChanged(it)) },
            onDelayMaxMsChange = { onEvent(MockRulesViewEvent.Editor.DelayMaxMsChanged(it)) },
        )

        Spacer(modifier = Modifier.height(FabSafeAreaHeight))
    }
}

@Preview(showBackground = true)
@Composable
private fun MockRuleEditorScreenPreview() {
    WormaCeptorTheme {
        MockRuleEditorContent(
            state = EditorState(),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MockRuleEditorEditPreview() {
    WormaCeptorTheme {
        MockRuleEditorContent(
            state = EditorState(
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
                isLoaded = true,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}

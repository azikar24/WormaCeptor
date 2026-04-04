package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.R
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/** Reusable search bar with clear button and configurable keyboard action. */
@Composable
fun WormaCeptorSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearch: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val keyboardOptions = KeyboardOptions(
        imeAction = if (onSearch != null) ImeAction.Search else ImeAction.Done,
    )
    val keyboardActions = KeyboardActions(
        onSearch = { onSearch?.invoke() },
    )
    val leadingIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(R.string.search_icon_description),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    val trailingIcon: @Composable () -> Unit = {
        ClearButton(
            visible = query.isNotEmpty(),
            onClick = { onQueryChange("") },
        )
    }
    val placeholderContent: @Composable () -> Unit = {
        Text(
            text = placeholder,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
        )
    }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        placeholder = placeholderContent,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = true,
        shape = RoundedCornerShape(WormaCeptorTokens.Radius.md),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = WormaCeptorTokens.Alpha.BOLD,
            ),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
private fun ClearButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = stringResource(R.string.search_clear_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// region Previews

@Preview(name = "SearchBar Empty - Light")
@Composable
private fun SearchBarEmptyLightPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorSearchBar(
                query = "",
                onQueryChange = {},
            )
        }
    }
}

@Preview(name = "SearchBar Empty - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchBarEmptyDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorSearchBar(
                query = "",
                onQueryChange = {},
            )
        }
    }
}

@Preview(name = "SearchBar With Query - Light")
@Composable
private fun SearchBarWithQueryLightPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorSearchBar(
                query = "api/v2/users",
                onQueryChange = {},
            )
        }
    }
}

@Preview(name = "SearchBar With Query - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SearchBarWithQueryDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorSearchBar(
                query = "api/v2/users",
                onQueryChange = {},
            )
        }
    }
}

// endregion

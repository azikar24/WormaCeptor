/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptor.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcBack
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcClose
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcSearch
import com.example.wormaceptor.ui.drawables.MyIconPack

object WormaCeptorToolbar {

    var activity: ComponentActivity? = null

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun WormaCeptorToolbar(
        title: String,
        navController: NavController,
        subtitle: String? = null,
        showSearch: Boolean = false,
        searchListener: SearchListener? = null,
        color: Color = MaterialTheme.colorScheme.primary,
        menuActions: @Composable() RowScope.() -> Unit = {}
    ) {
        Column {
            TopAppBar(
                title = {
                    Column() {
                        Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        subtitle?.let {
                            Text(
                                text = it.ifEmpty {
                                    stringResource(LocalContext.current.applicationInfo.labelRes)
                                },
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = menuActions, navigationIcon = {
                    IconButton(onClick = {
                        if (!navController.navigateUp()) {
                            activity?.finish()
                        }
                    }) {
                        Icon(imageVector = MyIconPack.IcBack, contentDescription = "")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = color,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .background(color)
                    .padding(bottom = 10.dp)

            )
            if (!showSearch) searchListener?.onQueryTextChange("")
            AnimatedVisibility(
                visible = showSearch,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                SearchUI(searchListener)
            }

        }
    }


    fun interface SearchListener {
        fun onQueryTextChange(value: String?)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SearchUI(
    searchListener: WormaCeptorToolbar.SearchListener? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    counter: Int? = null,
    maxCounter: Int? = null,
) {
    var value by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(horizontal = 10.dp)
            .padding(vertical = 20.dp),
    ) {
        OutlinedTextField(
            value = value,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.search),
                    color = MaterialTheme.colorScheme.onPrimary.copy(0.5f)
                )
            },
            onValueChange = {
                value = it
                searchListener?.onQueryTextChange(it)
            },
            maxLines = 1,
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors().copy(
                cursorColor = MaterialTheme.colorScheme.onPrimary,
                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                focusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onPrimary,
            ),
            leadingIcon = {
                Icon(
                    imageVector = MyIconPack.IcSearch,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = {
                            value = ""
                            searchListener?.onQueryTextChange(value)
                        }) {
                            Icon(
                                imageVector = MyIconPack.IcClose,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    if (counter != null && maxCounter != null && maxCounter != 0) {
                        Text(
                            text = "${counter + 1}/$maxCounter",
                            modifier = Modifier.padding(end = 5.dp)
                        )
                    }
                }

            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        focusRequester.requestFocus()
                    }
                }
        )
    }
}


@Preview(showBackground = true, device = Devices.PIXEL)
@Composable
private fun Preview() {
    WormaCeptorToolbar.WormaCeptorToolbar(
        title = "title",
        rememberNavController(),
        searchListener = {},
        menuActions = {
            Text("test1")
        }
    )
}

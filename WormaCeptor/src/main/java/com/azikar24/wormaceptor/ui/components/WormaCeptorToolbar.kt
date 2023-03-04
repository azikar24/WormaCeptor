/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptor.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ExposedDropdownMenuDefaults.textFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.R
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcBack
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcClose
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcSearch
import com.example.wormaceptor.ui.drawables.MyIconPack
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator


@Preview(showBackground = true, device = Devices.PIXEL)
@Composable
private fun Preview() {
    WormaCeptorToolbar.WormaCeptorToolbar(title = "title", EmptyDestinationsNavigator, searchListener = {}, menuActions = {
        Text("test1")

    })
}

object WormaCeptorToolbar {

    var activity: ComponentActivity? = null

    @Composable
    fun WormaCeptorToolbar(title: String, navController: DestinationsNavigator, subtitle: String? = null, showSearch: Boolean = false, searchListener: SearchListener? = null, color: Color = MaterialTheme.colors.primary, menuActions: @Composable() RowScope.() -> Unit = {}) {
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
                backgroundColor = color,
                contentColor = MaterialTheme.colors.onPrimary,
                elevation = 0.dp,
                modifier = Modifier
                    .background(color)
                    .padding(top = 30.dp, bottom = 10.dp)

            )
            if (!showSearch) searchListener?.OnQueryTextChange("")
            AnimatedVisibility(visible = showSearch, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                SearchUI(searchListener)
            }

        }
    }


    fun interface SearchListener {
        fun OnQueryTextChange(value: String?)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun SearchUI(
    searchListener: WormaCeptorToolbar.SearchListener? = null, color: Color = MaterialTheme.colors.primary,
    counter: Int? = null,
    maxCounter: Int? = null,
) {
    var value by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color),
    ) {
        TextField(
            value = value,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.search),
                    color = MaterialTheme.colors.onPrimary.copy(0.5f)
                )
            }, onValueChange = {
                value = it
                searchListener?.OnQueryTextChange(it)
            },
            shape = MaterialTheme.shapes.large,
            colors = textFieldColors(
                cursorColor = MaterialTheme.colors.onPrimary,
                textColor = MaterialTheme.colors.onPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            leadingIcon = {
                Icon(
                    imageVector = MyIconPack.IcSearch,
                    contentDescription = "",
                    tint = MaterialTheme.colors.onPrimary
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = {
                            value = ""
                            searchListener?.OnQueryTextChange(value)
                        }) {
                            Icon(
                                imageVector = MyIconPack.IcClose,
                                contentDescription = "",
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }
                    }

                    if (counter != null && maxCounter != null && maxCounter != 0) {
                        Text(text = "${counter + 1}/$maxCounter", modifier = Modifier.padding(end = 5.dp))
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
                .background(color)
        )
    }
}
/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.annotations.ScreenPreviews
import com.azikar24.wormaceptorapp.wormaceptorui.components.WormaCeptorToolbar
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme
import com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables.MyIconPack

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MainActivityContent()
        }
        WormaCeptor.startActivityOnShake(this)
    }

    @Composable
    private fun MainActivityContent(viewModel: MainActivityViewModel = MainActivityViewModel()) {
        enableEdgeToEdge()

        WormaCeptorMainTheme() {
            Surface(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Column() {
                    ToolBar(viewModel)
                    Header()
                    Content(viewModel)
                }
            }
        }
    }

    @Composable
    private fun ToolBar(viewModel: MainActivityViewModel) {
        WormaCeptorToolbar(stringResource(id = R.string.app_name)) {
            IconButton(onClick = {
                viewModel.goToGithub(this@MainActivity)
            }) {
                Icon(
                    imageVector = MyIconPack.IcGithub,
                    contentDescription = stringResource(id = R.string.github_page),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }


    @Composable
    private fun Header() {
        Image(
            imageVector = MyIconPack.icIconFull(),
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 40.dp)
        )
    }

    @Composable
    private fun Content(viewModel: MainActivityViewModel) {
        Column(
            verticalArrangement = Arrangement.spacedBy(
                15.dp,
                Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .fillMaxHeight(0.9f),
        ) {
            Button(
                onClick = { viewModel.doHttpActivity(baseContext) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.do_http_activity))
            }


            Button(
                onClick = {
                    viewModel.startWormaCeptor(this@MainActivity)
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.launch_directly))
            }


            Button(
                onClick = {
                    viewModel.simulateCrash()
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.simulateError))
            }
        }
    }

    @ScreenPreviews
    @Composable
    private fun PreviewMainActivityContent() {
        WormaCeptorMainTheme() {
            Surface(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                MainActivityContent()
            }
        }
    }
}


/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptorapp.sampleservice.Data
import com.azikar24.wormaceptorapp.sampleservice.SampleApiService
import com.azikar24.wormaceptorapp.sampleservice.VeryLargeData
import com.azikar24.wormaceptorapp.wormaceptorui.components.WormaCeptorToolbar
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MainActivityContent()
        }
        WormaCeptor.startActivityOnShake(this)
    }

    @Preview
    @Composable
    private fun MainActivityContent() {
        val systemUiController = rememberSystemUiController()
        systemUiController.setSystemBarsColor(
            color = Color.Transparent
        )

        WormaCeptorMainTheme() {
            Surface(Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)) {
                Column() {
                    ToolBar()
                    Header()
                    Content()
                }
            }
        }
    }


    @Composable
    private fun ToolBar() {
        WormaCeptorToolbar(stringResource(id = R.string.app_name)) {
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(getString(R.string.github_link))
                }
                startActivity(intent)
            }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_github),
                    contentDescription = stringResource(id = R.string.github_page),
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        }
    }

    @Composable
    private fun Header() {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_full),
            contentDescription = "",
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 40.dp)
        )
    }

    @Composable
    private fun Content() {
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
                onClick = { doHttpActivity() },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.do_http_activity))
            }


            Button(
                onClick = {
                    WormaCeptor.getLaunchIntent(baseContext)?.let {
                        startActivity(it)
                    }
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.launch_directly))
            }


            Button(
                onClick = {
                    val x = arrayOf("")
                    x[4]
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.simulateError))
            }

        }
    }


    private fun doHttpActivity() {
        val api = SampleApiService.getInstance(baseContext)

        val callBack = object : Callback<Void?> {
            override fun onResponse(call: Call<Void?>, response: Response<Void?>) = Unit
            override fun onFailure(call: Call<Void?>, t: Throwable) {
                t.printStackTrace()
            }
        }

        api.get().enqueue(callBack)
        api.post(Data("posted")).enqueue(callBack)

        api.postForm("I Am String", null, 2.34567891, 1234, false).enqueue(callBack)
        api.patch(Data("patched")).enqueue(callBack)
        api.put(Data("put")).enqueue(callBack)
        api.delete().enqueue(callBack)
        api.status(201).enqueue(callBack)
        api.status(401).enqueue(callBack)
        api.status(500).enqueue(callBack)
        api.delay(9).enqueue(callBack)
        api.delay(15).enqueue(callBack)
        api.bearer(UUID.randomUUID().toString()).enqueue(callBack)
        api.redirectTo("https://http2.akamai.com").enqueue(callBack)
        api.redirect(3).enqueue(callBack)
        api.redirectRelative(2).enqueue(callBack)
        api.redirectAbsolute(4).enqueue(callBack)
        api.stream(500).enqueue(callBack)
        api.streamBytes(2048).enqueue(callBack)
        api.image("image/png").enqueue(callBack)
        api.gzip().enqueue(callBack)
        api.xml().enqueue(callBack)
        api.utf8().enqueue(callBack)
        api.deflate().enqueue(callBack)
        api.cookieSet("v").enqueue(callBack)
        api.basicAuth("me", "pass").enqueue(callBack)
        api.drip(512, 5, 1, 200).enqueue(callBack)
        api.deny().enqueue(callBack)
        api.cache("Mon").enqueue(callBack)
        api.cache(30).enqueue(callBack)
        api.post(VeryLargeData()).enqueue(callBack)

    }

}
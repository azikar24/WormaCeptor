/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.azikar24.wormaceptor.databinding.ActivityMainBinding
import com.azikar24.wormaceptor.sampleservice.Data
import com.azikar24.wormaceptor.sampleservice.SampleApiService
import com.azikar24.wormaceptor.sampleservice.VeryLargeData
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        WormaCeptor.startActivityOnShake(this)
        setupButton()
    }

    private fun setupButton() {
        binding.doHttpActivityButton.setOnClickListener {
            doHttpActivity()
        }

        binding.launchWormaCeptorDirectlyButton.setOnClickListener {
            WormaCeptor.getLaunchIntent(this)?.let {
                startActivity(it)
            }
        }

        binding.simulateErrorButton.setOnClickListener {
            val x = arrayOf("")
            x[4]
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.actionMenuGithub) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(getString(R.string.github_link))
            }
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}
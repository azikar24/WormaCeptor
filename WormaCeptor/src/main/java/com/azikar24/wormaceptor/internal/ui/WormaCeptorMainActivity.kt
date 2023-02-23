/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.databinding.ActivityWormaceptorMainBinding
import com.azikar24.wormaceptor.internal.support.NotificationHelper
import com.azikar24.wormaceptor.internal.support.UIHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class WormaCeptorMainActivity : AppCompatActivity() {
    private lateinit var mNotificationHelper: NotificationHelper

    lateinit var binding: ActivityWormaceptorMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNotificationHelper = NotificationHelper(baseContext)
        binding = ActivityWormaceptorMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        UIHelper.fullScreen(window.decorView, window)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationView) as NavHostFragment
        val navController = navHostFragment.navController
        val navView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        navView.setupWithNavController(navController)
        binding.bottomNavigationView.updatePadding(0, 0, 0, if (UIHelper.isGestureNavigationEnabled(baseContext.contentResolver)) (20 * Resources.getSystem().displayMetrics.density).toInt() else 0)

        binding.bottomNavigationView.setOnApplyWindowInsetsListener(null)

        binding.bottomNavigationView.visibility = if (WormaCeptor.type == WormaCeptor.WormaCeptorType.PERSISTENCE) View.VISIBLE else View.GONE

    }

    override fun onResume() {
        super.onResume()
        IN_FOREGROUND = true
        mNotificationHelper.dismiss()

        findNavController(R.id.navigationView).addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.httpsListFragment2 || destination.id == R.id.stackTraceListFragment2) {
                binding.bottomNavigationView.visibility = View.VISIBLE
            } else {
                binding.bottomNavigationView.visibility = View.GONE
            }
        }
    }

    override fun onPause() {
        super.onPause()
        IN_FOREGROUND = false
    }

    companion object {
        var IN_FOREGROUND = false

    }
}
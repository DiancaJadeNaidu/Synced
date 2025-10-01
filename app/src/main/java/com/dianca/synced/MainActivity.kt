package com.dianca.synced

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnSync: Button
    private lateinit var btnAllow: Button
    private lateinit var btnNotNow: Button

    // Modern permission request API
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications not allowed", Toast.LENGTH_SHORT).show()
        }
        goToRules()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)

        btnSync = findViewById(R.id.btnSync)
        btnAllow = findViewById(R.id.btnAllow)
        btnNotNow = findViewById(R.id.btnNotNow)

        btnSync.setOnClickListener { goToRules() }

        btnAllow.setOnClickListener { requestNotificationPermission() }
        btnNotNow.setOnClickListener { goToRules() }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(this, "Notifications already enabled", Toast.LENGTH_SHORT).show()
                    goToRules()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    // Optional: Show rationale dialog before requesting
                    Toast.makeText(this, "Please allow notifications to stay connected", Toast.LENGTH_LONG).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For Android < 13, notifications are automatically allowed
            goToRules()
        }
    }

    private fun goToRules() {
        val intent = Intent(this, RulesActivity::class.java)
        startActivity(intent)
        finish()
    }
}

package com.songbookhost

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var toggleButton: Button
    private lateinit var ipTextView: TextView
    private var running = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val allGranted = granted.values.all { it }
        if (allGranted) {
            startHostService()
        }
    }

    private val ipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == HotspotService.ACTION_STARTED) {
                val ip = intent.getStringExtra(HotspotService.EXTRA_IP) ?: ""
                ipTextView.text = ip
                running = true
                toggleButton.text = getString(R.string.stop_hotspot)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleButton = findViewById(R.id.toggleButton)
        ipTextView = findViewById(R.id.ipTextView)

        toggleButton.setOnClickListener {
            if (running) {
                stopService(Intent(this, HotspotService::class.java))
                ipTextView.text = ""
                running = false
                toggleButton.text = getString(R.string.start_hotspot)
            } else {
                requestPermissionsAndStart()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(ipReceiver, IntentFilter(HotspotService.ACTION_STARTED))
    }

    override fun onStop() {
        unregisterReceiver(ipReceiver)
        super.onStop()
    }

    private fun requestPermissionsAndStart() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.NEARBY_WIFI_DEVICES
        )
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isEmpty()) {
            startHostService()
        } else {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    private fun startHostService() {
        val intent = Intent(this, HotspotService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
}

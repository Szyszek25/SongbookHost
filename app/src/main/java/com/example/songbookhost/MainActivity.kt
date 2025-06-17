package com.example.songbookhost

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var toggleButton: Button
    private var running = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleButton = findViewById(R.id.toggleButton)
        toggleButton.setOnClickListener {
            if (running) {
                stopService(Intent(this, HotspotService::class.java))
            } else {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        0
                    )
                    return@setOnClickListener
                }
                // Od Androida 8.0 musimy uruchamiać usługę jako
                // foreground service, aby system jej nie zatrzymał.
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, HotspotService::class.java)
                )
            }
            running = !running
            toggleButton.text = if (running) "Zatrzymaj" else "Uruchom hotspot"

        }
    }
}

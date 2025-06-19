package com.songbookhost

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent

import android.Manifest
import android.content.pm.PackageManager

>>>>>>> master
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.net.Inet4Address
import java.net.NetworkInterface
import java.time.Duration

class HotspotService : Service() {

    companion object {
        const val ACTION_STARTED = "com.songbookhost.HOST_STARTED"
        const val EXTRA_IP = "extra_ip"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "hotspot"
    }

    private lateinit var wifiManager: WifiManager
    private var reservation: WifiManager.LocalOnlyHotspotReservation? = null
    private var server: ApplicationEngine? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        startForeground(NOTIFICATION_ID, createNotification())


        if (checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf()
            return
        }

        wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
            override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                this@HotspotService.reservation = reservation
                startServer()
                val ip = getLocalIpAddress()
                val intent = Intent(ACTION_STARTED).putExtra(EXTRA_IP, ip)
                sendBroadcast(intent)
            }

            override fun onFailed(reason: Int) {
                stopSelf()
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun createNotification(): Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Hotspot", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
   ai3k52-codex/stwórz-aplikację-od-nowa
            .setSmallIcon(android.R.drawable.stat_sys_wifi_signal_4)

            .setSmallIcon(android.R.drawable.stat_sys_wifi)

            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_running))
            .build()
    }

    private fun startServer() {
        scope.launch {
            server = embeddedServer(Netty, port = 8080) {
                install(WebSockets) {
                    pingPeriod = Duration.ofSeconds(15)
                }
                routing {
                    get("/songs/{id}") {
                        val id = call.parameters["id"] ?: "1"
                        call.respondText(loadSong(id))
                    }
                    webSocket("/ws") {
                        send("WebSocket connected")
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                send("Echo: ${frame.readText()}")
                            }
                        }
                    }
                    get("/") {
                        call.respondText("SongbookHost running")
                    }
                }
            }.start(wait = false)
        }
    }

    private fun loadSong(id: String): String {
        return try {
            assets.open("songs/$id.txt").bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Nie znaleziono utworu."
        }
    }

    private fun getLocalIpAddress(): String {
        val interfaces = NetworkInterface.getNetworkInterfaces().toList()
        for (iface in interfaces) {
            val addresses = iface.inetAddresses.toList()
            for (addr in addresses) {
                if (!addr.isLoopbackAddress && addr is Inet4Address) {
                    return addr.hostAddress ?: ""
                }
            }
        }
        return ""
    }

    override fun onDestroy() {
        server?.stop(1000, 3000)
        reservation?.close()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

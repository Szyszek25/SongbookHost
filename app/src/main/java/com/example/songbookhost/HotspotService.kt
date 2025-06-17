package com.example.songbookhost

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.time.Duration

class HotspotService : Service() {

    private lateinit var wifiManager: WifiManager
    private var reservation: WifiManager.LocalOnlyHotspotReservation? = null
    private var server: ApplicationEngine? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val channelId = "hotspot"
    private val notifId = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = buildNotification()
        startForeground(notifId, notification)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
            override fun onStarted(res: WifiManager.LocalOnlyHotspotReservation) {
                reservation = res
                startServer()
            }

            override fun onFailed(reason: Int) {
                stopSelf()
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun startServer() {
        scope.launch {
            server = embeddedServer(Netty, port = 8080) {
                install(WebSockets) {
                    pingPeriod = Duration.ofSeconds(15)
                }
                routing {
                    get("/") {
                        call.respondText("SongbookHost działa!")
                    }
                    get("/songs/{id}") {
                        val id = call.parameters["id"] ?: "1"
                        val text = assetsSong(id)
                        call.respondText(text)
                    }
                    webSocket("/ws") {
                        send("Połączenie WebSocket nawiązane")
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                send("Echo: ${frame.readText()}")
                            }
                        }
                    }
                }
            }.start(wait = false)
        }
    }

    private fun assetsSong(id: String): String {
        return try {
            applicationContext.assets.open("songs/${id}.txt")
                .bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "Nie znaleziono utworu."
        }
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "Hotspot",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification() = NotificationCompat.Builder(this, channelId)
        .setContentTitle("SongbookHost")
        .setContentText("Uruchomiono hotspot")
        .setSmallIcon(android.R.drawable.stat_sys_wifi_signal_4)
        .setOngoing(true)
        .build()

    override fun onDestroy() {
        server?.stop(1000, 3000)
        reservation?.close()
        scope.cancel()
        stopForeground(true)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

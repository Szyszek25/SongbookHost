package com.example.songbookhost

import android.app.Service
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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

    override fun onCreate() {
        super.onCreate()
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

    override fun onDestroy() {
        server?.stop(1000, 3000)
        reservation?.close()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

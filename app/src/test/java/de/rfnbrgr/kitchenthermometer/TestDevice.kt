package de.rfnbrgr.kitchenthermometer

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.*

class TestDevice(val port: Int) : Runnable {

    companion object {
        val SOCKET_TIMEOUT_MS = 500
    }

    private var running: Boolean = true
    private var connections: MutableList<Socket> = mutableListOf()

    fun stop() {
        running = false
    }

    override fun run() {
        val socket = ServerSocket(port)
        socket.soTimeout = SOCKET_TIMEOUT_MS
        while (running) {
            try {
                val connectionSocket = socket.accept()
                connections.add(connectionSocket)
            } catch (e: SocketTimeoutException) {
                // retry
            }
        }
    }

    fun send(frame: String) {
        connections.forEach {
            it.getOutputStream().write("$frame\n".toByteArray())
        }
    }
}

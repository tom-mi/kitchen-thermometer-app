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

fun main(args: Array<String>) {
    val width = 8
    val height = 8
    val pixels: MutableList<Float> = (1..width * height).map { 0f }.toMutableList()

    val random = Random()
    val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    val heatFrameAdapter = moshi.adapter<HeatFrame>(HeatFrame::class.java)

    val device = TestDevice(5000)
    val deviceThread = Thread(device)
    deviceThread.start()

    while (true) {
        for (i in 0..pixels.size) {
            pixels[i] = pixels[i] + random.nextFloat()
            pixels[i] = maxOf(0f, minOf(100f, pixels[i]))
        }

        try {
            val frame = HeatFrame(0.7f, width, height, pixels)
            println("Sending frame $frame")
            device.send(heatFrameAdapter.toJson(frame) + '\n')
            Thread.sleep(100)
        } catch (e: InterruptedException) {
            break
        }
    }
    device.stop()
    deviceThread.join()
}

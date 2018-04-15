package de.rfnbrgr.kitchenheatseeker

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class DeviceClient(val host: String, val port: Int, val queue: Queue<HeatFrame>) : Runnable {

    companion object {
        const val READ_TIMEOUT_MS = 500
    }

    private var running: AtomicBoolean = AtomicBoolean(true)
    private var moshi: Moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    private var heatFrameAdapter = moshi.adapter<HeatFrame>(HeatFrame::class.java)

    fun stop() {
        running.set(false)
    }

    override fun run() {
        println("Starting client")
        val client = Socket(host, port)
        client.soTimeout = READ_TIMEOUT_MS
        val reader = BufferedReader(InputStreamReader(client.getInputStream()))
        while (running.get()) {
            try {
                val line = reader.readLine()
                println("Read $line")
                val frame = deserialize(line)
                if (frame != null) {
                    queue.offer(frame)
                }
            } catch (e: SocketTimeoutException) {
                // retry
            }
        }
        println("Closing connection")
        client.close()
        println("Closed connection")
    }

    private fun deserialize(json: String): HeatFrame? {
        return heatFrameAdapter.fromJson(json)
    }


}


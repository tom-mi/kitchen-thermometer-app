package de.rfnbrgr.kitchenthermometer

import android.util.Log
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicBoolean


class DeviceClient(private val host: String, private val port: Int,
                   private val frameCallback: (HeatFrame) -> Unit,
                   private val stateCallback: (ClientState) -> Unit) : Runnable {

    enum class ClientState {
        CONNECTING,
        CONNECTED,
        NOT_CONNECTED,
        FAILED,
        UNKNOWN,
    }

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
        try {
            connectAndRun()
            stateCallback(ClientState.NOT_CONNECTED)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Failed to connect to [$host]", e)
            stateCallback(ClientState.FAILED)
        }
    }

    private fun connectAndRun() {
        stateCallback(ClientState.CONNECTING)
        val client = Socket(host, port)
        stateCallback(ClientState.CONNECTED)
        client.soTimeout = READ_TIMEOUT_MS
        val reader = BufferedReader(InputStreamReader(client.getInputStream()))
        while (running.get()) {
            try {
                val line = reader.readLine()
                println("Read $line")
                if (line == null) {
                    continue
                }
                val frame = deserialize(line)
                if (frame != null) {
                    frameCallback(frame)
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


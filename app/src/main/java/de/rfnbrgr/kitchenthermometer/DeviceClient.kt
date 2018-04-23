package de.rfnbrgr.kitchenthermometer

import android.util.Log
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
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
        const val CONNECT_TIMEOUT_MS = 500
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
        Log.d(javaClass.simpleName, "${Thread.currentThread().name}: Starting client")
        try {
            connectAndRun()
            stateCallback(ClientState.NOT_CONNECTED)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "${Thread.currentThread().name}: Failed to connect to [$host]", e)
            stateCallback(ClientState.FAILED)
        }
    }

    private fun connectAndRun() {
        stateCallback(ClientState.CONNECTING)
        val client = Socket()
        try {
            client.soTimeout = READ_TIMEOUT_MS
            client.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
            stateCallback(ClientState.CONNECTED)
            val reader = BufferedReader(InputStreamReader(client.getInputStream()))
            while (running.get()) {
                try {
                    val line = reader.readLine() ?: break
                    val frame = deserialize(line)
                    if (frame != null) {
                        frameCallback(frame)
                    }
                } catch (e: SocketTimeoutException) {
                    // retry
                }
            }
        } finally {
            Log.d(javaClass.simpleName, "${Thread.currentThread().name}: Closing connection")
            client.close()
            Log.d(javaClass.simpleName, "${Thread.currentThread().name}: Closed connection")
        }

    }

    private fun deserialize(json: String): HeatFrame? {
        return try {
            heatFrameAdapter.fromJson(json)
        } catch(e: JsonEncodingException ) {
            Log.w(javaClass.simpleName, "Could not decode Json [$json]", e)
            null
        } catch(e: JsonDataException) {
            Log.w(javaClass.simpleName, "Could not decode Json [$json]", e)
            null
        }
    }

}


package de.rfnbrgr.kitchenheatseeker

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_heat_display.*
import java.util.concurrent.ConcurrentLinkedQueue

class HeatDisplay : AppCompatActivity() {
    companion object {
        val DEFAULT_PORT = 5000
        const val HOSTNAME = "hostname"
        const val PORT = "port"
    }

    private var deviceClient: DeviceClient? = null
    private var deviceClientThread: Thread? = null
    private var frameQueue = ConcurrentLinkedQueue<HeatFrame>()
    private var hostname: String = "localhost"
    private var port: Int = DEFAULT_PORT

    private val handlerThread = HandlerThread("HeatDisplayHandler")
    private var handler: Handler? = null

    inner class HeatFrameUpdater : Runnable {
        override fun run() {
            try {
                Log.d(javaClass.simpleName, "Currently ${frameQueue.size} items in queue")
                val frame = frameQueue.last()
                frameQueue.clear()
                heatmap.setFrame(frame)
            } catch (e: NoSuchElementException) {
                // do nothing
            }
            handler!!.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heat_display)
        hostname = intent.getStringExtra(HOSTNAME)
        port = intent.getIntExtra(PORT, DEFAULT_PORT)
        textHostname.text = hostname
        deviceClient = DeviceClient(hostname, port, frameQueue)
        heatmap.colorStops = mapOf(0f to Color.BLUE, 20f to Color.GREEN, 50f to Color.YELLOW, 100f to Color.RED)
        heatmap.smooth = true
        Log.d("Display", "heatmap width: ${heatmap.width}")
        handlerThread.start()
        Log.d(javaClass.simpleName, "Started handler thread in ${Thread.currentThread()}")
        handler = Handler(handlerThread.looper)
    }

    override fun onStart() {
        super.onStart()
        deviceClientThread = Thread(DeviceClient(hostname, port, frameQueue))
        deviceClientThread!!.start()
        Log.d("Display", "heatmap width2: ${heatmap.width}")
        handler!!.post(HeatFrameUpdater())
    }

    override fun onStop() {
        super.onStop()
        deviceClient!!.stop()
    }
}

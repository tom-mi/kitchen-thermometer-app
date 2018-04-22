package de.rfnbrgr.kitchenthermometer

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

val HEAT_FRAME_ACTION = "heat-frame"
val HEAT_FRAME_PAYLOAD = "frame"
val CLIENT_STATE_ACTION = "client-state"
val CLIENT_STATE_PAYLOAD = "state"
val CLIENT_STATE_HOSTNAME = "hostname"

class DeviceClientService : Service() {
    companion object {
        private val PORT = 5000
    }

    private var deviceClient: DeviceClient? = null
    private var deviceClientThread: Thread? = null

    private var preferencesListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private var currentHostname: String = ""
    @Volatile private var interpolate: Boolean = false

    override fun onCreate() {
        preferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == getString(R.string.pref_hostname)) {
                restartDeviceClientThread()
            }
            if (key == getString(R.string.pref_interpolate_heatmap)) {
                configureInterpolation()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(javaClass.simpleName, "${Thread.currentThread().name}: Starting service")
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.registerOnSharedPreferenceChangeListener(preferencesListener)
        configureInterpolation()
        restartDeviceClientThread()

        return START_STICKY  // TODO is that correct?
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.unregisterOnSharedPreferenceChangeListener(preferencesListener)
        stopDeviceClientThread()
    }

    private fun startDeviceClientThread() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        currentHostname = sharedPref.getString(getString(R.string.pref_hostname), "")
        Log.d(javaClass.simpleName, "Starting device client for ${currentHostname}")

        deviceClient = DeviceClient(currentHostname, PORT, this::sendFrame, this::sendState)
        deviceClientThread = Thread(deviceClient)
        deviceClientThread!!.start()
    }

    private fun stopDeviceClientThread() {
        Log.d(javaClass.simpleName, "Stopping device client ${deviceClientThread?.name}")
        deviceClient?.stop()
        deviceClientThread?.join(1000)
        Log.d(javaClass.simpleName, "Stopped device client: ${deviceClientThread?.isAlive}")
    }

    @Synchronized private fun restartDeviceClientThread() {
        stopDeviceClientThread()
        startDeviceClientThread()
    }

    private fun configureInterpolation() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        interpolate = sharedPref.getBoolean(getString(R.string.pref_interpolate_heatmap), false)
    }

    private fun sendFrame(frame: HeatFrame) {
        val intent = Intent(HEAT_FRAME_ACTION)
        val enrichedFrame = enrichHeatFrame(frame, interpolate)
        intent.putExtra(HEAT_FRAME_PAYLOAD, enrichedFrame)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendState(state: DeviceClient.ClientState) {
        val intent = Intent(CLIENT_STATE_ACTION)
        intent.putExtra(CLIENT_STATE_PAYLOAD, state)
        intent.putExtra(CLIENT_STATE_HOSTNAME, currentHostname)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
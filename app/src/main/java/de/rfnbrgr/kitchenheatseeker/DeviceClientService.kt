package de.rfnbrgr.kitchenheatseeker

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager

val HEAT_FRAME_ACTION = "heat-frame"
val HEAT_FRAME_PAYLOAD = "frame"
val CLIENT_STATE_ACTION = "client-state"
val CLIENT_STATE_PAYLOAD = "state"

class DeviceClientService : Service() {
    companion object {
        private val PORT = 5000
    }

    private var deviceClient: DeviceClient? = null
    private var deviceClientThread: Thread? = null

    private var preferencesListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        preferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == getString(R.string.pref_hostname)) {
                this.restartDeviceClientThread()
            }
        }
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.registerOnSharedPreferenceChangeListener(preferencesListener)

        return START_STICKY  // TODO is that correct?
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.unregisterOnSharedPreferenceChangeListener(preferencesListener)
    }

    private fun startDeviceClientThread() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val hostname = sharedPref.getString(getString(R.string.pref_hostname), "")

        deviceClient = DeviceClient(hostname, PORT, this::sendFrame, this::sendState)
        deviceClientThread = Thread(deviceClient)
        deviceClientThread!!.start()
    }

    private fun stopDeviceClientThread() {
        deviceClient?.stop()
        deviceClientThread?.join()
    }

    private fun restartDeviceClientThread() {
        stopDeviceClientThread()
        startDeviceClientThread()
    }

    private fun sendFrame(frame: HeatFrame) {
        val intent = Intent(HEAT_FRAME_ACTION)
        intent.putExtra(HEAT_FRAME_PAYLOAD, frame)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendState(state: DeviceClient.ClientState) {
        val intent = Intent(CLIENT_STATE_ACTION)
        intent.putExtra(CLIENT_STATE_PAYLOAD, state)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
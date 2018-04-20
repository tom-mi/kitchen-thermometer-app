package de.rfnbrgr.kitchenheatseeker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_heat_display.*

class HeatDisplay : AppCompatActivity() {

    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == HEAT_FRAME_ACTION) {
                val frame = intent.getParcelableExtra<HeatFrame>(HEAT_FRAME_PAYLOAD)
                heatmap.setFrame(frame)
                textClientStatus.text = "Receiving"
            }
            if (intent?.action == CLIENT_STATE_ACTION) {
                val state = intent.getSerializableExtra(CLIENT_STATE_PAYLOAD) as DeviceClient.ClientState
                textClientStatus.text = state.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        setContentView(R.layout.activity_heat_display)

        configureHeatmap()
    }

    override fun onStart() {
        super.onStart()
        Log.d("Display", "heatmap width2: ${heatmap.width}")
        val filter = IntentFilter()
        filter.addAction(HEAT_FRAME_ACTION)
        filter.addAction(CLIENT_STATE_ACTION)
        registerReceiver(receiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

    private fun configureHeatmap() {
        heatmap.colorStops = mapOf(0f to Color.BLUE, 20f to Color.GREEN, 50f to Color.YELLOW, 100f to Color.RED)
        heatmap.smooth = getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.pref_smoothHeatmap), false)
    }
}

package de.rfnbrgr.kitchenthermometer

import android.app.Fragment
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_min_max_temperature.*

class MinMaxTemperatureFragment : Fragment() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == HEAT_FRAME_ACTION) {
                val frame = intent.getParcelableExtra<EnrichedHeatFrame>(HEAT_FRAME_PAYLOAD)
                textMinTemperature.text = getString(R.string.celsius_temperature, frame.minTemperature)
                textMaxTemperature.text = getString(R.string.celsius_temperature, frame.maxTemperature)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_min_max_temperature, container, false)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, IntentFilter(HEAT_FRAME_ACTION))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)
    }
}
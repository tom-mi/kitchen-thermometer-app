package de.rfnbrgr.kitchenthermometer

import android.app.Fragment
import android.content.*
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rfnbrgr.kitchenthermometer.AlarmSettingsFragment.Companion.TEMPERATURE_RANGE_MAX_ARGUMENT
import de.rfnbrgr.kitchenthermometer.AlarmSettingsFragment.Companion.TEMPERATURE_RANGE_MIN_ARGUMENT
import kotlinx.android.synthetic.main.fragment_alarm.*

class AlarmFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {


    private var temperatureRange: Pair<Float, Float> = Pair(0f, 100f)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == HEAT_FRAME_ACTION) {
                val frame = intent.getParcelableExtra<EnrichedHeatFrame>(HEAT_FRAME_PAYLOAD)
                alarmView.setFrame(frame)
                temperatureRange = frame.temperatureRange
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_alarm, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this.activity)
        sharedPref.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        configureAlarmView()
        editAlarmsButton.setOnClickListener {
            val intent = Intent(this.activity, AlarmSettingsActivity::class.java)
            intent.putExtra(TEMPERATURE_RANGE_MIN_ARGUMENT, temperatureRange.first)
            intent.putExtra(TEMPERATURE_RANGE_MAX_ARGUMENT, temperatureRange.second)
            startActivity(intent)
        }
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, IntentFilter(HEAT_FRAME_ACTION))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key in listOf<String>(
                        getString(R.string.pref_alarm_lower_limit),
                        getString(R.string.pref_alarm_upper_limit))) {
            configureAlarmView()
        }
    }

    private fun configureAlarmView() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this.activity)
        val lowerLimit = sharedPref.getInt(getString(R.string.pref_alarm_lower_limit), 10).toFloat()
        val upperLimit = sharedPref.getInt(getString(R.string.pref_alarm_upper_limit), 40).toFloat()

        alarmView.setAlarms(lowerLimit, upperLimit, true, true)
    }

}
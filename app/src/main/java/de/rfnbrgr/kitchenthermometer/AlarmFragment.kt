package de.rfnbrgr.kitchenthermometer

import android.app.Fragment
import android.content.*
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            startActivity(Intent(this.activity, AlarmSettingsActivity::class.java))
        }
        toggleAlarmButton.setOnCheckedChangeListener { _, isChecked ->
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this.activity)
            Log.d(javaClass.simpleName, "Setting alarmEnabled=$isChecked due to toggle")
            sharedPref.edit().putBoolean(getString(R.string.pref_alarm_enabled), isChecked).apply()
        }
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, IntentFilter(HEAT_FRAME_ACTION))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key in listOf<String>(
                        getString(R.string.pref_alarm_enabled),
                        getString(R.string.pref_alarm_lower_limit),
                        getString(R.string.pref_alarm_upper_limit))) {
            configureAlarmView()
        }
    }

    private fun configureAlarmView() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this.activity)
        val lowerLimit = getFloatPreference(this.activity, getString(R.string.pref_alarm_lower_limit))
        val upperLimit = getFloatPreference(this.activity, getString(R.string.pref_alarm_upper_limit))
        val alarmEnabled = sharedPref.getBoolean(getString(R.string.pref_alarm_enabled), false)

        alarmView.setAlarms(lowerLimit, upperLimit, alarmEnabled)
        toggleAlarmButton.isChecked = alarmEnabled
    }

}
package de.rfnbrgr.kitchenthermometer

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import de.rfnbrgr.kitchenthermometer.AlarmSettingsFragment.Companion.TEMPERATURE_RANGE_MAX_ARGUMENT
import de.rfnbrgr.kitchenthermometer.AlarmSettingsFragment.Companion.TEMPERATURE_RANGE_MIN_ARGUMENT

class AlarmSettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

    override fun onStart() {
        super.onStart()
        val fragment = AlarmSettingsFragment()
        val bundle = Bundle()

        val temperatureRangeMin = intent.extras.getFloat(TEMPERATURE_RANGE_MIN_ARGUMENT)
        val temperatureRangeMax = intent.extras.getFloat(TEMPERATURE_RANGE_MAX_ARGUMENT)
        bundle.putFloat(TEMPERATURE_RANGE_MIN_ARGUMENT, temperatureRangeMin)
        bundle.putFloat(TEMPERATURE_RANGE_MAX_ARGUMENT, temperatureRangeMax)
        fragment.arguments = bundle

        fragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit()
    }
}
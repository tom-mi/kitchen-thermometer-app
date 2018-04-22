package de.rfnbrgr.kitchenthermometer

import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment

class SettingsFragment: PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences)

        val temperatureScalePref = findPreference(getString(R.string.pref_temperature_scale)) as ListPreference
        val temperatureScaleMin = findPreference(getString(R.string.pref_temperature_range_min))
        val temperatureScaleMax = findPreference(getString(R.string.pref_temperature_range_max))

        temperatureScalePref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
            val enabled = (newValue as String == getString(R.string.pref_temperature_scale_value_fixed))
            temperatureScaleMin.isEnabled = enabled
            temperatureScaleMax.isEnabled = enabled
            true
        }

        val enabled = (temperatureScalePref.value == getString(R.string.pref_temperature_scale_value_fixed))
        temperatureScaleMin.isEnabled = enabled
        temperatureScaleMax.isEnabled = enabled
    }
}
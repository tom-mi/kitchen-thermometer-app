package de.rfnbrgr.kitchenthermometer

import android.os.Bundle
import android.support.v14.preference.PreferenceFragment
import android.support.v7.preference.SeekBarPreference

class AlarmSettingsFragment : PreferenceFragment() {

    companion object {
        const val TEMPERATURE_RANGE_MIN_ARGUMENT = "temperature_range_min"
        const val TEMPERATURE_RANGE_MAX_ARGUMENT = "temperature_range_max"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.alarm_preferences)
        val rangeMin = arguments.getFloat(TEMPERATURE_RANGE_MIN_ARGUMENT, 0f)
        val rangeMax = arguments.getFloat(TEMPERATURE_RANGE_MAX_ARGUMENT, 0f)

        val lowerLimitPref = findPreference(getString(R.string.pref_alarm_lower_limit)) as SeekBarPreference
        val upperLimitPref = findPreference(getString(R.string.pref_alarm_upper_limit)) as SeekBarPreference


        lowerLimitPref.min = rangeMin.toInt()
        lowerLimitPref.max = rangeMax.toInt()
        upperLimitPref.min = rangeMin.toInt()
        upperLimitPref.max = rangeMax.toInt()
    }

}
package de.rfnbrgr.kitchenthermometer

import android.os.Bundle
import android.preference.PreferenceFragment

class AlarmSettingsFragment : PreferenceFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.alarm_preferences)
    }

}
package de.rfnbrgr.kitchenthermometer

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity

class AlarmSettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.alarm_preferences, false)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, AlarmSettingsFragment())
                .commit()
    }

}
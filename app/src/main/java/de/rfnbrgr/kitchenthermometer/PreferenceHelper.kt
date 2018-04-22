package de.rfnbrgr.kitchenthermometer

import android.content.Context
import android.preference.PreferenceManager

fun getFloatPreference(context: Context, key: String): Float {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    return try {
        sharedPref.getString(key, "0").toFloat()
    } catch (e: NumberFormatException) {
        0f
    }
}
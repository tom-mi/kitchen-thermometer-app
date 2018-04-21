package de.rfnbrgr.kitchenthermometer

import android.content.Context
import android.preference.EditTextPreference
import android.util.AttributeSet

@Suppress("unused")
class BetterEditTextPreference : EditTextPreference {
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    override fun getSummary(): CharSequence {
        return super.getSummary().toString().replace("%s", text ?: "")
    }
}
package de.rfnbrgr.kitchenthermometer

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class HeatFrame(
        val battery: Float = 0f,
        val width: Int = 0,
        val height: Int = 0,
        val temperatureRangeMin: Float = 0f,
        val temperatureRangeMax: Float = 1f,
        val temperatures: List<Float> = listOf()
) : Parcelable

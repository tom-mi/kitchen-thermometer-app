package de.rfnbrgr.kitchenthermometer

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class EnrichedHeatFrame(
        val battery: Float = 0f,
        val batteryVoltage: Float = 0f,
        val width: Int = 0,
        val height: Int = 0,
        val temperatureRange: Pair<Float, Float> = Pair(0f, 1f),
        val temperatures: List<List<Float>> = listOf(),
        val minTemperature: Float = Float.POSITIVE_INFINITY,
        val maxTemperature: Float = Float.NEGATIVE_INFINITY,
        val minPosition: Pair<Int, Int> = Pair(0, 0),
        val maxPosition: Pair<Int, Int> = Pair(0, 0)
        ) : Parcelable

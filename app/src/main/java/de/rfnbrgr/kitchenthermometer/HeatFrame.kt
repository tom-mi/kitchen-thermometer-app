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
        val temperatures: List<Float> = listOf()
) : Parcelable

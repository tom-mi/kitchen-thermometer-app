package de.rfnbrgr.kitchenheatseeker

data class HeatFrame(
        val battery: Float = 0f,
        val width: Int = 0,
        val height: Int = 0,
        val temperatures: List<Float> = listOf()
)

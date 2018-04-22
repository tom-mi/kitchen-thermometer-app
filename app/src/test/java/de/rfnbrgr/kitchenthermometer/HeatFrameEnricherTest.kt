package de.rfnbrgr.kitchenthermometer

import org.junit.Assert.*
import org.junit.Test

class HeatFrameEnricherTest {

    companion object {
        val BATTERY = 0.7f
        val SMALL_WIDTH = 3
        val SMALL_HEIGHT = 2
        val SMALL_FRAME = HeatFrame(BATTERY, SMALL_WIDTH, SMALL_HEIGHT, listOf(2f, 1f, 2f, 3f, 4f, 5f))
        val WIDTH = 5
        val HEIGHT = 6
        val FRAME = HeatFrame(BATTERY, WIDTH, HEIGHT,
                listOf(
                        0f, 0f, 1f, 1f, 0f,
                        0f, 0f, 1f, 1f, 0f,
                        2f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, -1f, 0f, 3f, 0f,
                        0f, 0f, 0f, 0f, 0f
                ))
        val MAX_FRAME = HeatFrame(BATTERY, 5, 5,
                listOf(
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 1f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f
                ))
        val MIN_FRAME = HeatFrame(BATTERY, 5, 5,
                listOf(
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, -1f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f,
                        0f, 0f, 0f, 0f, 0f
                ))
        val SCALE = 5
    }

    @Test
    fun enrich_empty_frame() {
        assertEquals(EnrichedHeatFrame(), enrichHeatFrame(HeatFrame(), false))
    }

    @Test
    fun enrich_HeatFrame_without_interpolation() {
        val result = enrichHeatFrame(FRAME, false)

        assertEquals(BATTERY, result.battery)
        assertEquals(WIDTH, result.width)
        assertEquals(HEIGHT , result.height)
        assertEquals(WIDTH , result.temperatures.size)
        assertEquals(HEIGHT, result.temperatures.first().size)
        assertEquals(HEIGHT, result.temperatures.last().size)
        assertEquals(2f, result.temperatures[0][2])
        assertEquals(Pair(1, 4), result.minPosition)
        assertEquals(Pair(3, 4), result.maxPosition)
        assertEquals(-1f, result.minTemperature)
        assertEquals(3f, result.maxTemperature)
    }

    @Test
    fun enrich_HeatFrame_with_interpolation_for_small_frame() {
        // Small frames cannot be interpolated
        val result = enrichHeatFrame(SMALL_FRAME, true)

        assertEquals(BATTERY, result.battery)
        assertEquals(SMALL_WIDTH , result.width)
        assertEquals(SMALL_HEIGHT , result.height)
        assertEquals(SMALL_WIDTH, result.temperatures.size)
        assertEquals(SMALL_HEIGHT, result.temperatures.first().size)
        assertEquals(SMALL_HEIGHT, result.temperatures.last().size)
        assertEquals(3f, result.temperatures[0][1])
        assertEquals(Pair(2, 1), result.maxPosition)
        assertEquals(Pair(1, 0), result.minPosition)
        assertEquals(1f, result.minTemperature)
        assertEquals(5f, result.maxTemperature)
    }

    @Test
    fun enrich_HeatFrame_with_interpolation_for_large_frame() {
        val result = enrichHeatFrame(FRAME, true)

        assertEquals(BATTERY, result.battery)
        assertEquals(WIDTH * SCALE, result.width)
        assertEquals(HEIGHT  * SCALE, result.height)
        assertEquals(WIDTH  * SCALE, result.temperatures.size)
        assertEquals(HEIGHT * SCALE, result.temperatures.first().size)
        assertEquals(HEIGHT * SCALE, result.temperatures.last().size)
        assertEquals(2f, result.temperatures[0 + 2][2 * 5 + 2], 0.1f)
    }

    @Test
    fun enrich_HeatFrame_with_interpolation_returns_correct_max() {
        val result = enrichHeatFrame(MAX_FRAME, true)

        assertEquals(Pair(12, 12), result.maxPosition)
        assertEquals(1f, result.maxTemperature, 0.1f)
    }

    @Test
    fun enrich_HeatFrame_with_interpolation_returns_correct_min() {
        val result = enrichHeatFrame(MIN_FRAME, true)

        assertEquals(Pair(12, 12), result.minPosition)
        assertEquals(-1f, result.minTemperature, 0.1f)
    }
}
package de.rfnbrgr.kitchenthermometer

import android.graphics.Color
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeatmapViewTest {

    @Test
    fun test_interpolation() {
        val colorStops = mapOf(0f to Color.BLUE, 0.2f to Color.BLACK, 1f to Color.WHITE)

        assertEquals(Color.BLUE, interpolate(colorStops, 0f))
        assertEquals(Color.BLUE, interpolate(colorStops, -1f))
        assertEquals(Color.BLACK, interpolate(colorStops, 0.2f))
        assertEquals(Color.WHITE, interpolate(colorStops, 1f))
        assertEquals(Color.WHITE, interpolate(colorStops, 2f))
    }

}
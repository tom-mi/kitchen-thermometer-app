package de.rfnbrgr.kitchenthermometer

import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DeviceClientTest {

    companion object {
        val PORT = 5000
        val FRAMES = listOf(
                "{\"battery\": 0.4, \"temperatureRangeMin\": 0, \"temperatureRangeMax\": 1, \"temperatures\": [[0.1, 0.2, 0.3], [0.4, 0.5, 0.6]]}",
                "{\"battery\": 0.3, \"temperatureRangeMin\": 0, \"temperatureRangeMax\": 1, \"temperatures\": [[0.1, 0.2, 0.3], [0.7, 0.8, 0.9]]}")
    }

    @Rule
    @JvmField
    val testDeviceRule = TestDeviceRule()

    @Test
    fun receives_frames() {
        val results: MutableList<HeatFrame> = mutableListOf()

        val client = DeviceClient("localhost", PORT, { frame -> results.add(frame) }, {})

        val thread = Thread(client)
        thread.start()
        Thread.sleep(1000)

        testDeviceRule.server.send(FRAMES[0])
        testDeviceRule.server.send(FRAMES[1])

        Thread.sleep(1000)

        client.stop()
        thread.join()

        assertEquals(results.size, 2)

        assertEquals(0.4f, results[0].battery, 0.01f)
        assertEquals(0f, results[0].temperatureRangeMin)
        assertEquals(1f, results[0].temperatureRangeMax)
        assertEquals(0.1f, results[0].temperatures[0][0], 0.01f)
        assertEquals(0.6f, results[0].temperatures[1][2], 0.01f)

        assertEquals(0.3f, results[1].battery, 0.01f)
        assertEquals(0f, results[1].temperatureRangeMin)
        assertEquals(1f, results[1].temperatureRangeMax)
        assertEquals(0.1f, results[1].temperatures[0][0], 0.01f)
        assertEquals(0.9f, results[1].temperatures[1][2], 0.01f)
    }
}

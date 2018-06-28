package de.rfnbrgr.kitchenthermometer

import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator
import java.lang.Math.max
import java.lang.Math.min

const val INTERPOLATE_PIXEL_MULTIPLIER = 5
const val MIN_INTERPOLATION_POINTS = 5

fun enrichHeatFrame(frame: HeatFrame, interpolate: Boolean): EnrichedHeatFrame {
    return if (interpolate && frame.width >= MIN_INTERPOLATION_POINTS && frame.height >= MIN_INTERPOLATION_POINTS) {
        enrichHeatFrameWithInterpolation(frame)
    } else {
        enrichHeatFrameWithoutInterpolation(frame)
    }
}

fun enrichHeatFrameWithoutInterpolation(frame: HeatFrame): EnrichedHeatFrame {
    var minTemperature = Float.POSITIVE_INFINITY
    var maxTemperature = Float.NEGATIVE_INFINITY
    var minPosition = Pair(0, 0)
    var maxPosition = Pair(0, 0)

    val temperatures2d = (0 until frame.width).map { i ->
        (0 until frame.height).map { j ->
            val value = frame.temperatures[i + frame.width * j]
            if (value < minTemperature) {
                minTemperature = value
                minPosition = Pair(i, j)
            }
            if (value > maxTemperature) {
                maxTemperature = value
                maxPosition = Pair(i, j)
            }
            value
        }
    }

    return EnrichedHeatFrame(frame.battery, frame.batteryVoltage, frame.width, frame.height, Pair(frame.temperatureRangeMin, frame.temperatureRangeMax),
            temperatures2d, minTemperature, maxTemperature, minPosition, maxPosition)
}

private fun enrichHeatFrameWithInterpolation(frame: HeatFrame): EnrichedHeatFrame {
    val xVal = (0 until frame.width).map { 0.5 + it }.toDoubleArray()
    val yVal = (0 until frame.height).map { 0.5 + it }.toDoubleArray()
    val fVal = (0 until frame.width).map { i ->
        (0 until frame.height).map { j ->
            frame.temperatures[i + frame.width * j].toDouble()
        }.toDoubleArray()
    }.toTypedArray()


    val f = PiecewiseBicubicSplineInterpolator().interpolate(xVal, yVal, fVal)

    var minTemperature = Float.POSITIVE_INFINITY
    var maxTemperature = Float.NEGATIVE_INFINITY
    var minPosition = Pair(0, 0)
    var maxPosition = Pair(0, 0)

    val temperatures2d = (0 until frame.width * INTERPOLATE_PIXEL_MULTIPLIER).map { i ->
        (0 until frame.height * INTERPOLATE_PIXEL_MULTIPLIER).map { j ->
            val x = max(xVal.first(), min(xVal.last(), i.toDouble() / INTERPOLATE_PIXEL_MULTIPLIER))
            val y = max(yVal.first(), min(yVal.last(), j.toDouble() / INTERPOLATE_PIXEL_MULTIPLIER))
            val value = f.value(x, y).toFloat()
            if (value < minTemperature) {
                minTemperature = value
                minPosition = Pair(i, j)
            }
            if (value > maxTemperature) {
                maxTemperature = value
                maxPosition = Pair(i, j)
            }
            value
        }
    }

    return EnrichedHeatFrame(frame.battery, frame.batteryVoltage,
            frame.width * INTERPOLATE_PIXEL_MULTIPLIER, frame.height * INTERPOLATE_PIXEL_MULTIPLIER,
            Pair(frame.temperatureRangeMin, frame.temperatureRangeMax),
            temperatures2d, minTemperature, maxTemperature, minPosition, maxPosition)
}

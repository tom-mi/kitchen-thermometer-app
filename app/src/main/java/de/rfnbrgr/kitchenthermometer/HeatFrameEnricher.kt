package de.rfnbrgr.kitchenthermometer

import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator
import java.lang.Math.max
import java.lang.Math.min

const val INTERPOLATE_PIXEL_MULTIPLIER = 5
const val MIN_INTERPOLATION_POINTS = 5

data class Shape(val width: Int, val height: Int)

fun enrichHeatFrame(frame: HeatFrame, interpolate: Boolean): EnrichedHeatFrame {
    val shape = determineShape(frame)
    return if (interpolate && shape.width >= MIN_INTERPOLATION_POINTS && shape.height >= MIN_INTERPOLATION_POINTS) {
        enrichHeatFrameWithInterpolation(frame, shape)
    } else {
        enrichHeatFrameWithoutInterpolation(frame, shape)
    }
}

fun determineShape(frame: HeatFrame): Shape {
    return Shape(frame.temperatures.getOrNull(0)?.size ?: 0, frame.temperatures.size)
}

fun enrichHeatFrameWithoutInterpolation(frame: HeatFrame, shape: Shape): EnrichedHeatFrame {
    var minTemperature = Float.POSITIVE_INFINITY
    var maxTemperature = Float.NEGATIVE_INFINITY
    var minPosition = Pair(0, 0)
    var maxPosition = Pair(0, 0)

    val temperatures2d = (0 until shape.width).map { i ->
        (0 until shape.height).map { j ->
            val value = frame.temperatures[j][i]
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

    return EnrichedHeatFrame(frame.battery, frame.batteryVoltage, shape.width, shape.height, Pair(frame.temperatureRangeMin, frame.temperatureRangeMax),
            temperatures2d, minTemperature, maxTemperature, minPosition, maxPosition)
}

private fun enrichHeatFrameWithInterpolation(frame: HeatFrame, shape: Shape): EnrichedHeatFrame {
    val xVal = (0 until shape.width).map { 0.5 + it }.toDoubleArray()
    val yVal = (0 until shape.height).map { 0.5 + it }.toDoubleArray()
    val fVal = (0 until shape.width).map { i ->
        (0 until shape.height).map { j ->
            frame.temperatures[j][i].toDouble()
        }.toDoubleArray()
    }.toTypedArray()


    val f = PiecewiseBicubicSplineInterpolator().interpolate(xVal, yVal, fVal)

    var minTemperature = Float.POSITIVE_INFINITY
    var maxTemperature = Float.NEGATIVE_INFINITY
    var minPosition = Pair(0, 0)
    var maxPosition = Pair(0, 0)

    val temperatures2d = (0 until shape.width * INTERPOLATE_PIXEL_MULTIPLIER).map { i ->
        (0 until shape.height * INTERPOLATE_PIXEL_MULTIPLIER).map { j ->
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
            shape.width * INTERPOLATE_PIXEL_MULTIPLIER, shape.height * INTERPOLATE_PIXEL_MULTIPLIER,
            Pair(frame.temperatureRangeMin, frame.temperatureRangeMax),
            temperatures2d, minTemperature, maxTemperature, minPosition, maxPosition)
}

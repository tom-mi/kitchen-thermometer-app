package de.rfnbrgr.kitchenthermometer

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.lang.Math.min

class HeatmapView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var currentFrame: EnrichedHeatFrame = EnrichedHeatFrame()

    var colorStops: Map<Float, Int> = mapOf(0f to Color.BLACK, 1f to Color.WHITE)
    var smooth: Boolean = false
    var temperatureScale: TemperatureScale = TemperatureScale.AUTO
    var fixedTemperatureRange: Pair<Float, Float> = Pair(0f, 100f)

    private val bitmapPaintPixels = Paint()
    private val bitmapPaintPixelsSmooth = Paint()
    private var bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var bitmapRect = Rect(0, 0, bitmap.width, bitmap.height)
    private var drawingRect = Rect(0, 0, 0, 0)


    init {
        bitmapPaintPixels.isAntiAlias = false
        bitmapPaintPixels.isDither = false
        bitmapPaintPixels.isFilterBitmap = false
        bitmapPaintPixelsSmooth.isAntiAlias = false
        bitmapPaintPixelsSmooth.isDither = false
        bitmapPaintPixelsSmooth.isFilterBitmap = true
    }

    fun setFrame(newFrame: EnrichedHeatFrame) {
        currentFrame = newFrame
        calculateScaleAndOffset()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateScaleAndOffset()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (currentFrame.height * currentFrame.width == 0) {
            return
        }

        createBitmap()
        setBitmapPixels(currentFrame)
        drawBitmapWithColors(canvas, bitmap)
    }

    private fun createBitmap() {
        if (currentFrame.width != bitmap.width || currentFrame.height != bitmap.height) {
            bitmap = Bitmap.createBitmap(currentFrame.width, currentFrame.height, Bitmap.Config.ARGB_8888)
            bitmapRect = Rect(0, 0, bitmap.width, bitmap.height)
        }
    }

    private fun setBitmapPixels(frame: EnrichedHeatFrame) {
        for (x in 0..(frame.width - 1)) {
            for (y in 0..(frame.height - 1)) {
                val color = interpolateWithScale(frame, frame.temperatures[x][y])
                bitmap.setPixel(x, y, color)
            }
        }
    }

    private fun interpolateWithScale(frame: EnrichedHeatFrame, temperature: Float): Int {
        val normalizedValue = when (temperatureScale) {
            TemperatureScale.AUTO -> scale(frame.minTemperature, frame.maxTemperature, temperature)
            TemperatureScale.FULL -> scale(frame.temperatureRange.first, frame.temperatureRange.second, temperature)
            TemperatureScale.FIXED -> scale(fixedTemperatureRange.first, fixedTemperatureRange.second, temperature)
        }
        return interpolate(colorStops, normalizedValue)
    }

    private fun scale(minValue: Float, maxValue: Float, value: Float): Float {
        return (value - minValue) / (maxValue - minValue)
    }

    private fun drawBitmapWithColors(canvas: Canvas?, bitmap: Bitmap) {
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val paint = if (smooth) bitmapPaintPixelsSmooth else bitmapPaintPixels
        canvas?.drawBitmap(bitmap, rect, drawingRect, paint)
    }

    private fun calculateScaleAndOffset() {
        val scale = min(width.toFloat() / currentFrame.width, height.toFloat() / currentFrame.height)
        val dx = (width - (currentFrame.width * scale)).toInt() / 2
        val dy = (height - (currentFrame.height * scale)).toInt() / 2

        drawingRect = Rect(dx, dy, width - dx, height - dy)
    }

}

fun interpolate(colorStops: Map<Float, Int>, value: Float): Int {
    val stops = colorStops.keys.sorted()

    val start = stops.lastOrNull { it <= value } ?: stops.first()
    val end = stops.firstOrNull { value <= it } ?: stops.last()

    if (start == end) {
        return colorStops[start] as Int
    }
    val fraction = (value - start) / (end - start)
    return ArgbEvaluator().evaluate(fraction, colorStops[start], colorStops[end]) as Int
}
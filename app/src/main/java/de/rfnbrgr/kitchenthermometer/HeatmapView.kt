package de.rfnbrgr.kitchenthermometer

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import org.apache.commons.math3.analysis.interpolation.PiecewiseBicubicSplineInterpolator
import java.lang.Math.max
import java.lang.Math.min

class HeatmapView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var frame: HeatFrame = HeatFrame()

    var colorStops: Map<Float, Int> = mapOf(0f to Color.BLACK, 1f to Color.WHITE)
    var smooth: Boolean = false

    private val bitmapPaintPixels = Paint()
    private var bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    private var bitmapRect = Rect(0, 0, bitmap.width, bitmap.height)
    private var drawingRect = Rect(0, 0, 0, 0)

    companion object {
        private val smoothPixelMultiplier = 5
    }

    init {
        bitmapPaintPixels.isAntiAlias = false
        bitmapPaintPixels.isDither = false
        bitmapPaintPixels.isFilterBitmap = false
    }

    fun setFrame(newFrame: HeatFrame) {
        Log.d(javaClass.name, "Setting frame")
        frame = newFrame
        val pixelMultiplier = if (smooth) smoothPixelMultiplier else 1
        if (frame.width * pixelMultiplier != bitmap.width || frame.height * pixelMultiplier != bitmap.height) {
            bitmap = Bitmap.createBitmap(frame.width * pixelMultiplier, frame.height * pixelMultiplier, Bitmap.Config.ARGB_8888)
            bitmapRect = Rect(0, 0, bitmap.width, bitmap.height)
        }
        calculateScaleAndOffset()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateScaleAndOffset()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (frame.height * frame.width == 0) {
            return
        }

        if (smooth) {
            setBitmapPixelsSmooth(frame)
        } else {
            setBitmapPixels(frame)
        }
        drawBitmapWithColors(canvas, bitmap)
    }

    private fun setBitmapPixels(frame: HeatFrame) {
        for (x in 0..(frame.width - 1)) {
            for (y in 0..(frame.height - 1)) {
                val color = interpolate(colorStops, frame.temperatures[x + y * frame.height])
                bitmap.setPixel(x, y, color)
            }
        }
    }

    private fun setBitmapPixelsSmooth(frame: HeatFrame) {
        val xVal = (0 until frame.width).map { 0.5 + it }.toDoubleArray()
        val yVal = (0 until frame.height).map { 0.5 + it }.toDoubleArray()
        val fVal = (0 until frame.width).map { i ->
            (0 until frame.height).map { j ->
                frame.temperatures[i + frame.width * j].toDouble()
            }.toDoubleArray()
        }.toTypedArray()

        val f = PiecewiseBicubicSplineInterpolator().interpolate(xVal, yVal, fVal)


        for (i in 0 until frame.width * smoothPixelMultiplier) {
            for (j in 0 until frame.height * smoothPixelMultiplier) {
                val x = max(xVal.first(), min(xVal.last(), i.toDouble() / smoothPixelMultiplier))
                val y = max(yVal.first(), min(yVal.last(), j.toDouble() / smoothPixelMultiplier))
                val color = interpolate(colorStops, f.value(x, y).toFloat())
                bitmap.setPixel(i, j, color)
            }
        }
    }

    private fun drawBitmapWithColors(canvas: Canvas?, bitmap: Bitmap) {
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        canvas?.drawBitmap(bitmap, rect, drawingRect, bitmapPaintPixels)
    }

    private fun calculateScaleAndOffset() {
        val scale = min(width.toFloat() / frame.width, height.toFloat() / frame.height)
        val dx = (width - (frame.width * scale)).toInt() / 2
        val dy = (height - (frame.height * scale)).toInt() / 2

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
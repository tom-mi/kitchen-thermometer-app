package de.rfnbrgr.kitchenthermometer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class AlarmView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var currentFrame: EnrichedHeatFrame = EnrichedHeatFrame()
    private var lowerLimit: Float = 0f
    private var upperLimit: Float = 100f
    private var lowerLimitEnabled: Boolean = false
    private var upperLimitEnabled: Boolean = false

    private var barPaint = Paint()
    private var barBackgroundPaint = Paint()
    private var lowerLimitPaint = Paint()
    private var upperLimitPaint = Paint()

    init {
        barPaint.color = Color.GRAY
        barBackgroundPaint.color = Color.LTGRAY
        lowerLimitPaint.color = Color.BLUE
        lowerLimitPaint.strokeWidth = 5f
        upperLimitPaint.color = Color.RED
        upperLimitPaint.strokeWidth = 5f
    }

    companion object {
        const val BAR_HEIGHT_RELATIVE = 0.5f
    }

    fun setFrame(newFrame: EnrichedHeatFrame) {
        currentFrame = newFrame
        invalidate()
    }

    fun setAlarms(lowerLimit: Float, upperLimit: Float, lowerLimitEnabled: Boolean, upperLimitEnabled: Boolean) {
        this.lowerLimit = lowerLimit
        this.upperLimit = upperLimit
        this.lowerLimitEnabled = lowerLimitEnabled
        this.upperLimitEnabled = upperLimitEnabled
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawFrame(canvas, currentFrame)
    }

    private fun drawFrame(canvas: Canvas, frame: EnrichedHeatFrame) {
        if (currentFrame.height * currentFrame.width == 0) {
            return
        }

        val availableHeight = height - paddingTop - paddingBottom

        val barTop = paddingTop + (BAR_HEIGHT_RELATIVE / 2) * availableHeight
        val barBottom = paddingTop + (1 - BAR_HEIGHT_RELATIVE / 2) * availableHeight

        canvas.drawRect(
                temperatureToX(frame.temperatureRange.first, frame), barTop,
                temperatureToX(frame.temperatureRange.second, frame), barBottom, barBackgroundPaint)
        canvas.drawRect(
                temperatureToX(frame.temperatureRange.first, frame), barTop,
                temperatureToX(frame.maxTemperature, frame), barBottom, barPaint)

        val lowerMarkerX = temperatureToX(lowerLimit, frame)
        val upperMarkerX = temperatureToX(upperLimit, frame)
        canvas.drawLine(lowerMarkerX, paddingTop.toFloat(), lowerMarkerX, height - paddingBottom.toFloat(), lowerLimitPaint)
        canvas.drawLine(upperMarkerX, paddingTop.toFloat(), upperMarkerX, height - paddingBottom.toFloat(), upperLimitPaint)
    }

    private fun temperatureToX(temperature: Float, frame: EnrichedHeatFrame): Float {
        val relativeTemperature = (temperature - frame.temperatureRange.first) / (frame.temperatureRange.second - frame.temperatureRange.first)
        val availableWidth = width - paddingLeft - paddingRight
        return paddingLeft + relativeTemperature * availableWidth
    }

}
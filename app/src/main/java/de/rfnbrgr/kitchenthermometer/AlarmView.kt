package de.rfnbrgr.kitchenthermometer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.Ringtone
import android.media.RingtoneManager
import android.util.AttributeSet
import android.view.View

class AlarmView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var currentFrame: EnrichedHeatFrame = EnrichedHeatFrame()
    private var lowerLimit: Float = 0f
    private var upperLimit: Float = 100f
    private var alarmEnabled: Boolean = false

    private var barPaint = Paint()
    private var barBackgroundPaint = Paint()
    private var lowerLimitPaint = Paint()
    private var upperLimitPaint = Paint()
    private var alarmTone: Ringtone

    init {
        barPaint.color = Color.BLACK
        barBackgroundPaint.color = Color.LTGRAY
        lowerLimitPaint.color = Color.BLUE
        lowerLimitPaint.strokeWidth = 10f
        upperLimitPaint.color = Color.RED
        upperLimitPaint.strokeWidth = 10f
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        alarmTone = RingtoneManager.getRingtone(context, notification)
    }

    companion object {
        const val BAR_HEIGHT_RELATIVE = 0.5f
    }

    fun setFrame(newFrame: EnrichedHeatFrame) {
        currentFrame = newFrame
        invalidate()
    }

    fun setAlarms(lowerLimit: Float, upperLimit: Float, alarmEnabled: Boolean) {
        this.lowerLimit = lowerLimit
        this.upperLimit = upperLimit
        this.alarmEnabled = alarmEnabled
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

        if (alarmEnabled && upperLimit <= frame.maxTemperature) {
            canvas.drawColor(0xFFFF8080.toInt())
            alarmTone.play()
        } else if (alarmEnabled && frame.maxTemperature <= lowerLimit) {
            canvas.drawColor(0xFF8080FF.toInt())
            alarmTone.play()
        } else {
            alarmTone.stop()
        }

        canvas.drawRect(
                temperatureToX(frame.temperatureRange.first, frame), barTop,
                temperatureToX(frame.temperatureRange.second, frame), barBottom, barBackgroundPaint)
        canvas.drawRect(
                temperatureToX(frame.temperatureRange.first, frame), barTop,
                temperatureToX(frame.maxTemperature, frame), barBottom, barPaint)

        val lowerMarkerX = temperatureToX(lowerLimit, frame)
        val upperMarkerX = temperatureToX(upperLimit, frame)
        if (alarmEnabled) {
            canvas.drawLine(lowerMarkerX, paddingTop.toFloat(), lowerMarkerX, height - paddingBottom.toFloat(), lowerLimitPaint)
            canvas.drawLine(upperMarkerX, paddingTop.toFloat(), upperMarkerX, height - paddingBottom.toFloat(), upperLimitPaint)
        }
    }

    private fun temperatureToX(temperature: Float, frame: EnrichedHeatFrame): Float {
        val relativeTemperature = (temperature - frame.temperatureRange.first) / (frame.temperatureRange.second - frame.temperatureRange.first)
        val availableWidth = width - paddingLeft - paddingRight
        return paddingLeft + relativeTemperature * availableWidth
    }

}
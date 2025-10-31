package com.iie.st10089153.txdevsystems_app.ui.dashboard.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.max
import kotlin.math.sin

class GaugeBackgroundView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val TAG = "GaugeBackgroundView"

    private val arcStrokeDp = 10.7f
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, arcStrokeDp, resources.displayMetrics
        )
        strokeCap = Paint.Cap.BUTT
    }

    private val needleWidthDp = 2.667f
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, needleWidthDp, resources.displayMetrics
        )
        color = Color.parseColor("#E48079")
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 4f
    }

    private val rectF = RectF()

    // The SAFE temperature range (green zone)
    private var safeMin = -50f
    private var safeMax = 50f

    // The FULL gauge range (includes red danger zones)
    private var fullMin = -70f
    private var fullMax = 70f

    // Current temperature value
    private var currentValue = 0f

    private val startAngle = 135f
    private val sweepAngle = 270f
    private val dangerExtension = 20f  // How much red zone extends beyond safe zone

    /**
     * Set the safe operating range
     * @param min Lower temperature limit (safe zone starts here)
     * @param max Upper temperature limit (safe zone ends here)
     */
    fun updateRanges(min: Float, max: Float) {
        // Ensure safe range is correct (lower value first)
        safeMin = min(min, max)
        safeMax = max(min, max)

        // Extend gauge to include danger zones
        fullMin = safeMin - dangerExtension
        fullMax = safeMax + dangerExtension

        Log.d(TAG, "Safe range: $safeMin to $safeMax")
        Log.d(TAG, "Full gauge: $fullMin to $fullMax")

        invalidate()
    }

    /**
     * Animate needle to a temperature value
     */
    fun animateToValue(value: Float, duration: Long = 1000L) {
        // Clamp to full gauge range
        val clamped = value.coerceIn(fullMin, fullMax)

        Log.d(TAG, "Animating to value: $value (clamped: $clamped)")

        ValueAnimator.ofFloat(currentValue, clamped).apply {
            this.duration = duration
            addUpdateListener {
                currentValue = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    /**
     * Convert a temperature value to its angle position on the gauge
     */
    private fun valueToAngle(value: Float): Float {
        // Calculate position as fraction of full range
        val fraction = (value - fullMin) / (fullMax - fullMin)
        return startAngle + fraction * sweepAngle
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val strokeOffset = arcPaint.strokeWidth / 2
        rectF.set(
            strokeOffset,
            strokeOffset,
            width.toFloat() - strokeOffset,
            height.toFloat() - strokeOffset
        )

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width / 2f - strokeOffset

        // Calculate angles for each zone boundary
        val fullMinAngle = valueToAngle(fullMin)
        val safeMinAngle = valueToAngle(safeMin)
        val safeMaxAngle = valueToAngle(safeMax)
        val fullMaxAngle = valueToAngle(fullMax)

        // Draw RED zone BEFORE safe range (fullMin to safeMin)
        arcPaint.color = Color.RED
        canvas.drawArc(rectF, fullMinAngle, safeMinAngle - fullMinAngle, false, arcPaint)

        // Draw GREEN safe zone (safeMin to safeMax)
        arcPaint.color = Color.GREEN
        canvas.drawArc(rectF, safeMinAngle, safeMaxAngle - safeMinAngle, false, arcPaint)

        // Draw RED zone AFTER safe range (safeMax to fullMax)
        arcPaint.color = Color.RED
        canvas.drawArc(rectF, safeMaxAngle, fullMaxAngle - safeMaxAngle, false, arcPaint)

        // Draw tick marks
        val tickCount = 10
        val outerRadius = radius + arcPaint.strokeWidth / 2
        val innerRadius = radius - arcPaint.strokeWidth / 2

        for (i in 0..tickCount) {
            val fraction = i / tickCount.toFloat()
            val angle = Math.toRadians((startAngle + fraction * sweepAngle).toDouble())
            val startX = centerX + outerRadius * cos(angle).toFloat()
            val startY = centerY + outerRadius * sin(angle).toFloat()
            val endX = centerX + innerRadius * cos(angle).toFloat()
            val endY = centerY + innerRadius * sin(angle).toFloat()
            canvas.drawLine(startX, startY, endX, endY, tickPaint)
        }

        // Draw gauge range labels
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }

        val labelRadius = radius + 40f
        val verticalOffset = 20f

        // Left label (gauge minimum)
        canvas.drawText(
            "${fullMin.toInt()}°",
            centerX + labelRadius * cos(Math.toRadians(startAngle.toDouble())).toFloat(),
            centerY + labelRadius * sin(Math.toRadians(startAngle.toDouble())).toFloat() + verticalOffset,
            labelPaint
        )

        // Right label (gauge maximum)
        canvas.drawText(
            "${fullMax.toInt()}°",
            centerX + labelRadius * cos(Math.toRadians((startAngle + sweepAngle).toDouble())).toFloat(),
            centerY + labelRadius * sin(Math.toRadians((startAngle + sweepAngle).toDouble())).toFloat() + verticalOffset,
            labelPaint
        )

        // Draw current value in center
        canvas.drawText(
            String.format("%.1f°C", currentValue),
            centerX,
            centerY + radius / 2f + 10f,
            textPaint
        )

        // Draw needle
        val needleLength = radius
        val needleAngle = valueToAngle(currentValue)
        val needleX = centerX + needleLength * cos(Math.toRadians(needleAngle.toDouble())).toFloat()
        val needleY = centerY + needleLength * sin(Math.toRadians(needleAngle.toDouble())).toFloat()
        canvas.drawLine(centerX, centerY, needleX, needleY, needlePaint)

        // Draw needle center circle with gradient
        val centerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9.33f, resources.displayMetrics)
        val gradient = RadialGradient(
            centerX, centerY, centerRadius,
            Color.parseColor("#333333"),
            Color.parseColor("#1F1F1F"),
            Shader.TileMode.CLAMP
        )
        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = gradient }
        canvas.drawCircle(centerX, centerY, centerRadius, centerPaint)

        // Debug logging
        if (currentValue != 0f) {
            val inSafeZone = currentValue in safeMin..safeMax
            Log.d(TAG, "Current: $currentValue, Safe: $safeMin-$safeMax, In safe zone: $inSafeZone")
        }
    }
}
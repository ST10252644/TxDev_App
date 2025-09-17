package com.iie.st10089153.txdevsystems_app.ui.dashboard.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class GaugeBackgroundView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val arcStrokeDp = 10.7f
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, arcStrokeDp, resources.displayMetrics
        )
        strokeCap = Paint.Cap.BUTT
    }

    private val arcBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics
        )
        color = Color.WHITE
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
    var minValue = -50f
    var maxValue = 50f
    private var currentValue = 0f

    private var dangerBefore = 0f
    private var dangerAfter = 0f
    private var safeStart = 0f
    private var safeEnd = 0f

    private val startAngle = 135f
    private val sweepAngle = 270f

    fun updateRanges(min: Float, max: Float) {
        minValue = min
        maxValue = max

        // Expand arc beyond min/max by 20 units
        dangerBefore = minValue - 20f
        dangerAfter = maxValue + 20f

        safeStart = minValue
        safeEnd = maxValue

        invalidate()
    }

    fun animateToValue(value: Float, duration: Long = 1000L) {
        val clamped = value.coerceIn(minValue, maxValue)
        ValueAnimator.ofFloat(currentValue, clamped).apply {
            this.duration = duration
            addUpdateListener {
                currentValue = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun valueToAngle(value: Float): Float {
        val fraction = (value - dangerBefore) / (dangerAfter - dangerBefore)
        return startAngle + fraction * sweepAngle
    }

    private fun rangeToAngle(value: Float) = ((value - dangerBefore) / (dangerAfter - dangerBefore)) * sweepAngle

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

        // 🔹 Draw arcs
        arcPaint.color = Color.RED
        canvas.drawArc(rectF, startAngle, rangeToAngle(safeStart), false, arcPaint)

        arcPaint.color = Color.GREEN
        canvas.drawArc(rectF, startAngle + rangeToAngle(safeStart),
            rangeToAngle(safeEnd) - rangeToAngle(safeStart), false, arcPaint)

        arcPaint.color = Color.RED
        canvas.drawArc(rectF, startAngle + rangeToAngle(safeEnd),
            rangeToAngle(dangerAfter) - rangeToAngle(safeEnd), false, arcPaint)

        // 🔹 Ticks from edge
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

        // 🔹 Start and end labels (full arc), smaller, closer, and slightly lower
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f // smaller than middle label
            textAlign = Paint.Align.CENTER
        }

        val labelRadius = radius + 40f // closer to arc than before
        val verticalOffset = 20f // move labels downward

        canvas.drawText(
            "${dangerBefore.toInt()}°",
            centerX + labelRadius * cos(Math.toRadians(startAngle.toDouble())).toFloat(),
            centerY + labelRadius * sin(Math.toRadians(startAngle.toDouble())).toFloat() + verticalOffset,
            labelPaint
        )
        canvas.drawText(
            "${dangerAfter.toInt()}°",
            centerX + labelRadius * cos(Math.toRadians((startAngle + sweepAngle).toDouble())).toFloat(),
            centerY + labelRadius * sin(Math.toRadians((startAngle + sweepAngle).toDouble())).toFloat() + verticalOffset,
            labelPaint
        )

        // 🔹 Middle value label slightly lower
        canvas.drawText("${"%.1f".format(currentValue)}°C", centerX, centerY + radius / 2f + 10f, textPaint)

        // 🔹 Needle
        val needleLength = radius
        val needleAngle = valueToAngle(currentValue)
        val needleX = centerX + needleLength * cos(Math.toRadians(needleAngle.toDouble())).toFloat()
        val needleY = centerY + needleLength * sin(Math.toRadians(needleAngle.toDouble())).toFloat()
        canvas.drawLine(centerX, centerY, needleX, needleY, needlePaint)

        // 🔹 Needle center circle with gradient
        val centerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9.33f, resources.displayMetrics)
        val gradient = RadialGradient(centerX, centerY, centerRadius,
            Color.parseColor("#333333"), Color.parseColor("#1F1F1F"), Shader.TileMode.CLAMP)
        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = gradient }
        canvas.drawCircle(centerX, centerY, centerRadius, centerPaint)
    }
}

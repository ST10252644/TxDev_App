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

class BatteryGaugeView @JvmOverloads constructor(
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

    private val needleWidthDp = 2.667f
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, needleWidthDp, resources.displayMetrics
        )
        color = Color.parseColor("#E48079")
        strokeCap = Paint.Cap.ROUND
    }

    // 🔹 Big center text (matches GaugeBackgroundView)
    private val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }

    // 🔹 Smaller tick labels (matches GaugeBackgroundView labels)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 4f
    }

    private val rectF = RectF()
    private val startValue = 6f
    private val endValue = 9f
    private var currentValue = 6f

    private val startAngle = 135f
    private val sweepAngle = 270f

    private val safeRanges = listOf(
        (6f to 6.5f) to Color.RED,
        (6.5f to 7.5f) to Color.parseColor("#FFA500"), // orange
        (7.5f to 8.5f) to Color.GREEN,
        (8.5f to 9f) to Color.RED
    )

    fun animateToValue(value: Float, duration: Long = 1000L) {
        val clamped = value.coerceIn(startValue, endValue)
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
        return startAngle + ((value - startValue) / (endValue - startValue)) * sweepAngle
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = width / 2f - TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, arcStrokeDp, resources.displayMetrics
        ) / 2

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // 🔹 Draw colored arcs
        for ((range, color) in safeRanges) {
            val start = range.first
            val end = range.second
            arcPaint.color = color
            val startAngleSegment = ((start - startValue) / (endValue - startValue)) * sweepAngle + startAngle
            val sweepSegment = ((end - start) / (endValue - startValue)) * sweepAngle
            canvas.drawArc(rectF, startAngleSegment, sweepSegment, false, arcPaint)
        }

        // 🔹 Draw ticks + labels (6,7,8,9)
        val ticks = listOf(6f, 7f, 8f, 9f)
        val outerRadius = radius + arcPaint.strokeWidth / 2
        val innerRadius = radius - arcPaint.strokeWidth / 2
        val labelRadius = radius + 40f

        for (tick in ticks) {
            val angle = Math.toRadians(valueToAngle(tick).toDouble())
            val startX = centerX + outerRadius * cos(angle).toFloat()
            val startY = centerY + outerRadius * sin(angle).toFloat()
            val endX = centerX + innerRadius * cos(angle).toFloat()
            val endY = centerY + innerRadius * sin(angle).toFloat()
            canvas.drawLine(startX, startY, endX, endY, tickPaint)

            // 🔹 Smaller labels (match GaugeBackgroundView)
            canvas.drawText(
                tick.toInt().toString(),
                centerX + labelRadius * cos(angle).toFloat(),
                centerY + labelRadius * sin(angle).toFloat() + 20f,
                labelPaint
            )
        }

        // 🔹 Big center value text
        canvas.drawText(
            String.format("%.1f V", currentValue),
            centerX,
            centerY + radius / 2f + 10f,
            centerTextPaint
        )

        // 🔹 Needle
        val needleLength = radius
        val needleAngle = valueToAngle(currentValue)
        val needleX = centerX + needleLength * cos(Math.toRadians(needleAngle.toDouble())).toFloat()
        val needleY = centerY + needleLength * sin(Math.toRadians(needleAngle.toDouble())).toFloat()
        canvas.drawLine(centerX, centerY, needleX, needleY, needlePaint)

        // 🔹 Needle center circle with gradient
        val centerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 9.33f, resources.displayMetrics)
        val gradient = RadialGradient(
            centerX, centerY, centerRadius,
            Color.parseColor("#333333"), Color.parseColor("#1F1F1F"), Shader.TileMode.CLAMP
        )
        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = gradient }
        canvas.drawCircle(centerX, centerY, centerRadius, centerPaint)
    }
}

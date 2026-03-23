package dk.codella.vantadot.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import dk.codella.vantadot.R
import kotlin.math.roundToInt

enum class CircleStyle { FILLED, HOLLOW, DASHED }

object GlanceText {

    fun renderLoadingDots(
        context: Context,
        activeIndex: Int,
        sizeDp: Float = 4f,
        spacingDp: Float = 3f,
        color: Int = android.graphics.Color.WHITE,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val dotSize = sizeDp * density
        val activeDotSize = dotSize * 1.6f
        val spacing = spacingDp * density
        val totalWidth = (dotSize * 2 + activeDotSize + spacing * 2).roundToInt()
        val totalHeight = activeDotSize.roundToInt()

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val centerY = totalHeight / 2f

        var x = 0f
        for (i in 0..2) {
            val isActive = i == activeIndex
            val radius = if (isActive) activeDotSize / 2f else dotSize / 2f
            val alpha = if (isActive) 255 else 120
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                this.alpha = alpha
            }
            x += radius
            canvas.drawCircle(x, centerY, radius, paint)
            x += radius + spacing
        }

        return bitmap
    }

    fun renderCircle(
        context: Context,
        sizeDp: Float,
        color: Int,
        style: CircleStyle = CircleStyle.FILLED,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).roundToInt()
        val strokeDp = 1.5f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            when (style) {
                CircleStyle.FILLED -> this.style = Paint.Style.FILL
                CircleStyle.HOLLOW -> {
                    this.style = Paint.Style.STROKE
                    this.strokeWidth = strokeDp * density
                }
                CircleStyle.DASHED -> {
                    this.style = Paint.Style.STROKE
                    this.strokeWidth = strokeDp * density
                    val dashPx = 2f * density
                    val gapPx = 2f * density
                    this.pathEffect = DashPathEffect(floatArrayOf(dashPx, gapPx), 0f)
                }
            }
        }

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radius = if (style == CircleStyle.FILLED) sizePx / 2f else (sizePx - strokeDp * density) / 2f
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, radius, paint)

        return bitmap
    }

    fun renderDotoText(
        context: Context,
        text: String,
        textSizeSp: Float = 24f,
        color: Int = android.graphics.Color.WHITE,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val scaledDensity = context.resources.displayMetrics.scaledDensity
        val textSizePx = textSizeSp * scaledDensity

        val typeface = ResourcesCompat.getFont(context, R.font.doto)
            ?: Typeface.MONOSPACE

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.textSize = textSizePx
            this.color = color
        }

        val textWidth = paint.measureText(text).roundToInt()
        val metrics = paint.fontMetrics
        val textHeight = (metrics.bottom - metrics.top).roundToInt()

        if (textWidth <= 0 || textHeight <= 0) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }

        val bitmap = Bitmap.createBitmap(textWidth, textHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, 0f, -metrics.top, paint)

        return bitmap
    }
}

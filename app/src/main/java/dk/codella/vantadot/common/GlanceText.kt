package dk.codella.vantadot.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import dk.codella.vantadot.R
import kotlin.math.roundToInt

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

    fun renderFilledCircle(
        context: Context,
        sizeDp: Float,
        color: Int,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).roundToInt()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.style = Paint.Style.FILL
        }

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)

        return bitmap
    }

    fun renderHollowCircle(
        context: Context,
        sizeDp: Float,
        color: Int,
        strokeDp: Float = 1.5f,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).roundToInt()
        val strokePx = strokeDp * density

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.style = Paint.Style.STROKE
            this.strokeWidth = strokePx
        }

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radius = (sizePx - strokePx) / 2f
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, radius, paint)

        return bitmap
    }

    fun renderDashedCircle(
        context: Context,
        sizeDp: Float,
        color: Int,
        strokeDp: Float = 1.5f,
        dashDp: Float = 2f,
        gapDp: Float = 2f,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).roundToInt()
        val strokePx = strokeDp * density
        val dashPx = dashDp * density
        val gapPx = gapDp * density

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.style = Paint.Style.STROKE
            this.strokeWidth = strokePx
            this.pathEffect = android.graphics.DashPathEffect(floatArrayOf(dashPx, gapPx), 0f)
        }

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val radius = (sizePx - strokePx) / 2f
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

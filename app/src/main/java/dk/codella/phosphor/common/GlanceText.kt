package dk.codella.phosphor.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import dk.codella.phosphor.R
import kotlin.math.roundToInt

object GlanceText {

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

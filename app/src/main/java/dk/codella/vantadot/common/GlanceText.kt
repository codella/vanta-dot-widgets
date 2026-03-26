package dk.codella.vantadot.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import dk.codella.vantadot.R
import kotlin.math.ceil
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

    fun renderFilledSquare(
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
        canvas.drawRect(0f, 0f, sizePx.toFloat(), sizePx.toFloat(), paint)

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
        maxWidthDp: Float? = null,
        maxLines: Int? = null,
    ): Bitmap {
        try {
            val density = context.resources.displayMetrics.density
            val scaledDensity = context.resources.displayMetrics.scaledDensity
            val textSizePx = textSizeSp * scaledDensity

            val typeface = ResourcesCompat.getFont(context, R.font.doto)
                ?: Typeface.MONOSPACE

            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = typeface
                this.textSize = textSizePx
                this.color = color
            }

            // Multi-line wrapping when maxWidthDp is provided
            if (maxWidthDp != null) {
                val maxWidthPx = (maxWidthDp * density).roundToInt().coerceAtLeast(1)
                val builder = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, maxWidthPx)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setIncludePad(false)
                if (maxLines != null) {
                    builder.setMaxLines(maxLines)
                    builder.setEllipsize(TextUtils.TruncateAt.END)
                }
                val layout = builder.build()

                // Use actual text width, not the constraint width — avoids oversized bitmaps
                // that can exceed RemoteViews Binder transaction limits with many events
                val actualWidth = (0 until layout.lineCount)
                    .maxOfOrNull { layout.getLineWidth(it) }
                    ?.let { ceil(it.toDouble()).toInt() }
                    ?.coerceAtLeast(1) ?: 1

                if (actualWidth <= 0 || layout.height <= 0) {
                    return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                }

                val bitmap = Bitmap.createBitmap(actualWidth, layout.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                layout.draw(canvas)
                return bitmap
            }

            // Single-line (no max width) — headers, messages, etc.
            val textWidth = textPaint.measureText(text).roundToInt()
            val metrics = textPaint.fontMetrics
            val textHeight = (metrics.bottom - metrics.top).roundToInt()

            if (textWidth <= 0 || textHeight <= 0) {
                return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }

            val bitmap = Bitmap.createBitmap(textWidth, textHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawText(text, 0f, -metrics.top, textPaint)

            return bitmap
        } catch (e: Throwable) {
            Log.e("GlanceText", "renderDotoText failed for '${text.take(50)}'", e)
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
    }
}

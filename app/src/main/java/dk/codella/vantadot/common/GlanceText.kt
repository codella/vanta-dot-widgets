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

    fun renderProgressBar(
        context: Context,
        widthDp: Float,
        heightDp: Float = 6f,
        progress: Float,
        filledColor: Int = android.graphics.Color.WHITE,
        emptyColor: Int = VantaDotWidgetTheme.GreyMediumArgb,
        dotSizeDp: Float = 4f,
        gapDp: Float = 2f,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val totalWidthPx = (widthDp * density).roundToInt().coerceAtLeast(1)
        val totalHeightPx = (heightDp * density).roundToInt().coerceAtLeast(1)
        val dotSizePx = dotSizeDp * density
        val gapPx = gapDp * density
        val step = dotSizePx + gapPx
        val dotCount = ((totalWidthPx + gapPx) / step).toInt().coerceAtLeast(1)
        val filledCount = (dotCount * progress.coerceIn(0f, 1f)).roundToInt()

        val bitmap = Bitmap.createBitmap(totalWidthPx, totalHeightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val centerY = totalHeightPx / 2f

        for (i in 0 until dotCount) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (i < filledCount) filledColor else emptyColor
                style = Paint.Style.FILL
            }
            val x = i * step
            canvas.drawRect(x, centerY - dotSizePx / 2, x + dotSizePx, centerY + dotSizePx / 2, paint)
        }
        return bitmap
    }

    fun renderChevron(
        context: Context,
        sizeDp: Float,
        color: Int,
        pointLeft: Boolean,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).roundToInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 2f * density
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val margin = sizePx * 0.25f
        val path = android.graphics.Path()
        if (pointLeft) {
            path.moveTo(sizePx - margin, margin)
            path.lineTo(margin, sizePx / 2f)
            path.lineTo(sizePx - margin, sizePx - margin)
        } else {
            path.moveTo(margin, margin)
            path.lineTo(sizePx - margin, sizePx / 2f)
            path.lineTo(margin, sizePx - margin)
        }
        canvas.drawPath(path, paint)
        return bitmap
    }

    fun renderBinaryClockFace(
        context: Context,
        hours: Int,
        minutes: Int,
        seconds: Int,
        showSeconds: Boolean,
        showBitLabels: Boolean,
        showColumnLabels: Boolean,
        dotShape: Int,
        onColor: Int,
        offColor: Int,
        labelColor: Int,
        dotSizeDp: Float = 10f,
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val scaledDensity = context.resources.displayMetrics.scaledDensity

        val dotSizePx = dotSizeDp * density
        val dotSpacingPx = dotSizePx * 0.5f
        val groupSpacingPx = dotSizePx * 1.0f
        val rowSpacingPx = dotSizePx * 0.5f

        val labelSizePx = dotSizeDp * 0.8f * scaledDensity
        val typeface = ResourcesCompat.getFont(context, R.font.doto) ?: Typeface.MONOSPACE
        val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.textSize = labelSizePx
            this.color = labelColor
        }

        val digits = mutableListOf(hours / 10, hours % 10, minutes / 10, minutes % 10)
        if (showSeconds) {
            digits.add(seconds / 10)
            digits.add(seconds % 10)
        }
        val numCols = digits.size
        val numGroups = numCols / 2

        val bitLabelWidth = if (showBitLabels) {
            labelPaint.measureText("8") + dotSpacingPx
        } else 0f

        val colLabelHeight = if (showColumnLabels) {
            val fm = labelPaint.fontMetrics
            (fm.bottom - fm.top) + rowSpacingPx * 0.5f
        } else 0f

        // Precompute column X positions
        val colX = FloatArray(numCols)
        var x = bitLabelWidth
        for (col in 0 until numCols) {
            if (col > 0) {
                x += if (col % 2 == 0) groupSpacingPx else dotSpacingPx
            }
            colX[col] = x
            x += dotSizePx
        }
        val totalWidth = ceil(x.toDouble()).toInt().coerceAtLeast(1)

        // Precompute row Y positions
        val rowY = FloatArray(4)
        for (row in 0 until 4) {
            rowY[row] = colLabelHeight + row * (dotSizePx + rowSpacingPx)
        }
        val totalHeight = ceil((rowY[3] + dotSizePx).toDouble()).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bitValues = intArrayOf(8, 4, 2, 1)

        // Column labels (H H M M S S)
        if (showColumnLabels) {
            val labels = if (showSeconds) listOf("H", "H", "M", "M", "S", "S")
                         else listOf("H", "H", "M", "M")
            for (col in 0 until numCols) {
                val lbl = labels[col]
                val lblWidth = labelPaint.measureText(lbl)
                canvas.drawText(
                    lbl,
                    colX[col] + (dotSizePx - lblWidth) / 2,
                    -labelPaint.fontMetrics.top,
                    labelPaint,
                )
            }
        }

        // Bit labels (8 4 2 1)
        if (showBitLabels) {
            for (row in 0 until 4) {
                val lbl = bitValues[row].toString()
                val lblWidth = labelPaint.measureText(lbl)
                val labelX = (bitLabelWidth - dotSpacingPx - lblWidth) / 2
                val labelY = rowY[row] + dotSizePx / 2 -
                    (labelPaint.fontMetrics.top + labelPaint.fontMetrics.bottom) / 2
                canvas.drawText(lbl, labelX, labelY, labelPaint)
            }
        }

        // Dots
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val strokeWidth = 1.5f * density

        for (row in 0 until 4) {
            val bitValue = bitValues[row]
            for (col in 0 until numCols) {
                val isOn = (digits[col] and bitValue) != 0
                val cx = colX[col] + dotSizePx / 2
                val cy = rowY[row] + dotSizePx / 2

                dotPaint.color = if (isOn) onColor else offColor

                when (dotShape) {
                    0 -> { // Circle
                        if (isOn) {
                            dotPaint.style = Paint.Style.FILL
                            canvas.drawCircle(cx, cy, dotSizePx / 2, dotPaint)
                        } else {
                            dotPaint.style = Paint.Style.STROKE
                            dotPaint.strokeWidth = strokeWidth
                            canvas.drawCircle(cx, cy, (dotSizePx - strokeWidth) / 2, dotPaint)
                        }
                    }
                    1 -> { // Square
                        if (isOn) {
                            dotPaint.style = Paint.Style.FILL
                            canvas.drawRect(
                                colX[col], rowY[row],
                                colX[col] + dotSizePx, rowY[row] + dotSizePx,
                                dotPaint,
                            )
                        } else {
                            dotPaint.style = Paint.Style.STROKE
                            dotPaint.strokeWidth = strokeWidth
                            val half = strokeWidth / 2
                            canvas.drawRect(
                                colX[col] + half, rowY[row] + half,
                                colX[col] + dotSizePx - half, rowY[row] + dotSizePx - half,
                                dotPaint,
                            )
                        }
                    }
                    else -> { // Diamond
                        val path = android.graphics.Path().apply {
                            moveTo(cx, rowY[row])
                            lineTo(colX[col] + dotSizePx, cy)
                            lineTo(cx, rowY[row] + dotSizePx)
                            lineTo(colX[col], cy)
                            close()
                        }
                        if (isOn) {
                            dotPaint.style = Paint.Style.FILL
                        } else {
                            dotPaint.style = Paint.Style.STROKE
                            dotPaint.strokeWidth = strokeWidth
                        }
                        canvas.drawPath(path, dotPaint)
                    }
                }
            }
        }

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

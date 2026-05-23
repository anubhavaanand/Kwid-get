package com.example.parser

import android.content.Context
import android.graphics.*
import com.example.model.LayerType
import com.example.model.ProgressStyle
import com.example.model.ShapeType
import com.example.model.WidgetConfig
import com.example.model.WidgetLayer
import java.util.Locale

object WidgetBitmapRenderer {

    private fun parseColorToAndroid(hexStr: String, fallbackColor: Int = Color.WHITE): Int {
        return try {
            val cleaned = hexStr.trim().replace("#", "")
            when (cleaned.length) {
                6 -> Color.parseColor("#FF$cleaned")
                8 -> Color.parseColor("#$cleaned")
                else -> fallbackColor
            }
        } catch (e: Exception) {
            fallbackColor
        }
    }

    /**
     * Renders a [WidgetConfig] into a high-fidelity [Bitmap].
     */
    fun renderToBitmap(context: Context, config: WidgetConfig, desiredWidth: Int, desiredHeight: Int): Bitmap {
        val width = if (desiredWidth <= 0) 800 else desiredWidth
        val height = if (desiredHeight <= 0) 400 else desiredHeight
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw Widget Background
        val bgColor = parseColorToAndroid(config.backgroundColor, Color.DKGRAY)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }

        val widgetRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val radiusMultiplier = width / 400f // Scaling factor for sizing and thickness
        val borderRadiusPx = config.borderRadius * radiusMultiplier
        canvas.drawRoundRect(widgetRect, borderRadiusPx, borderRadiusPx, bgPaint)

        // Draw Widget Border if any
        if (config.borderStrokeWidth > 0f) {
            val strokeColor = parseColorToAndroid(config.borderStrokeColor, Color.TRANSPARENT)
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = strokeColor
                style = Paint.Style.STROKE
                strokeWidth = config.borderStrokeWidth * radiusMultiplier
            }
            // Deflate slightly to keep stroke within bounds
            val inset = (config.borderStrokeWidth * radiusMultiplier) / 2f
            val borderRect = RectF(inset, inset, width - inset, height - inset)
            canvas.drawRoundRect(borderRect, borderRadiusPx, borderRadiusPx, strokePaint)
        }

        val scale = width / 400f // Core layer coordinate scaling multiplier
        val centerX = width / 2f
        val centerY = height / 2f

        // Draw layers sequentially
        for (layer in config.layers) {
            if (!layer.visible) continue

            canvas.save()
            // Translate layout relative to the center, just like KWGT coordinates
            canvas.translate(centerX + (layer.offsetX * scale), centerY + (layer.offsetY * scale))
            canvas.rotate(layer.rotation)

            when (layer.type) {
                LayerType.TEXT -> {
                    val evaluatedText = KustomFormulaParser.evaluate(context, layer.textValue)
                    val textColorVal = parseColorToAndroid(layer.textColor, Color.WHITE)
                    
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = textColorVal
                        textSize = layer.textSize * scale
                        textAlign = when (layer.textAlignment.uppercase(Locale.getDefault())) {
                            "LEFT" -> Paint.Align.LEFT
                            "RIGHT" -> Paint.Align.RIGHT
                            else -> Paint.Align.CENTER
                        }
                        
                        val isBold = layer.isBold
                        val isItalic = layer.isItalic
                        typeface = Typeface.create(
                            Typeface.SANS_SERIF,
                            if (isBold && isItalic) Typeface.BOLD_ITALIC
                            else if (isBold) Typeface.BOLD
                            else if (isItalic) Typeface.ITALIC
                            else Typeface.NORMAL
                        )
                    }

                    // Vertically center text
                    val bounds = Rect()
                    textPaint.getTextBounds(evaluatedText, 0, evaluatedText.length, bounds)
                    val textHeightOffset = bounds.height() / 2f
                    
                    canvas.drawText(evaluatedText, 0f, textHeightOffset, textPaint)
                }

                LayerType.SHAPE -> {
                    val shapeColorVal = parseColorToAndroid(layer.shapeColor, Color.GREEN)
                    val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = shapeColorVal
                        style = Paint.Style.FILL
                    }

                    val shapeW = layer.shapeWidth * scale
                    val shapeH = layer.shapeHeight * scale

                    // Draw fill shape
                    when (layer.shapeType) {
                        ShapeType.CIRCLE -> {
                            val r = shapeW / 2f
                            canvas.drawCircle(0f, 0f, r, shapePaint)
                        }
                        ShapeType.ROUNDED_RECTANGLE -> {
                            val r = layer.shapeCornerRadius * scale
                            val rect = RectF(-shapeW / 2f, -shapeH / 2f, shapeW / 2f, shapeH / 2f)
                            canvas.drawRoundRect(rect, r, r, shapePaint)
                        }
                        ShapeType.RECTANGLE -> {
                            val rect = RectF(-shapeW / 2f, -shapeH / 2f, shapeW / 2f, shapeH / 2f)
                            canvas.drawRect(rect, shapePaint)
                        }
                    }

                    // Draw stroke line if configured
                    if (layer.shapeStrokeWidth > 0f) {
                        val strokeColorVal = parseColorToAndroid(layer.shapeStrokeColor, Color.WHITE)
                        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = strokeColorVal
                            style = Paint.Style.STROKE
                            strokeWidth = layer.shapeStrokeWidth * scale
                        }
                        when (layer.shapeType) {
                            ShapeType.CIRCLE -> {
                                val r = (shapeW / 2f) - (layer.shapeStrokeWidth * scale / 2f)
                                canvas.drawCircle(0f, 0f, r, strokePaint)
                            }
                            ShapeType.ROUNDED_RECTANGLE -> {
                                val r = layer.shapeCornerRadius * scale
                                val inset = (layer.shapeStrokeWidth * scale) / 2f
                                val rect = RectF((-shapeW / 2f) + inset, (-shapeH / 2f) + inset, (shapeW / 2f) - inset, (shapeH / 2f) - inset)
                                canvas.drawRoundRect(rect, r, r, strokePaint)
                            }
                            ShapeType.RECTANGLE -> {
                                val inset = (layer.shapeStrokeWidth * scale) / 2f
                                val rect = RectF((-shapeW / 2f) + inset, (-shapeH / 2f) + inset, (shapeW / 2f) - inset, (shapeH / 2f) - inset)
                                canvas.drawRect(rect, strokePaint)
                            }
                        }
                    }
                }

                LayerType.ICON -> {
                    val iconColorVal = parseColorToAndroid(layer.iconColor, Color.WHITE)
                    val iconSizePx = layer.iconSize * scale
                    drawCustomVectorIcon(canvas, layer.iconName, iconSizePx, iconColorVal)
                }

                LayerType.PROGRESS -> {
                    val evaluatedFormula = KustomFormulaParser.evaluate(context, layer.progressValueFormula)
                    val parsedVal = evaluatedFormula.trim().replace("%", "").toFloatOrNull() ?: 50f
                    val progressFraction = (parsedVal / 100f).coerceIn(0f, 1f)
                    
                    val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = parseColorToAndroid(layer.progressTrackColor, Color.parseColor("#33FFFFFF"))
                    }
                    val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = parseColorToAndroid(layer.progressColor, Color.CYAN)
                    }

                    val pW = layer.progressWidth * scale
                    val pH = layer.progressHeight * scale

                    if (layer.progressStyle == ProgressStyle.CIRCULAR) {
                        val strokeW = pH * 0.4f
                        val radius = (pH * 2f) - (strokeW / 2f)
                        
                        trackPaint.style = Paint.Style.STROKE
                        trackPaint.strokeWidth = strokeW
                        
                        progressPaint.style = Paint.Style.STROKE
                        progressPaint.strokeWidth = strokeW
                        progressPaint.strokeCap = Paint.Cap.ROUND
                        
                        val arcBounds = RectF(-radius, -radius, radius, radius)
                        // Draw full track circle
                        canvas.drawCircle(0f, 0f, radius, trackPaint)
                        // Draw sweep progress arch
                        canvas.drawArc(arcBounds, -90f, progressFraction * 360f, false, progressPaint)
                    } else {
                        // Horizontal progress bar
                        trackPaint.style = Paint.Style.FILL
                        progressPaint.style = Paint.Style.FILL
                        
                        val r = pH / 2f
                        val trackRect = RectF(-pW / 2f, -pH / 2f, pW / 2f, pH / 2f)
                        canvas.drawRoundRect(trackRect, r, r, trackPaint)
                        
                        if (progressFraction > 0.02f) {
                            val activeWidth = pW * progressFraction
                            val progressRect = RectF(-pW / 2f, -pH / 2f, (-pW / 2f) + activeWidth, pH / 2f)
                            canvas.drawRoundRect(progressRect, r, r, progressPaint)
                        }
                    }
                }
            }
            canvas.restore()
        }

        return bitmap
    }

    /**
     * Hand-crafts iconic, clean visual geometric vector shapes onto the Canvas based on iconic keys.
     * This ensures superb, premium custom visual appearance without relying on raw XML resources.
     */
    private fun drawCustomVectorIcon(canvas: Canvas, iconKey: String, size: Float, colorInt: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            style = Paint.Style.STROKE
            strokeWidth = size * 0.1f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            style = Paint.Style.FILL
        }

        val h = size / 2f

        when (iconKey.lowercase().trim()) {
            "schedule", "time", "clock" -> {
                // Draw clock outline circle
                canvas.drawCircle(0f, 0f, h * 0.9f, paint)
                // Draw clock hour/minute indicator lines
                canvas.drawLine(0f, 0f, 0f, -h * 0.5f, paint) // hour hand pointing up
                canvas.drawLine(0f, 0f, h * 0.4f, 0f, paint) // minute hand pointing right
            }
            "wb_sunny", "sun", "sunny" -> {
                // Sun center circle
                canvas.drawCircle(0f, 0f, h * 0.45f, fillPaint)
                // Draw rays
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45).toDouble())
                    val startX = (Math.cos(angle) * (h * 0.6f)).toFloat()
                    val startY = (Math.sin(angle) * (h * 0.6f)).toFloat()
                    val endX = (Math.cos(angle) * (h * 0.85f)).toFloat()
                    val endY = (Math.sin(angle) * (h * 0.85f)).toFloat()
                    canvas.drawLine(startX, startY, endX, endY, paint)
                }
            }
            "battery_std", "battery" -> {
                // Shell of standard battery symbol rotated horizontally/vertically
                paint.style = Paint.Style.STROKE
                val bW = h * 0.55f
                val bH = h * 1.1f
                val body = RectF(-bW, -bH, bW, bH * 0.85f)
                canvas.drawRoundRect(body, h * 0.12f, h * 0.12f, paint)
                // Cap of battery
                val cap = RectF(-bW * 0.4f, bH * 0.85f, bW * 0.4f, bH)
                canvas.drawRect(cap, fillPaint)
                // Infill level representation indicator
                val infill = RectF(-bW * 0.7f, -bH * 0.75f, bW * 0.7f, bH * 0.6f)
                canvas.drawRect(infill, fillPaint)
            }
            "bolt", "charge", "electricity", "lightning" -> {
                // A modern geometric lightning bolt lightning path
                fillPaint.style = Paint.Style.FILL
                val path = Path().apply {
                    moveTo(0f, -h * 1.0f)
                    lineTo(h * 0.5f, -h * 0.2f)
                    lineTo(h * 0.15f, -h * 0.2f)
                    lineTo(h * 0.4f, h * 1.0f)
                    lineTo(-h * 0.5f, h * 0.2f)
                    lineTo(-h * 0.15f, h * 0.2f)
                    close()
                }
                canvas.drawPath(path, fillPaint)
            }
            "cloud", "weather", "cloudy" -> {
                fillPaint.style = Paint.Style.FILL
                // Multi-overlapping bubble path representing a cloud
                canvas.drawCircle(-h * 0.3f, h * 0.2f, h * 0.45f, fillPaint) // left base bubble
                canvas.drawCircle(h * 0.3f, h * 0.2f, h * 0.45f, fillPaint)  // right base bubble
                canvas.drawCircle(0f, -h * 0.15f, h * 0.55f, fillPaint)     // tall upper bubble
                canvas.drawRect(-h * 0.35f, h * 0.35f, h * 0.35f, h * 0.65f, fillPaint) // fill bottom gap
            }
            "music_note", "music", "song" -> {
                // Traditional double musical eighth note representation
                canvas.drawCircle(-h * 0.4f, h * 0.4f, h * 0.22f, fillPaint) // left note node
                canvas.drawCircle(h * 0.2f, h * 0.25f, h * 0.22f, fillPaint)  // right note node
                paint.apply {
                    style = Paint.Style.STROKE
                    strokeWidth = size * 0.08f
                }
                // Draw note sticks pointing upwards
                canvas.drawLine(-h * 0.2f, h * 0.4f, -h * 0.2f, -h * 0.5f, paint)
                canvas.drawLine(h * 0.4f, h * 0.25f, h * 0.4f, -h * 0.65f, paint)
                // Connecting beam roof flag
                canvas.drawLine(-h * 0.2f, -h * 0.5f, h * 0.4f, -h * 0.65f, paint)
            }
            "favorite", "heart", "love" -> {
                // Heart path
                fillPaint.style = Paint.Style.FILL
                val path = Path().apply {
                    moveTo(0f, h * 0.35f)
                    cubicTo(-h * 0.8f, -h * 0.4f, -h * 0.8f, -h * 0.9f, 0f, -h * 0.25f)
                    moveTo(0f, h * 0.35f)
                    cubicTo(h * 0.8f, -h * 0.4f, h * 0.8f, -h * 0.9f, 0f, -h * 0.25f)
                    close()
                }
                canvas.drawCircle(-h * 0.35f, -h * 0.25f, h * 0.35f, fillPaint)
                canvas.drawCircle(h * 0.35f, -h * 0.25f, h * 0.35f, fillPaint)
                val triangle = Path().apply {
                    moveTo(-h * 0.7f, -h * 0.15f)
                    lineTo(h * 0.7f, -h * 0.15f)
                    lineTo(0f, h * 0.8f)
                    close()
                }
                canvas.drawPath(triangle, fillPaint)
            }
            "android" -> {
                // Draw Android robot head outline and antennae dome representation
                val rect = RectF(-h * 0.65f, -h * 0.5f, h * 0.65f, h * 0.7f)
                canvas.drawArc(rect, 180f, 180f, true, fillPaint) // head dome
                canvas.drawRect(-h * 0.65f, -h * 0.05f, h * 0.65f, h * 0.15f, fillPaint) // buffer
                // Antenna left and right
                paint.strokeWidth = size * 0.07f
                canvas.drawLine(-h * 0.35f, -h * 0.45f, -h * 0.55f, -h * 0.85f, paint)
                canvas.drawLine(h * 0.35f, -h * 0.45f, h * 0.55f, -h * 0.85f, paint)
                // Eyes cut-out
                val oldCol = paint.color
                paint.apply {
                    color = Color.BLACK
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(-h * 0.25f, -h * 0.25f, h * 0.07f, paint)
                canvas.drawCircle(h * 0.25f, -h * 0.25f, h * 0.07f, paint)
                paint.color = oldCol
            }
            "person", "user", "avatar" -> {
                // Head node
                canvas.drawCircle(0f, -h * 0.3f, h * 0.32f, fillPaint)
                // Shoulders arc
                val bodyBounds = RectF(-h * 0.75f, h * 0.1f, h * 0.75f, h * 1.1f)
                canvas.drawArc(bodyBounds, 180f, 180f, true, fillPaint)
            }
            "star", "rating" -> {
                val starPath = Path()
                starPath.moveTo(0f, -h)
                for (i in 0..4) {
                    val angle1 = Math.toRadians((18 + i * 72).toDouble())
                    val angleInner = Math.toRadians((54 + i * 72).toDouble())
                    val innerR = h * 0.4
                    val outerR = h.toDouble()
                    
                    starPath.lineTo((Math.cos(angleInner) * innerR).toFloat(), (Math.sin(angleInner) * innerR).toFloat())
                    starPath.lineTo((Math.cos(angle1) * outerR).toFloat(), (Math.sin(angle1) * outerR).toFloat())
                }
                starPath.close()
                canvas.drawPath(starPath, fillPaint)
            }
            else -> {
                // Aesthetic generic gear or focal indicator fallback icon
                canvas.drawCircle(0f, 0f, h * 0.75f, paint)
                canvas.drawCircle(0f, 0f, h * 0.25f, fillPaint)
            }
        }
    }
}

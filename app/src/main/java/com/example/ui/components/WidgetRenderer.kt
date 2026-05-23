package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LayerType
import com.example.model.ProgressStyle
import com.example.model.ShapeType
import com.example.model.WidgetConfig
import com.example.model.WidgetLayer
import com.example.parser.KustomFormulaParser

fun parseHexColor(hexStr: String, fallback: Color = Color.White): Color {
    return try {
        val cleaned = hexStr.trim().replace("#", "")
        when (cleaned.length) {
            6 -> Color(android.graphics.Color.parseColor("#FF$cleaned"))
            8 -> Color(android.graphics.Color.parseColor("#$cleaned"))
            else -> fallback
        }
    } catch (e: Exception) {
        fallback
    }
}

fun getIconByName(name: String): ImageVector {
    return when (name.lowercase().trim()) {
        "schedule", "time", "clock" -> Icons.Default.Schedule
        "wb_sunny", "sun", "sunny" -> Icons.Default.WbSunny
        "battery_std", "battery", "battery_std" -> Icons.Default.BatteryStd
        "bolt", "electricity", "charge", "lightning" -> Icons.Default.Bolt
        "cloud", "cloudy", "weather" -> Icons.Default.Cloud
        "music_note", "music", "song" -> Icons.Default.MusicNote
        "favorite", "heart", "love" -> Icons.Default.Favorite
        "star", "rating" -> Icons.Default.Star
        "android", "phone" -> Icons.Default.Android
        "person", "user", "avatar" -> Icons.Default.Person
        "notifications", "bell" -> Icons.Default.Notifications
        "wifi", "internet" -> Icons.Default.Wifi
        "thermostat", "temp" -> Icons.Default.Thermostat
        else -> Icons.Default.Extension
    }
}

@Composable
fun WidgetRenderer(
    config: WidgetConfig,
    modifier: Modifier = Modifier,
    scale: Float = 1.0f // Let us scale the entire widget relative for editor preview
) {
    val context = LocalContext.current
    val widgetBgColor = remember(config.backgroundColor) { parseHexColor(config.backgroundColor, Color.DarkGray) }
    val borderStrokeColor = remember(config.borderStrokeColor) { parseHexColor(config.borderStrokeColor, Color.Transparent) }

    val safeHeight = if (config.heightRatio > 0) config.heightRatio.toFloat() else 1f
    val safeWidth = if (config.widthRatio > 0) config.widthRatio.toFloat() else 1f
    Box(
        modifier = modifier
            .aspectRatio(safeWidth / safeHeight)
            .clip(RoundedCornerShape((config.borderRadius * scale).dp))
            .background(widgetBgColor)
            .then(
                if (config.borderStrokeWidth > 0f) {
                    Modifier.border(
                        (config.borderStrokeWidth * scale).dp,
                        borderStrokeColor,
                        RoundedCornerShape((config.borderRadius * scale).dp)
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Render every visible layer relative to the center of the widget container
        config.layers.filter { it.visible }.forEach { layer ->
            Box(
                modifier = Modifier
                    .offset(
                        x = (layer.offsetX * scale).dp,
                        y = (layer.offsetY * scale).dp
                    )
                    .rotate(layer.rotation),
                contentAlignment = Alignment.Center
            ) {
                when (layer.type) {
                    LayerType.TEXT -> {
                        val evaluatedText = KustomFormulaParser.evaluate(context, layer.textValue)
                        val textColor = parseHexColor(layer.textColor)
                        
                        Text(
                            text = evaluatedText,
                            color = textColor,
                            fontSize = (layer.textSize * scale).sp,
                            fontWeight = if (layer.isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (layer.isItalic) FontStyle.Italic else FontStyle.Normal,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = when (layer.textAlignment.uppercase()) {
                                "LEFT" -> TextAlign.Left
                                "RIGHT" -> TextAlign.Right
                                else -> TextAlign.Center
                            }
                        )
                    }
                    
                    LayerType.SHAPE -> {
                        val shapeColor = parseHexColor(layer.shapeColor)
                        val strokeColor = parseHexColor(layer.shapeStrokeColor, Color.Transparent)
                        
                        Box(
                            modifier = Modifier
                                .size(
                                    width = (layer.shapeWidth * scale).dp,
                                    height = (layer.shapeHeight * scale).dp
                                )
                                .then(
                                    if (layer.shapeStrokeWidth > 0) {
                                        Modifier.border(
                                            (layer.shapeStrokeWidth * scale).dp,
                                            strokeColor,
                                            when (layer.shapeType) {
                                                ShapeType.CIRCLE -> CircleShape
                                                ShapeType.ROUNDED_RECTANGLE -> RoundedCornerShape((layer.shapeCornerRadius * scale).dp)
                                                else -> RoundedCornerShape(0.dp)
                                            }
                                        )
                                    } else Modifier
                                )
                                .background(
                                    color = shapeColor,
                                    shape = when (layer.shapeType) {
                                        ShapeType.CIRCLE -> CircleShape
                                        ShapeType.ROUNDED_RECTANGLE -> RoundedCornerShape((layer.shapeCornerRadius * scale).dp)
                                        else -> RoundedCornerShape(0.dp)
                                    }
                                )
                        )
                    }
                    
                    LayerType.ICON -> {
                        val iconColor = parseHexColor(layer.iconColor)
                        val iconVector = getIconByName(layer.iconName)
                        
                        Icon(
                            imageVector = iconVector,
                            contentDescription = layer.name,
                            tint = iconColor,
                            modifier = Modifier.size((layer.iconSize * scale).dp)
                        )
                    }
                    
                    LayerType.PROGRESS -> {
                        val evaluatedFormula = KustomFormulaParser.evaluate(context, layer.progressValueFormula)
                        // Sanitize string mapping to numeric, e.g. "83" or "83%" -> 0.83f
                        val parsedVal = evaluatedFormula.trim().replace("%", "").toFloatOrNull() ?: 50f
                        val progressFraction = (parsedVal / 100f).coerceIn(0f, 1f)
                        
                        val trackColor = parseHexColor(layer.progressTrackColor, Color.Gray.copy(alpha = 0.3f))
                        val progressColor = parseHexColor(layer.progressColor, Color.Cyan)
                        
                        if (layer.progressStyle == ProgressStyle.CIRCULAR) {
                            CircularProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier.size((layer.progressHeight * 4f * scale).dp), // circular progress scale
                                color = progressColor,
                                strokeWidth = (layer.progressHeight * 0.3f * scale).dp,
                                trackColor = trackColor
                            )
                        } else {
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .size(
                                        width = (layer.progressWidth * scale).dp,
                                        height = (layer.progressHeight * scale).dp
                                    )
                                    .clip(RoundedCornerShape((layer.progressHeight * 0.5f * scale).dp)),
                                color = progressColor,
                                trackColor = trackColor
                            )
                        }
                    }
                }
            }
        }
    }
}

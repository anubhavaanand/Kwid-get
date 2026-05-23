package com.example.model

import com.squareup.moshi.JsonClass

enum class LayerType {
    TEXT,
    SHAPE,
    ICON,
    PROGRESS
}

enum class ShapeType {
    RECTANGLE,
    CIRCLE,
    ROUNDED_RECTANGLE
}

enum class ProgressStyle {
    HORIZONTAL,
    CIRCULAR
}

@JsonClass(generateAdapter = true)
data class WidgetLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "New Layer",
    val type: LayerType = LayerType.TEXT,
    
    // Transform
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val rotation: Float = 0f,
    val visible: Boolean = true,
    
    // Text Layer Specific
    val textValue: String = "My Text", // Plain text or e.g. "$df(hh:mm)$"
    val textColor: String = "#FFFFFFFF",
    val textSize: Float = 16f,
    val textAlignment: String = "CENTER", // LEFT, CENTER, RIGHT
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    
    // Shape Layer Specific
    val shapeType: ShapeType = ShapeType.ROUNDED_RECTANGLE,
    val shapeWidth: Float = 120f,
    val shapeHeight: Float = 60f,
    val shapeColor: String = "#FF6200EE",
    val shapeCornerRadius: Float = 12f,
    val shapeStrokeWidth: Float = 0f,
    val shapeStrokeColor: String = "#FFFFFFFF",
    
    // Icon Layer Specific
    val iconName: String = "schedule", // e.g. "schedule", "wb_sunny", "battery_std"
    val iconSize: Float = 24f,
    val iconColor: String = "#FFFFFFFF",
    
    // Progress Layer Specific
    val progressValueFormula: String = "\$bi(level)\$", // Kustom formula
    val progressStyle: ProgressStyle = ProgressStyle.HORIZONTAL,
    val progressTrackColor: String = "#33FFFFFF",
    val progressColor: String = "#FF03DAC6",
    val progressWidth: Float = 150f,
    val progressHeight: Float = 10f
)

@JsonClass(generateAdapter = true)
data class WidgetConfig(
    val id: Int = 0,
    val name: String = "Custom Widget",
    val description: String = "My beautiful homescreen widget",
    val author: String = "KWGT Free User",
    val widthRatio: Int = 4, // standard 4x2
    val heightRatio: Int = 2,
    val backgroundColor: String = "#FF121212",
    val borderRadius: Float = 16f,
    val borderStrokeWidth: Float = 0f,
    val borderStrokeColor: String = "#00000000",
    val layers: List<WidgetLayer> = emptyList()
)

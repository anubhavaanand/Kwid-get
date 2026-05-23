package com.example.model

object PresetWidgets {
    val presets = listOf(
        WidgetConfig(
            id = -1, // Convention for non-saved built-ins
            name = "Material Minimal",
            description = "Elegant pixel-like clock displaying live dates, system battery status, and ambient weather sync.",
            author = "KWGT Free Team",
            widthRatio = 4,
            heightRatio = 2,
            backgroundColor = "#FF121214",
            borderRadius = 22f,
            borderStrokeWidth = 1f,
            borderStrokeColor = "#33FFFFFF",
            layers = listOf(
                // Background Highlight Card
                WidgetLayer(
                    name = "Time Card Accent",
                    type = LayerType.SHAPE,
                    offsetX = -65f,
                    offsetY = 0f,
                    shapeType = ShapeType.ROUNDED_RECTANGLE,
                    shapeWidth = 160f,
                    shapeHeight = 110f,
                    shapeColor = "#1AFFFFFF",
                    shapeCornerRadius = 14f
                ),
                // Clock Time
                WidgetLayer(
                    name = "Clock Time",
                    type = LayerType.TEXT,
                    offsetX = -65f,
                    offsetY = -10f,
                    textValue = "\$df(hh:mm)\$",
                    textColor = "#FFFFFFFF",
                    textSize = 38f,
                    isBold = true
                ),
                // AM/PM marker
                WidgetLayer(
                    name = "AM/PM",
                    type = LayerType.TEXT,
                    offsetX = -10f,
                    offsetY = -35f,
                    textValue = "\$df(a)\$",
                    textColor = "#FF80CBC4",
                    textSize = 10f,
                    isBold = true
                ),
                // Live Date
                WidgetLayer(
                    name = "Date String",
                    type = LayerType.TEXT,
                    offsetX = -65f,
                    offsetY = 25f,
                    textValue = "\$df(EEE, d MMM)\$",
                    textColor = "#FFB0BEC5",
                    textSize = 12f,
                    textAlignment = "CENTER"
                ),
                // Weather icon column on the right
                WidgetLayer(
                    name = "Weather Icon",
                    type = LayerType.ICON,
                    offsetX = 65f,
                    offsetY = -30f,
                    iconName = "wb_sunny",
                    iconColor = "#FFFFD54F",
                    iconSize = 32f
                ),
                WidgetLayer(
                    name = "Weather Temp",
                    type = LayerType.TEXT,
                    offsetX = 105f,
                    offsetY = -30f,
                    textValue = "\$wi(temp)\$",
                    textColor = "#FFFFFFFF",
                    textSize = 16f,
                    isBold = true
                ),
                // Battery metric
                WidgetLayer(
                    name = "Battery Label",
                    type = LayerType.TEXT,
                    offsetX = 65f,
                    offsetY = 10f,
                    textValue = "BATTERY \$bi(level)\$%",
                    textColor = "#FF80CBC4",
                    textSize = 11f,
                    isBold = true
                ),
                // Battery progress track
                WidgetLayer(
                    name = "Battery Progress",
                    type = LayerType.PROGRESS,
                    offsetX = 78f,
                    offsetY = 32f,
                    progressStyle = ProgressStyle.HORIZONTAL,
                    progressValueFormula = "\$bi(level)\$",
                    progressColor = "#FF80CBC4",
                    progressTrackColor = "#22FFFFFF",
                    progressWidth = 110f,
                    progressHeight = 8f
                )
            )
        ),
        WidgetConfig(
            id = -2,
            name = "Neon Cyberpunk Tracker",
            description = "A striking futuristic neon blade dashboard showing a full 24-hr layout with detailed system properties.",
            author = "RetroAesthetic",
            widthRatio = 4,
            heightRatio = 2,
            backgroundColor = "#FF08070F",
            borderRadius = 24f,
            borderStrokeWidth = 2.5f,
            borderStrokeColor = "#FFFF009F", // Neon Pink border
            layers = listOf(
                // Clock layer left
                WidgetLayer(
                    name = "Neon Hours",
                    type = LayerType.TEXT,
                    offsetX = -85f,
                    offsetY = -12f,
                    textValue = "\$df(HH)\$",
                    textColor = "#FF00FFFF", // Neon Cyan
                    textSize = 52f,
                    isBold = true
                ),
                WidgetLayer(
                    name = "Neon Minutes",
                    type = LayerType.TEXT,
                    offsetX = -85f,
                    offsetY = 30f,
                    textValue = "\$df(mm)\$",
                    textColor = "#FFFF009F", // Neon Pink
                    textSize = 34f,
                    isBold = true
                ),
                // Center Separator shape
                WidgetLayer(
                    name = "Vertical Neon Line",
                    type = LayerType.SHAPE,
                    offsetX = -25f,
                    offsetY = 0f,
                    shapeType = ShapeType.RECTANGLE,
                    shapeWidth = 3f,
                    shapeHeight = 110f,
                    shapeColor = "#FFFF009F"
                ),
                // Device name row
                WidgetLayer(
                    name = "Sys Label",
                    type = LayerType.TEXT,
                    offsetX = 35f,
                    offsetY = -35f,
                    textValue = "NET.NODE //",
                    textColor = "#FF8E8E93",
                    textSize = 10f,
                    textAlignment = "LEFT",
                    isBold = true
                ),
                WidgetLayer(
                    name = "Device Info",
                    type = LayerType.TEXT,
                    offsetX = 105f,
                    offsetY = -35f,
                    textValue = "\$si(model)\$",
                    textColor = "#FF00FFFF", // Neon Cyan
                    textSize = 11f,
                    isBold = true
                ),
                // Battery circular frame
                WidgetLayer(
                    name = "Circular Indicator",
                    type = LayerType.PROGRESS,
                    offsetX = 55f,
                    offsetY = 15f,
                    progressStyle = ProgressStyle.CIRCULAR,
                    progressValueFormula = "\$bi(level)\$",
                    progressColor = "#FF00FFFF",
                    progressTrackColor = "#1100FFFF",
                    progressHeight = 12f
                ),
                WidgetLayer(
                    name = "Circular Bat Text",
                    type = LayerType.TEXT,
                    offsetX = 55f,
                    offsetY = 15f,
                    textValue = "\$bi(level)\$",
                    textColor = "#FFFFFFFF",
                    textSize = 11f,
                    isBold = true
                ),
                // Mini weather summary card
                WidgetLayer(
                    name = "Condition Tracker",
                    type = LayerType.TEXT,
                    offsetX = 125f,
                    offsetY = 5f,
                    textValue = "STATUS",
                    textColor = "#FF8E8E93",
                    textSize = 9f,
                    isBold = true
                ),
                WidgetLayer(
                    name = "Weather Condition Status",
                    type = LayerType.TEXT,
                    offsetX = 125f,
                    offsetY = 22f,
                    textValue = "\$wi(cond)\$",
                    textColor = "#FFFF009F", // Neon Pink
                    textSize = 13f,
                    isBold = true
                )
            )
        ),
        WidgetConfig(
            id = -3,
            name = "iOS Crimson Calendar",
            description = "A beautiful minimalist calendar tile matching classical Cupertino home screens with full dates.",
            author = "CupertinoDesigns",
            widthRatio = 2,
            heightRatio = 2,
            backgroundColor = "#FFFFFFFF",
            borderRadius = 24f,
            borderStrokeWidth = 1f,
            borderStrokeColor = "#E5E5EA",
            layers = listOf(
                // Month header bar base
                WidgetLayer(
                    name = "Top Bar Accent",
                    type = LayerType.SHAPE,
                    offsetX = 0f,
                    offsetY = -55f,
                    shapeType = ShapeType.ROUNDED_RECTANGLE,
                    shapeWidth = 130f,
                    shapeHeight = 25f,
                    shapeColor = "#FFFF3B30", // Crimson red
                    shapeCornerRadius = 4f
                ),
                WidgetLayer(
                    name = "Month String",
                    type = LayerType.TEXT,
                    offsetX = 0f,
                    offsetY = -28f,
                    textValue = "\$df(MMMM)\$",
                    textColor = "#FFFF3B30",
                    textSize = 15f,
                    isBold = true
                ),
                WidgetLayer(
                    name = "Day Numeric",
                    type = LayerType.TEXT,
                    offsetX = 0f,
                    offsetY = 10f,
                    textValue = "\$df(dd)\$",
                    textColor = "#FF1C1C1E",
                    textSize = 40f,
                    isBold = true
                ),
                WidgetLayer(
                    name = "Day Weekname",
                    type = LayerType.TEXT,
                    offsetX = 0f,
                    offsetY = 42f,
                    textValue = "\$df(EEEE)\$",
                    textColor = "#FF8E8E93",
                    textSize = 12f,
                    isBold = true
                )
            )
        ),
        WidgetConfig(
            id = -4,
            name = "Ethereal Neon Ring",
            description = "A radial minimalist clock framed by circular power and calendar meters.",
            author = "ZenMinds",
            widthRatio = 4,
            heightRatio = 2,
            backgroundColor = "#FF0C0F17",
            borderRadius = 28f,
            borderStrokeWidth = 0f,
            layers = listOf(
                // Radial ring
                WidgetLayer(
                    name = "Power Arc Ring",
                    type = LayerType.PROGRESS,
                    offsetX = -70f,
                    offsetY = 0f,
                    progressStyle = ProgressStyle.CIRCULAR,
                    progressValueFormula = "\$bi(level)\$",
                    progressColor = "#FFE040FB", // Violet accent
                    progressTrackColor = "#11E040FB",
                    progressHeight = 18f
                ),
                // Hour digital centered inside ring
                WidgetLayer(
                    name = "Clock Hours Centered",
                    type = LayerType.TEXT,
                    offsetX = -70f,
                    offsetY = -8f,
                    textValue = "\$df(hh:mm)\$",
                    textColor = "#FFFFFFFF",
                    textSize = 18f,
                    isBold = true
                ),
                // PM indicator below it inside ring
                WidgetLayer(
                    name = "Clock AM/PM subtext",
                    type = LayerType.TEXT,
                    offsetX = -70f,
                    offsetY = 12f,
                    textValue = "\$df(a)\$",
                    textColor = "#FFE040FB",
                    textSize = 8f,
                    isBold = true
                ),
                // Right panel text stack
                WidgetLayer(
                    name = "Greetings Label",
                    type = LayerType.TEXT,
                    offsetX = 50f,
                    offsetY = -30f,
                    textValue = "SYSTEM CORE",
                    textColor = "#FFB0BEC5",
                    textSize = 11f,
                    textAlignment = "LEFT",
                    isBold = true
                ),
                WidgetLayer(
                    name = "Model Value text",
                    type = LayerType.TEXT,
                    offsetX = 50f,
                    offsetY = -12f,
                    textValue = "\$si(model)\$",
                    textColor = "#FFFFFFFF",
                    textSize = 14f,
                    textAlignment = "LEFT",
                    isBold = true
                ),
                // Bottom detail
                WidgetLayer(
                    name = "Bottom Details",
                    type = LayerType.TEXT,
                    offsetX = 50f,
                    offsetY = 18f,
                    textValue = "\$df(EEEE, dd MMMM)\$",
                    textColor = "#FFE040FB",
                    textSize = 12f,
                    textAlignment = "LEFT",
                    isBold = true
                ),
                // Small battery charge flash icon
                WidgetLayer(
                    name = "Battery Flash Bolt icon",
                    type = LayerType.ICON,
                    offsetX = 120f,
                    offsetY = -12f,
                    iconName = "bolt",
                    iconColor = "#FFE040FB",
                    iconSize = 16f
                )
            )
        )
    )
}

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WidgetViewModel
import com.example.model.*
import com.example.ui.components.WidgetRenderer
import com.example.ui.components.getIconByName
import com.example.ui.components.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetEditorScreen(
    viewModel: WidgetViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val widget by viewModel.selectedWidget.collectAsStateWithLifecycle()
    val activeIdx by viewModel.selectedLayerIndex.collectAsStateWithLifecycle()
    
    var activeTab by remember { mutableStateOf(0) } // 0: Layers, 1: Details, 2: Globals, 3: JSON Code
    var zoomScale by remember { mutableStateOf(1.0f) }

    if (widget == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFFFF029F))
                Spacer(modifier = Modifier.height(14.dp))
                Text("Retrieving canvas parameters...", color = Color.Gray)
            }
        }
        return
    }

    val currentWidget = widget!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentWidget.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (currentWidget.id < 0) "Built-in Preset Mode" else "Custom Local Workspace",
                            fontSize = 11.sp,
                            color = if (currentWidget.id < 0) Color(0xFFFF029F) else Color(0xFF00FFCC)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0F1D),
                    titleContentColor = Color.White
                ),
                actions = {
                    // Zoom Controls
                    IconButton(onClick = { zoomScale = (zoomScale - 0.1f).coerceIn(0.5f, 1.5f) }) {
                        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.LightGray)
                    }
                    Text(
                        "${(zoomScale * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = { zoomScale = (zoomScale + 0.1f).coerceIn(0.5f, 1.5f) }) {
                        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.LightGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Build Action Save
                    Button(
                        onClick = {
                            viewModel.saveActiveWidget(context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00FFCC),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        },
        containerColor = Color(0xFF060912),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // VIEWPORT 1: REAL-TIME canvas visual preview (Dynamic scaling)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.42f)
                    .background(Color(0xFF03050B))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                WidgetRenderer(
                    config = currentWidget,
                    scale = zoomScale
                )
            }

            // Divider separating canvas and controller deck
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )

            // VIEWPORT 2: SPLIT control tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF0A0F1D),
                contentColor = Color(0xFF00FFCC),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = Color(0xFF00FFCC)
                    )
                }
            ) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                    Text("Layers Tree", modifier = Modifier.padding(vertical = 12.dp), fontSize = 12.sp, color = if (activeTab == 0) Color(0xFF00FFCC) else Color.Gray, fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("Layer Settings", modifier = Modifier.padding(vertical = 12.dp), fontSize = 12.sp, color = if (activeTab == 1) Color(0xFF00FFCC) else Color.Gray, fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                    Text("Widget Rules", modifier = Modifier.padding(vertical = 12.dp), fontSize = 12.sp, color = if (activeTab == 2) Color(0xFF00FFCC) else Color.Gray, fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeTab == 3, onClick = { activeTab = 3 }) {
                    Text("JSON Code", modifier = Modifier.padding(vertical = 12.dp), fontSize = 12.sp, color = if (activeTab == 3) Color(0xFF00FFCC) else Color.Gray, fontWeight = FontWeight.Bold)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.58f)
                    .background(Color(0xFF070A14))
            ) {
                when (activeTab) {
                    0 -> LayersListTab(
                        config = currentWidget,
                        activeIdx = activeIdx,
                        onSelectIdx = { viewModel.selectLayerIndex(it) },
                        onAddLayer = { viewModel.addLayer(it) },
                        onDeleteActive = { viewModel.deleteActiveLayer() },
                        onMoveUp = { viewModel.moveActiveLayerUp() },
                        onMoveDown = { viewModel.moveActiveLayerDown() }
                    )
                    1 -> LayerDetailsTab(
                        config = currentWidget,
                        activeIdx = activeIdx,
                        onUpdateLayer = { viewModel.updateActiveLayer(it) }
                    )
                    2 -> WidgetRulesTab(
                        config = currentWidget,
                        onUpdateWidget = { name, bg, radius, sw, sc ->
                            viewModel.updateWidgetProperties(name, bg, radius, sw, sc)
                        }
                    )
                    3 -> JsonTreeTab(
                        json = viewModel.exportWidgetToJson()
                    )
                }
            }
        }
    }
}

// TAB 0: LAYERS LIST TREE PANEL
@Composable
fun LayersListTab(
    config: WidgetConfig,
    activeIdx: Int,
    onSelectIdx: (Int) -> Unit,
    onAddLayer: (LayerType) -> Unit,
    onDeleteActive: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Quick Action Bar for layer rearrange
        if (activeIdx in config.layers.indices) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F1426))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Layer: '${config.layers[activeIdx].name}'",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
                Row {
                    IconButton(onClick = onMoveUp, enabled = activeIdx > 0, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "", tint = if (activeIdx > 0) Color.LightGray else Color.DarkGray)
                    }
                    IconButton(onClick = onMoveDown, enabled = activeIdx < config.layers.lastIndex, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "", tint = if (activeIdx < config.layers.lastIndex) Color.LightGray else Color.DarkGray)
                    }
                    IconButton(onClick = onDeleteActive, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "", tint = Color(0xFFFF5252))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // List representation
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(config.layers) { index, layer ->
                val isActive = index == activeIdx
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) Color(0xFFFF029F).copy(alpha = 0.15f) else Color(0xFF0E1325))
                        .border(
                            width = 1.dp,
                            color = if (isActive) Color(0xFFFF029F) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectIdx(index) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = when (layer.type) {
                                    LayerType.TEXT -> Color(0xFF00FFCC).copy(alpha = 0.2f)
                                    LayerType.SHAPE -> Color(0xFF03DAC6).copy(alpha = 0.2f)
                                    LayerType.ICON -> Color(0xFFFFD54F).copy(alpha = 0.2f)
                                    LayerType.PROGRESS -> Color(0xFF00E5FF).copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (layer.type) {
                                LayerType.TEXT -> Icons.Default.TextFields
                                LayerType.SHAPE -> Icons.Default.Category
                                LayerType.ICON -> Icons.Default.EmojiEmotions
                                LayerType.PROGRESS -> Icons.Default.Speed
                            },
                            contentDescription = "",
                            tint = when (layer.type) {
                                LayerType.TEXT -> Color(0xFF00FFCC)
                                LayerType.SHAPE -> Color(0xFF03DAC6)
                                LayerType.ICON -> Color(0xFFFFD54F)
                                LayerType.PROGRESS -> Color(0xFF00E5FF)
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(layer.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = when (layer.type) {
                                LayerType.TEXT -> "Value: '${layer.textValue}'"
                                LayerType.SHAPE -> "Type: ${layer.shapeType} (${layer.shapeWidth.toInt()}x${layer.shapeHeight.toInt()}dp)"
                                LayerType.ICON -> "Icon Key: '${layer.iconName}'"
                                LayerType.PROGRESS -> "Formula: '${layer.progressValueFormula}'"
                            },
                            fontSize = 10.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Layer Position Marker Tag
                    Text(
                        "X:${layer.offsetX.toInt()}, Y:${layer.offsetY.toInt()}",
                        color = Color.DarkGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Add Layer footer bar
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("ADD WIDGET COMPONENT:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AddLayerActionBtn(title = "Text", icon = Icons.Default.TextFields) { onAddLayer(LayerType.TEXT) }
                    AddLayerActionBtn(title = "Shape", icon = Icons.Default.Category) { onAddLayer(LayerType.SHAPE) }
                    AddLayerActionBtn(title = "Icon", icon = Icons.Default.EmojiEmotions) { onAddLayer(LayerType.ICON) }
                    AddLayerActionBtn(title = "Progress", icon = Icons.Default.Speed) { onAddLayer(LayerType.PROGRESS) }
                }
            }
        }
    }
}

// TAB 1: DETAILS TAB (Active Layer Editing panel)
@Composable
fun LayerDetailsTab(
    config: WidgetConfig,
    activeIdx: Int,
    onUpdateLayer: (WidgetLayer) -> Unit
) {
    if (activeIdx !in config.layers.indices) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a layer in 'Layers Tree' to begin fine-tuning", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    val layer = config.layers[activeIdx]

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Core Transform Block (Always applicable)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1325))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Transform Coordinates", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // OffsetX Slide
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Offset X:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
                        Slider(
                            value = layer.offsetX,
                            onValueChange = { onUpdateLayer(layer.copy(offsetX = it)) },
                            valueRange = -180f..180f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC), thumbColor = Color(0xFF00FFCC))
                        )
                        Text("${layer.offsetX.toInt()}", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
                    }
                    
                    // OffsetY Slide
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Offset Y:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
                        Slider(
                            value = layer.offsetY,
                            onValueChange = { onUpdateLayer(layer.copy(offsetY = it)) },
                            valueRange = -90f..90f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC), thumbColor = Color(0xFF00FFCC))
                        )
                        Text("${layer.offsetY.toInt()}", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
                    }

                    // Rotation Slide
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rotation:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
                        Slider(
                            value = layer.rotation,
                            onValueChange = { onUpdateLayer(layer.copy(rotation = it)) },
                            valueRange = 0f..360f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(activeTrackColor = Color(0xFFFF029F), thumbColor = Color(0xFFFF029F))
                        )
                        Text("${layer.rotation.toInt()}°", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
                    }
                }
            }
        }

        // Layer Specific Blocks
        item {
            when (layer.type) {
                LayerType.TEXT -> EditTextPropertiesBlock(layer, onUpdateLayer)
                LayerType.SHAPE -> EditShapePropertiesBlock(layer, onUpdateLayer)
                LayerType.ICON -> EditIconPropertiesBlock(layer, onUpdateLayer)
                LayerType.PROGRESS -> EditProgressPropertiesBlock(layer, onUpdateLayer)
            }
        }
    }
}

// Sub-Block: Editing Text layer specs
@Composable
fun EditTextPropertiesBlock(
    layer: WidgetLayer,
    onUpdateLayer: (WidgetLayer) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1325)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Content & Styling (Text)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // Value / Formula Text Field
            OutlinedTextField(
                value = layer.textValue,
                onValueChange = { onUpdateLayer(layer.copy(textValue = it)) },
                label = { Text("Text / Kustom formula", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00FFCC),
                    unfocusedBorderColor = Color.DarkGray
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Formula quick insertions helper tags
            Text("FORMULA TAG INSERTS:", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FormulaTagChip(label = "Clock (time)") { onUpdateLayer(layer.copy(textValue = layer.textValue + "\$df(hh:mm)$")) }
                FormulaTagChip(label = "Date (day)") { onUpdateLayer(layer.copy(textValue = layer.textValue + "\$df(EEEE, d MMM)$")) }
                FormulaTagChip(label = "Battery%") { onUpdateLayer(layer.copy(textValue = layer.textValue + "\$bi(level)$%")) }
                FormulaTagChip(label = "Weather") { onUpdateLayer(layer.copy(textValue = layer.textValue + "\$wi(temp)$")) }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Text Size Slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Text Size:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(68.dp))
                Slider(
                    value = layer.textSize,
                    onValueChange = { onUpdateLayer(layer.copy(textSize = it)) },
                    valueRange = 8f..64f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC), thumbColor = Color(0xFF00FFCC))
                )
                Text("${layer.textSize.toInt()}sp", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Alignment Row Action
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Alignment:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(68.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AlignmentButton(activeSelected = layer.textAlignment == "LEFT", label = "Left") { onUpdateLayer(layer.copy(textAlignment = "LEFT")) }
                    AlignmentButton(activeSelected = layer.textAlignment == "CENTER", label = "Center") { onUpdateLayer(layer.copy(textAlignment = "CENTER")) }
                    AlignmentButton(activeSelected = layer.textAlignment == "RIGHT", label = "Right") { onUpdateLayer(layer.copy(textAlignment = "RIGHT")) }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bold & Italic row switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Bold face", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Checkbox(
                        checked = layer.isBold,
                        onCheckedChange = { onUpdateLayer(layer.copy(isBold = it)) },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF029F))
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Italic style", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Checkbox(
                        checked = layer.isItalic,
                        onCheckedChange = { onUpdateLayer(layer.copy(isItalic = it)) },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF029F))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hex Color Selector
            OutlinedTextField(
                value = layer.textColor,
                onValueChange = { onUpdateLayer(layer.copy(textColor = it)) },
                label = { Text("Hex Text Color (AARRGGBB)", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFFF029F)
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            ColorPresetRow(selectedHex = layer.textColor) { onUpdateLayer(layer.copy(textColor = it)) }
        }
    }
}

// Sub-Block: Editing Shapes layer specs
@Composable
fun EditShapePropertiesBlock(
    layer: WidgetLayer,
    onUpdateLayer: (WidgetLayer) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1325)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Content & Styling (Shape)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // ShapeType Select Buttons
            Text("Shape Type Selection:", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShapeSelectButton(selected = layer.shapeType == ShapeType.RECTANGLE, label = "Rect") { onUpdateLayer(layer.copy(shapeType = ShapeType.RECTANGLE)) }
                ShapeSelectButton(selected = layer.shapeType == ShapeType.ROUNDED_RECTANGLE, label = "R-Rect") { onUpdateLayer(layer.copy(shapeType = ShapeType.ROUNDED_RECTANGLE)) }
                ShapeSelectButton(selected = layer.shapeType == ShapeType.CIRCLE, label = "Circle") { onUpdateLayer(layer.copy(shapeType = ShapeType.CIRCLE)) }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Width Slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Width:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
                Slider(
                    value = layer.shapeWidth,
                    onValueChange = { onUpdateLayer(layer.copy(shapeWidth = it)) },
                    valueRange = 5f..360f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC))
                )
                Text("${layer.shapeWidth.toInt()}dp", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }

            // Height Slider (disabled for pure Circle)
            if (layer.shapeType != ShapeType.CIRCLE) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Height:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
                    Slider(
                        value = layer.shapeHeight,
                        onValueChange = { onUpdateLayer(layer.copy(shapeHeight = it)) },
                        valueRange = 2f..240f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC))
                    )
                    Text("${layer.shapeHeight.toInt()}dp", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                }
            }

            // Corner Radius (active only for RoundedRect)
            if (layer.shapeType == ShapeType.ROUNDED_RECTANGLE) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Radius:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
                    Slider(
                        value = layer.shapeCornerRadius,
                        onValueChange = { onUpdateLayer(layer.copy(shapeCornerRadius = it)) },
                        valueRange = 0f..80f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(activeTrackColor = Color(0xFFFF029F))
                    )
                    Text("${layer.shapeCornerRadius.toInt()}dp", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Shape Fill Color
            OutlinedTextField(
                value = layer.shapeColor,
                onValueChange = { onUpdateLayer(layer.copy(shapeColor = it)) },
                label = { Text("Fill Hex Color", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFFF029F)
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            ColorPresetRow(selectedHex = layer.shapeColor) { onUpdateLayer(layer.copy(shapeColor = it)) }

            Spacer(modifier = Modifier.height(14.dp))

            // Stroke Properties
            Text("Stroke Outlines (Borders):", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Width:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
                Slider(
                    value = layer.shapeStrokeWidth,
                    onValueChange = { onUpdateLayer(layer.copy(shapeStrokeWidth = it)) },
                    valueRange = 0f..20f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC))
                )
                Text("${layer.shapeStrokeWidth.toInt()}dp", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }

            if (layer.shapeStrokeWidth > 0f) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = layer.shapeStrokeColor,
                    onValueChange = { onUpdateLayer(layer.copy(shapeStrokeColor = it)) },
                    label = { Text("Stroke Hex Color", color = Color.Gray, fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Sub-Block: Editing Icon properties
@Composable
fun EditIconPropertiesBlock(
    layer: WidgetLayer,
    onUpdateLayer: (WidgetLayer) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1325)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Content & Styling (Vector Icon)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // Icon library selection scroll row
            Text("Select Vector Icon Symbol:", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            
            val iconPresetsList = listOf("schedule", "wb_sunny", "battery_std", "bolt", "cloud", "music_note", "favorite", "star", "android", "person")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyColumn(modifier = Modifier.height(115.dp).weight(1f)) {
                    val chunked = iconPresetsList.chunked(5)
                    itemsIndexed(chunked) { _, inlineList ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                            inlineList.forEach { pKey ->
                                val selected = layer.iconName == pKey
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) Color(0xFFFF029F) else Color(0xFF252A41))
                                        .clickable { onUpdateLayer(layer.copy(iconName = pKey)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getIconByName(pKey),
                                        contentDescription = "",
                                        tint = if (selected) Color.White else Color.LightGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Icon Size Slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Icon Size:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
                Slider(
                    value = layer.iconSize,
                    onValueChange = { onUpdateLayer(layer.copy(iconSize = it)) },
                    valueRange = 8f..64f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC))
                )
                Text("${layer.iconSize.toInt()}dp", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Color selection
            OutlinedTextField(
                value = layer.iconColor,
                onValueChange = { onUpdateLayer(layer.copy(iconColor = it)) },
                label = { Text("Icon Hex Color", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            ColorPresetRow(selectedHex = layer.iconColor) { onUpdateLayer(layer.copy(iconColor = it)) }
        }
    }
}

// Sub-Block: Editing Progress properties
@Composable
fun EditProgressPropertiesBlock(
    layer: WidgetLayer,
    onUpdateLayer: (WidgetLayer) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1325)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Content & Styling (Metric Gauge)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Formula representation
            OutlinedTextField(
                value = layer.progressValueFormula,
                onValueChange = { onUpdateLayer(layer.copy(progressValueFormula = it)) },
                label = { Text("Metric Formula (Outputs 0-100)", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormulaTagChip(label = "Battery formula") { onUpdateLayer(layer.copy(progressValueFormula = "\$bi(level)$")) }
                FormulaTagChip(label = "Static 50%") { onUpdateLayer(layer.copy(progressValueFormula = "50")) }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gauge Style (Horizontal vs Circular)
            Text("Gauge Shape Mode:", fontSize = 11.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShapeSelectButton(selected = layer.progressStyle == ProgressStyle.HORIZONTAL, label = "Horizontal Track") { 
                    onUpdateLayer(layer.copy(progressStyle = ProgressStyle.HORIZONTAL)) 
                }
                ShapeSelectButton(selected = layer.progressStyle == ProgressStyle.CIRCULAR, label = "Circular Ring") { 
                    onUpdateLayer(layer.copy(progressStyle = ProgressStyle.CIRCULAR)) 
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dimension Width/Thickness Slices
            if (layer.progressStyle == ProgressStyle.HORIZONTAL) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Width:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(64.dp))
                    Slider(
                        value = layer.progressWidth,
                        onValueChange = { onUpdateLayer(layer.copy(progressWidth = it)) },
                        valueRange = 25f..320f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC))
                    )
                    Text("${layer.progressWidth.toInt()}dp", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (layer.progressStyle == ProgressStyle.CIRCULAR) "Ring Size:" else "Thickness:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(64.dp))
                Slider(
                    value = layer.progressHeight,
                    onValueChange = { onUpdateLayer(layer.copy(progressHeight = it)) },
                    valueRange = 2f..48f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(activeTrackColor = Color(0xFFFF029F))
                )
                Text("${layer.progressHeight.toInt()}dp", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Active color block
            OutlinedTextField(
                value = layer.progressColor,
                onValueChange = { onUpdateLayer(layer.copy(progressColor = it)) },
                label = { Text("Progress Active Hex Color", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            ColorPresetRow(selectedHex = layer.progressColor) { onUpdateLayer(layer.copy(progressColor = it)) }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Track background color block
            OutlinedTextField(
                value = layer.progressTrackColor,
                onValueChange = { onUpdateLayer(layer.copy(progressTrackColor = it)) },
                label = { Text("Track Background Hex Color", color = Color.Gray, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


// TAB 2: GLOBAL ACTIONS (Widget settings color/border)
@Composable
fun WidgetRulesTab(
    config: WidgetConfig,
    onUpdateWidget: (String, String, Float, Float, String) -> Unit
) {
    var name by remember(config.name) { mutableStateOf(config.name) }
    var bgHex by remember(config.backgroundColor) { mutableStateOf(config.backgroundColor) }
    var radius by remember(config.borderRadius) { mutableStateOf(config.borderRadius) }
    var strWidth by remember(config.borderStrokeWidth) { mutableStateOf(config.borderStrokeWidth) }
    var strColor by remember(config.borderStrokeColor) { mutableStateOf(config.borderStrokeColor) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1325))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Container Framework", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            onUpdateWidget(name, bgHex, radius, strWidth, strColor)
                        },
                        label = { Text("Widget Title Label", color = Color.Gray, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Border corner radius
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Radius:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
                        Slider(
                            value = radius,
                            onValueChange = {
                                radius = it
                                onUpdateWidget(name, bgHex, radius, strWidth, strColor)
                            },
                            valueRange = 0f..36f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(activeTrackColor = Color(0xFF00FFCC))
                        )
                        Text("${radius.toInt()}dp", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Outer Hex color background
                    OutlinedTextField(
                        value = bgHex,
                        onValueChange = {
                            bgHex = it
                            onUpdateWidget(name, bgHex, radius, strWidth, strColor)
                        },
                        label = { Text("Container Hex Background", color = Color.Gray, fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ColorPresetRow(selectedHex = bgHex) {
                        bgHex = it
                        onUpdateWidget(name, bgHex, radius, strWidth, strColor)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1325))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Outer Stroke Border", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Border stroke width
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Thickness:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(64.dp))
                        Slider(
                            value = strWidth,
                            onValueChange = {
                                strWidth = it
                                onUpdateWidget(name, bgHex, radius, strWidth, strColor)
                            },
                            valueRange = 0f..10f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(activeTrackColor = Color(0xFFFF029F))
                        )
                        Text("${strWidth.toInt()}dp", fontSize = 11.sp, color = Color.White, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                    }

                    if (strWidth > 0f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = strColor,
                            onValueChange = {
                                strColor = it
                                onUpdateWidget(name, bgHex, radius, strWidth, strColor)
                            },
                            label = { Text("Stroke Hex Color", color = Color.Gray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}


// TAB 3: JSON CODE TAB
@Composable
fun JsonTreeTab(
    json: String
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dynamic JSON Config spec:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("KWGT Free Widget JSON", json)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Specification JSON copied to Clipboard!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF232946)),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "", modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Spec String", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF03050C))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text(
                        text = json,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color.Green,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}


// REUSABLE MINI INTERFACE ATOM UI COMPONENTS

@Composable
fun AddLayerActionBtn(
    title: String,
    icon: @Composable () -> Unit, // Overload for dynamic icons
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(4.dp)) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF14192E)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AddLayerActionBtn(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    AddLayerActionBtn(
        title = title,
        icon = { Icon(imageVector = icon, contentDescription = "", tint = Color(0xFF00FFCC), modifier = Modifier.size(20.dp)) },
        onClick = onClick
    )
}

@Composable
fun FormulaTagChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFFF029F).copy(alpha = 0.15f))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, color = Color(0xFFFF029F), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AlignmentButton(
    activeSelected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (activeSelected) Color(0xFF00FFCC) else Color(0xFF1E243E))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (activeSelected) Color.Black else Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ShapeSelectButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) Color(0xFFFF029F) else Color(0xFF1E243E))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ColorPresetRow(
    selectedHex: String,
    onSelectHex: (String) -> Unit
) {
    val hexPresets = listOf("#FFFFFFFF", "#FF00FFCC", "#FFFF029F", "#FF00E5FF", "#FFFFD54F", "#FF80CBC4", "#FF1E1E1E", "#00000000") // 00000000 at end represents transparent!
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        hexPresets.forEach { hex ->
            val color = parseHexColor(hex, Color.Transparent)
            val isCurrent = selectedHex.uppercase() == hex.uppercase()
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) Color.White else Color.DarkGray,
                        shape = CircleShape
                    )
                    .clickable { onSelectHex(hex) }
            )
        }
    }
}

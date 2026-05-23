package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.WidgetUiState
import com.example.WidgetViewModel
import com.example.model.PresetWidgets
import com.example.model.WidgetConfig
import com.example.ui.components.WidgetRenderer
import com.example.ui.components.parseHexColor
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WidgetViewModel,
    onNavigateToEditor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedWidgets by viewModel.allSavedWidgets.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showAiWaitDialog by remember { mutableStateOf(false) }
    
    var widgetNameInput by remember { mutableStateOf("") }
    var importJsonInput by remember { mutableStateOf("") }
    var aiPromptInput by remember { mutableStateOf("") }

    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importKwgtFile(context, uri)
        }
    }

    // Display Toast notifications for UI State changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is WidgetUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                showCreateDialog = false
                showImportDialog = false
                showAiWaitDialog = false
            }
            is WidgetUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                showAiWaitDialog = false
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(BoxBrush, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "KWID-GET",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0F1D),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { 
                        try {
                            filePickerLauncher.launch(arrayOf("*/*")) 
                        } catch(e: Exception) {
                            Toast.makeText(context, "File picker not available", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = "Import Directory/File", tint = Color.LightGray)
                    }
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(imageVector = Icons.Default.Terminal, contentDescription = "Import JSON Terminal", tint = Color.LightGray)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Creative Welcome Card & Digital clock header
            item {
                WelcomeHeader()
            }

            // AI WIDGET GENERATOR PROMPT ENGINE CARD
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10162B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF10162B), Color(0xFF0A0F1D))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFF029F).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = Color(0xFFFF029F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "AI Widget Builder",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Type what you want and Gemini AI will design, style, code, and generate your custom widget structure instantly!",
                            color = Color(0xFF90A4AE),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = aiPromptInput,
                            onValueChange = { aiPromptInput = it },
                            placeholder = { Text("e.g., Neon red cyberpunk clocks with circular battery arc...", fontSize = 13.sp, color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0A0D1A),
                                unfocusedContainerColor = Color(0xFF0A0D1A),
                                focusedBorderColor = Color(0xFFFF029F),
                                unfocusedBorderColor = Color(0xFF263238)
                            ),
                            maxLines = 3,
                            singleLine = false,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (aiPromptInput.trim().isNotEmpty()) {
                                    showAiWaitDialog = true
                                    viewModel.generateAIWidget(aiPromptInput) { success ->
                                        if (success) {
                                            aiPromptInput = ""
                                            val currentWidget = viewModel.selectedWidget.value
                                            if (currentWidget != null) {
                                                onNavigateToEditor(currentWidget.id)
                                            }
                                        }
                                        showAiWaitDialog = false
                                    }
                                } else {
                                    Toast.makeText(context, "Please describe your widget prompt!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF029F),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = "", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate visual layout", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // BUILT-IN PRESETS ROW title
            item {
                SectionHeader(title = "Stellar Presets", subtitle = "Load & customize responsive built-in designs")
            }

            // Preset visual carousels
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(PresetWidgets.presets) { preset ->
                        PresetWidgetCard(
                            config = preset,
                            onClick = {
                                viewModel.selectWidget(preset.id)
                                onNavigateToEditor(preset.id)
                            }
                        )
                    }
                }
            }

            // SAVED DESIGNS HEADER string
            item {
                SectionHeader(
                    title = "My Custom Canvas",
                    subtitle = "Locally saved custom widgets applied to launcher",
                    action = {
                        IconButton(onClick = {
                            widgetNameInput = ""
                            showCreateDialog = true
                        }) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = "New Layout", tint = Color(0xFF00FFCC))
                        }
                    }
                )
            }

            // Grid of saved customized widgets
            if (savedWidgets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DashboardCustomize,
                                contentDescription = "",
                                tint = Color(0xFF263238),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "No Custom Widgets Yet",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Tap + icon or use AI prompt to build your first template",
                                color = Color(0xFF455A64),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(savedWidgets) { customWidget ->
                    SavedWidgetListItem(
                        config = customWidget,
                        onEdit = {
                            viewModel.selectWidget(customWidget.id)
                            onNavigateToEditor(customWidget.id)
                        },
                        onDelete = {
                            viewModel.deleteWidget(customWidget.id)
                            Toast.makeText(context, "Widget Deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        // DESIGN: POPUP MODAL DIALOGS
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Erect Blank Canvas", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Give your custom widget templates an appropriate identity:", fontSize = 13.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = widgetNameInput,
                            onValueChange = { widgetNameInput = it },
                            label = { Text("Widget Title", color = Color.Gray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00FFCC),
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                containerColor = Color(0xFF0A0E1A),
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (widgetNameInput.trim().isNotEmpty()) {
                                viewModel.createNewWidget(widgetNameInput.trim())
                                showCreateDialog = false
                                // Auto transition to editor after creation on UI stream
                                viewModel.selectedWidget.value?.let {
                                    onNavigateToEditor(it.id)
                                }
                            } else {
                                Toast.makeText(context, "Please enter a valid title", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Create Workspace", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // Import Terminal Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("Import JSON Preset Terminal", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Paste backup JSON specification strings to parser engine:", fontSize = 12.sp, color = Color.Gray)
                        OutlinedTextField(
                            value = importJsonInput,
                            onValueChange = { importJsonInput = it },
                            placeholder = { Text("Paste valid WidgetConfig JSON object here...", fontSize = 11.sp, color = Color.DarkGray) },
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Green),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Green,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            maxLines = 10,
                            singleLine = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                },
                containerColor = Color(0xFF04060C),
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (importJsonInput.trim().isNotEmpty()) {
                                val success = viewModel.importWidgetFromJson(importJsonInput.trim())
                                if (success) {
                                    showImportDialog = false
                                    importJsonInput = ""
                                    viewModel.selectedWidget.value?.let {
                                        onNavigateToEditor(it.id)
                                    }
                                } else {
                                    Toast.makeText(context, "Mismatched syntax. Check input JSON schema matches WidgetConfig!", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    ) {
                        Text("Compile & Load", color = Color.Green, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Close", color = Color.Gray)
                    }
                }
            )
        }

        // AI Generation Load Screen
        if (showAiWaitDialog) {
            AlertDialog(
                onDismissRequest = {}, // Force loading blocking
                title = { null },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF029F))
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "AI Creative Mind at Work...",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Gemini is generating layout trees, calculating coordinates, matching color rules, and writing Kustom tags...",
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                },
                containerColor = Color(0xFF070A14),
                confirmButton = {}
            )
        }
    }
}

@Composable
fun WelcomeHeader() {
    var currentTimeString by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
            currentTimeString = sdf.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF00FFCC).copy(alpha = 0.08f), Color(0xFFFF029F).copy(alpha = 0.08f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(Color(0xFF00FFCC).copy(alpha = 0.3f), Color(0xFFFF029F).copy(alpha = 0.3f))),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "WIDGET TERMINAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00FFCC),
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Create Without Limits",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Digital dynamic clock display
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentTimeString,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = Color(0xFFFF029F),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Welcome to Kwid-get, the comprehensive widget engine. Design custom shapes, embed progress gauges, hook into dynamic system formulas, or harness Gemini AI to build home screen widgets in seconds.",
                color = Color(0xFFBACADB),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
        if (action != null) {
            action()
        }
    }
}

@Composable
fun PresetWidgetCard(
    config: WidgetConfig,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1325)),
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Widget representation render container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF060912))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                // Render custom widget in smaller miniature scale:
                WidgetRenderer(config = config, scale = 0.65f)
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Text(
                text = config.name,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = config.description,
                color = Color.Gray,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "by ${config.author}",
                    color = Color(0xFF00FFCC).copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF029F).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${config.widthRatio}x${config.heightRatio}",
                        fontSize = 9.sp,
                        color = Color(0xFFFF029F),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SavedWidgetListItem(
    config: WidgetConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1224)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Widget Render block
            Box(
                modifier = Modifier
                    .size(width = 130.dp, height = 75.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF050810))
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                WidgetRenderer(config = config, scale = 0.35f)
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // Description section
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Type: Grid ${config.widthRatio}x${config.heightRatio}",
                    color = Color(0xFF00FFCC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Modified recently",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // Trailing actions
            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.LightGray)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFFF3B30))
                }
            }
        }
    }
}

val BoxBrush = Brush.horizontalGradient(listOf(Color(0xFF00FFCC), Color(0xFFFF029F)))

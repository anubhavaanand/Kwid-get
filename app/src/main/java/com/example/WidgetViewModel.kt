package com.example

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.model.LayerType
import com.example.model.PresetWidgets
import com.example.model.WidgetConfig
import com.example.model.WidgetLayer
import com.example.provider.KwgtFreeWidgetProvider
import com.example.repository.WidgetRepository
import com.example.service.GeminiGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetViewModel(private val repository: WidgetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<WidgetUiState>(WidgetUiState.Idle)
    val uiState: StateFlow<WidgetUiState> = _uiState.asStateFlow()

    // Query database list
    val allSavedWidgets: StateFlow<List<WidgetConfig>> = repository.getAllWidgets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedWidget = MutableStateFlow<WidgetConfig?>(null)
    val selectedWidget: StateFlow<WidgetConfig?> = _selectedWidget.asStateFlow()

    private val _selectedLayerIndex = MutableStateFlow<Int>(-1)
    val selectedLayerIndex: StateFlow<Int> = _selectedLayerIndex.asStateFlow()

    fun selectWidget(id: Int) {
        viewModelScope.launch {
            _uiState.value = WidgetUiState.Loading
            
            // Check if it is a built-in preset (negative IDs)
            if (id < 0) {
                val preset = PresetWidgets.presets.find { it.id == id }
                _selectedWidget.value = preset
                _selectedLayerIndex.value = if (preset?.layers?.isNotEmpty() == true) 0 else -1
                _uiState.value = WidgetUiState.Success("Preset loaded successfully")
                return@launch
            }

            // Otherwise, grab from local database
            val dbWidget = repository.getWidgetById(id)
            if (dbWidget != null) {
                _selectedWidget.value = dbWidget
                _selectedLayerIndex.value = if (dbWidget.layers.isNotEmpty() == true) 0 else -1
                _uiState.value = WidgetUiState.Success("Widget loaded successfully")
            } else {
                // If ID is specified but not found, fallback to loading first preset
                _selectedWidget.value = PresetWidgets.presets[0]
                _selectedLayerIndex.value = if (PresetWidgets.presets[0].layers.isNotEmpty()) 0 else -1
                _uiState.value = WidgetUiState.Error("Widget not found, fallback loaded")
            }
        }
    }

    fun createNewWidget(name: String, width: Int = 4, height: Int = 2) {
        viewModelScope.launch {
            _uiState.value = WidgetUiState.Loading
            val defaultWidget = WidgetConfig(
                name = name,
                description = "Custom visual widget",
                widthRatio = width,
                heightRatio = height,
                backgroundColor = "#FF1E1E1E",
                layers = listOf(
                    WidgetLayer(
                        name = "Main Calendar Text",
                        type = LayerType.TEXT,
                        textValue = "\$df(EEEE, d MMMM)\$",
                        textColor = "#FFFFFFFF",
                        textSize = 20f,
                        offsetY = -15f
                    ),
                    WidgetLayer(
                        name = "System Model",
                        type = LayerType.TEXT,
                        textValue = "SYSTEM CORE // \$si(model)\$",
                        textColor = "#FF80CBC4",
                        textSize = 12f,
                        offsetY = 15f,
                        isBold = true
                    )
                )
            )
            val newId = repository.saveWidget(defaultWidget)
            val updated = defaultWidget.copy(id = newId)
            _selectedWidget.value = updated
            _selectedLayerIndex.value = 0
            _uiState.value = WidgetUiState.Success("New Custom Widget Built!")
        }
    }

    fun deleteWidget(id: Int) {
        viewModelScope.launch {
            if (id > 0) {
                repository.deleteWidgetById(id)
                _uiState.value = WidgetUiState.Success("Widget deleted successfully")
            }
        }
    }

    fun addLayer(type: LayerType) {
        val current = _selectedWidget.value ?: return
        val newLayer = when (type) {
            LayerType.TEXT -> WidgetLayer(
                name = "My Text Layer",
                type = LayerType.TEXT,
                textValue = "Time: \$df(hh:mm a)\$",
                textColor = "#FFFFFFFF",
                textSize = 16f
            )
            LayerType.SHAPE -> WidgetLayer(
                name = "My Card Shape",
                type = LayerType.SHAPE,
                shapeColor = "#33FFFFFF"
            )
            LayerType.ICON -> WidgetLayer(
                name = "My Clock Icon",
                type = LayerType.ICON,
                iconName = "schedule",
                iconColor = "#FF80CBC4"
            )
            LayerType.PROGRESS -> WidgetLayer(
                name = "Battery Charger Meter",
                type = LayerType.PROGRESS,
                progressValueFormula = "\$bi(level)\$",
                progressColor = "#FF80CBC4"
            )
        }
        val updatedLayers = current.layers.toMutableList().apply { add(newLayer) }
        _selectedWidget.value = current.copy(layers = updatedLayers)
        _selectedLayerIndex.value = updatedLayers.lastIndex
    }

    fun selectLayerIndex(index: Int) {
        _selectedLayerIndex.value = index
    }

    fun updateActiveLayer(updated: WidgetLayer) {
        val current = _selectedWidget.value ?: return
        val activeIndex = _selectedLayerIndex.value
        if (activeIndex in current.layers.indices) {
            val updatedLayers = current.layers.toMutableList().apply {
                set(activeIndex, updated)
            }
            _selectedWidget.value = current.copy(layers = updatedLayers)
        }
    }

    fun updateWidgetProperties(
        name: String,
        bgHex: String,
        radius: Float,
        strokeWidth: Float,
        strokeColHex: String
    ) {
        val current = _selectedWidget.value ?: return
        _selectedWidget.value = current.copy(
            name = name,
            backgroundColor = bgHex,
            borderRadius = radius,
            borderStrokeWidth = strokeWidth,
            borderStrokeColor = strokeColHex
        )
    }

    fun deleteActiveLayer() {
        val current = _selectedWidget.value ?: return
        val activeIndex = _selectedLayerIndex.value
        if (activeIndex in current.layers.indices) {
            val updatedLayers = current.layers.toMutableList().apply {
                removeAt(activeIndex)
            }
            _selectedWidget.value = current.copy(layers = updatedLayers)
            _selectedLayerIndex.value = if (updatedLayers.isNotEmpty()) 0 else -1
        }
    }

    fun moveActiveLayerUp() {
        val current = _selectedWidget.value ?: return
        val activeIndex = _selectedLayerIndex.value
        if (activeIndex > 0 && activeIndex in current.layers.indices) {
            val updatedLayers = current.layers.toMutableList().apply {
                val temp = this[activeIndex]
                this[activeIndex] = this[activeIndex - 1]
                this[activeIndex - 1] = temp
            }
            _selectedWidget.value = current.copy(layers = updatedLayers)
            _selectedLayerIndex.value = activeIndex - 1
        }
    }

    fun moveActiveLayerDown() {
        val current = _selectedWidget.value ?: return
        val activeIndex = _selectedLayerIndex.value
        if (activeIndex in 0 until current.layers.lastIndex) {
            val updatedLayers = current.layers.toMutableList().apply {
                val temp = this[activeIndex]
                this[activeIndex] = this[activeIndex + 1]
                this[activeIndex + 1] = temp
            }
            _selectedWidget.value = current.copy(layers = updatedLayers)
            _selectedLayerIndex.value = activeIndex + 1
        }
    }

    fun saveActiveWidget(context: Context) {
        val current = _selectedWidget.value ?: return
        
        viewModelScope.launch {
            _uiState.value = WidgetUiState.Loading
            
            // If ID < 0, it is a built-in. Save as a duplicate NEW custom widget instead!
            val toSave = if (current.id < 0) {
                current.copy(id = 0, name = "${current.name} Copy")
            } else current

            val savedId = repository.saveWidget(toSave)
            _selectedWidget.value = toSave.copy(id = savedId)
            
            // Force broadcast home-widget update instantly!
            KwgtFreeWidgetProvider.forceUpdateAllWidgets(context)
            _uiState.value = WidgetUiState.Success("Widget saved and applied successfully!")
        }
    }

    fun generateAIWidget(prompt: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = WidgetUiState.Loading
            val config = withContext(Dispatchers.IO) {
                GeminiGenerator.generateWidgetDesign(prompt)
            }
            
            if (config != null) {
                // Assign 0 to save as a new custom local widget config
                val configForSaving = config.copy(id = 0, author = "Gemini Creator")
                val savedId = repository.saveWidget(configForSaving)
                val finalConfig = configForSaving.copy(id = savedId)
                
                _selectedWidget.value = finalConfig
                _selectedLayerIndex.value = if (finalConfig.layers.isNotEmpty()) 0 else -1
                _uiState.value = WidgetUiState.Success("AI widget loaded and inserted!")
                callback(true)
            } else {
                _uiState.value = WidgetUiState.Error("Failed to parse AI layout. Check your API key!")
                callback(false)
            }
        }
    }

    /**
     * Lets users import widgets from direct JSON pasting or download formulas!
     */
    fun importWidgetFromJson(json: String): Boolean {
        return try {
            val adapter = com.squareup.moshi.Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
                .adapter(WidgetConfig::class.java)
            val config = adapter.fromJson(json)
            if (config != null) {
                viewModelScope.launch {
                    val toSave = config.copy(id = 0)
                    val savedId = repository.saveWidget(toSave)
                    _selectedWidget.value = toSave.copy(id = savedId)
                    _selectedLayerIndex.value = if (toSave.layers.isNotEmpty()) 0 else -1
                    _uiState.value = WidgetUiState.Success("Widget imported successfully!")
                }
                true
            } else false
        } catch (e: Exception) {
            Log.e("WidgetViewModel", "JSON import mismatch: ${e.message}")
            false
        }
    }

    fun importKwgtFile(context: Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = WidgetUiState.Loading
            try {
                val contentResolver = context.contentResolver
                var jsonStr: String? = null
                withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        java.util.zip.ZipInputStream(inputStream).use { zis ->
                            var entry = zis.nextEntry
                            while (entry != null) {
                                if (entry.name == "preset.json" || entry.name == "widget.json" || entry.name.endsWith(".json")) {
                                    jsonStr = zis.bufferedReader(Charsets.UTF_8).readText()
                                    break
                                }
                                entry = zis.nextEntry
                            }
                        }
                    }
                }
                if (jsonStr != null) {
                    val success = importWidgetFromJson(jsonStr!!)
                    if (!success) {
                        _uiState.value = WidgetUiState.Error("Failed to parse KWGT JSON structure")
                    }
                } else {
                     _uiState.value = WidgetUiState.Error("No valid configuration found in KWGT zip file")
                }
            } catch(e: Exception) {
               _uiState.value = WidgetUiState.Error("Error reading KWGT archive: ${e.message}")
            }
        }
    }

    fun exportWidgetToJson(): String {
        return try {
            val current = _selectedWidget.value ?: return ""
            com.squareup.moshi.Moshi.Builder()
                .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()
                .adapter(WidgetConfig::class.java)
                .indent("  ")
                .toJson(current)
        } catch (e: Exception) {
            ""
        }
    }
}

sealed class WidgetUiState {
    object Idle : WidgetUiState()
    object Loading : WidgetUiState()
    data class Success(val message: String) : WidgetUiState()
    data class Error(val message: String) : WidgetUiState()
}

class WidgetViewModelFactory(private val repository: WidgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WidgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WidgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.provider

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.model.PresetWidgets
import com.example.model.WidgetConfig
import com.example.parser.WidgetBitmapRenderer
import com.example.repository.WidgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KwgtFreeWidgetProvider : AppWidgetProvider() {

    private val jobScope = CoroutineScope(Dispatchers.Default)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d("KwgtFreeWidgetProvider", "onUpdate triggered for widgets: ${appWidgetIds.joinToString()}")
        
        for (appWidgetId in appWidgetIds) {
            updateWidgetContent(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        Log.d("KwgtFreeWidgetProvider", "onAppWidgetOptionsChanged for widget: $appWidgetId")
        updateWidgetContent(context, appWidgetManager, appWidgetId)
    }

    private fun updateWidgetContent(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        jobScope.launch {
            val repository = WidgetRepository.create(context)
            
            // Get mapping from preferences
            val prefs = context.getSharedPreferences("kwgt_free_widget_prefs", Context.MODE_PRIVATE)
            val mappedId = prefs.getInt("widget_mapped_id_$appWidgetId", 999999) // 999999 represents "unmapped" or "default first"

            // Load correct configuration: either standard database item or a matching built-in preset
            val config: WidgetConfig = when {
                mappedId == -1 || mappedId == 999999 -> PresetWidgets.presets[0] // Material Minimal
                mappedId == -2 -> PresetWidgets.presets[1] // Cyberpunk Neon
                mappedId == -3 -> PresetWidgets.presets[2] // Cupertino Calendar
                mappedId == -4 -> PresetWidgets.presets[3] // Radial Ring Clock
                mappedId > 0 -> {
                    repository.getWidgetById(mappedId) ?: PresetWidgets.presets[0]
                }
                else -> {
                    // Grab first saved database item, fallback to first preset
                    val saved = repository.getAllWidgets().firstOrNull()?.firstOrNull()
                    saved ?: PresetWidgets.presets[0]
                }
            }

            // Calculate cell dimensions in pixels
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 270)
            val minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 130)

            val dm = context.resources.displayMetrics
            val pixelWidth = (minWidthDp * dm.density).toInt().coerceAtLeast(300)
            val pixelHeight = (minHeightDp * dm.density).toInt().coerceAtLeast(150)

            Log.d("KwgtFreeWidgetProvider", "Rendering widget $appWidgetId: $minWidthDp x $minHeightDp dp -> $pixelWidth x $pixelHeight px")

            // Render high-fidelity bitmap
            val renderedBitmap = WidgetBitmapRenderer.renderToBitmap(context, config, pixelWidth, pixelHeight)

            val views = RemoteViews(context.packageName, R.layout.kwgt_free_widget_layout)
            views.setImageViewBitmap(R.id.widget_image, renderedBitmap)

            // Setup click intent to launch MainActivity
            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("edit_widget_id", config.id)
                putExtra("triggered_appwidget_id", appWidgetId)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            // Commit the update of RemoteViews
            withContext(Dispatchers.Main) {
                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d("KwgtFreeWidgetProvider", "AppWidget updated successfully!")
            }
        }
    }
    
    companion object {
        /**
         * Helper utility that can be manually called to force re-draw all home widgets.
         */
        fun forceUpdateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val provider = android.content.ComponentName(context, KwgtFreeWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(provider)
            
            val intent = Intent(context, KwgtFreeWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
            Log.d("KwgtFreeWidgetProvider", "Broadcast forced update sent to all widget ids.")
        }
    }
}

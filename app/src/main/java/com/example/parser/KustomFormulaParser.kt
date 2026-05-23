package com.example.parser

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object KustomFormulaParser {
    var currentTemperature: Int = 23
    var currentWeatherCondition: String = "Sunny"
    var currentHumidity: Int = 45
    var currentWindSpeed: Int = 12

    /**
     * Parses and evaluates a string with Kustom formulas, e.g.:
     * "It is $df(hh:mm a)$ with $bi(level)$% battery remaining"
     */
    fun evaluate(context: Context?, formula: String): String {
        if (!formula.contains("$")) return formula
        
        // Match expressions in form: $func(args)$
        val regex = "\\$([a-zA-Z]+)\\(([^)]*)\\)\\$".toRegex()
        return regex.replace(formula) { matchResult ->
            val function = matchResult.groupValues[1].lowercase()
            val arguments = matchResult.groupValues[2]
            
            when (function) {
                "df" -> evaluateDateFormat(arguments)
                "bi" -> evaluateBatteryInfo(context, arguments)
                "wi" -> evaluateWeatherInfo(arguments)
                "si" -> evaluateSystemInfo(arguments)
                "mu" -> evaluateMath(arguments)
                else -> matchResult.value
            }
        }
    }

    private val sdfCache = java.util.concurrent.ConcurrentHashMap<String, SimpleDateFormat>()

    private var lastBatteryCheckTime = 0L
    private var cachedBatteryLevel = 75
    private var cachedIsCharging = false

    private fun evaluateDateFormat(pattern: String): String {
        return try {
            val trimmed = pattern.trim().replace("'", "")
            val sdf = sdfCache.getOrPut(trimmed) {
                SimpleDateFormat(trimmed, Locale.getDefault())
            }
            synchronized(sdf) {
                sdf.format(Date())
            }
        } catch (e: Exception) {
            "Format error"
        }
    }

    private fun evaluateBatteryInfo(context: Context?, arg: String): String {
        if (context == null) return "75"
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBatteryCheckTime > 5000L) {
            try {
                val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                    context.registerReceiver(null, filter)
                }
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 75
                val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
                cachedBatteryLevel = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 75
                
                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                cachedIsCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                lastBatteryCheckTime = currentTime
            } catch (e: Exception) {
                // Return cached fallback value on exception
            }
        }
        
        return when (arg.trim().lowercase()) {
            "level" -> cachedBatteryLevel.toString()
            "status" -> if (cachedIsCharging) "Charging" else "Discharging"
            "charging" -> if (cachedIsCharging) "1" else "0"
            else -> cachedBatteryLevel.toString()
        }
    }

    private fun evaluateWeatherInfo(arg: String): String {
        return when (arg.trim().lowercase()) {
            "temp" -> "${currentTemperature}°C"
            "cond" -> currentWeatherCondition
            "humidity" -> "${currentHumidity}%"
            "wind" -> "${currentWindSpeed} km/h"
            else -> "${currentTemperature}°C"
        }
    }

    private fun evaluateSystemInfo(arg: String): String {
        return when (arg.trim().lowercase()) {
            "model" -> Build.MODEL
            "man" -> Build.MANUFACTURER
            "android" -> "Android ${Build.VERSION.RELEASE}"
            else -> Build.MODEL
        }
    }

    private fun evaluateMath(arg: String): String {
        val parts = arg.split(",")
        if (parts.isEmpty()) return "0"
        val operation = parts[0].trim().lowercase()
        return try {
            when (operation) {
                "round" -> {
                    val num = parts.getOrNull(1)?.trim()?.toFloatOrNull() ?: 0f
                    Math.round(num).toString()
                }
                "cos" -> {
                    val num = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0
                    Math.cos(Math.toRadians(num)).toString()
                }
                "sin" -> {
                    val num = parts.getOrNull(1)?.trim()?.toDoubleOrNull() ?: 0.0
                    Math.sin(Math.toRadians(num)).toString()
                }
                else -> "0"
            }
        } catch (e: Exception) {
            "0"
        }
    }
}

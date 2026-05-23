package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.model.WidgetConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiGenerator {
    private const val TAG = "GeminiGenerator"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun generateWidgetDesign(userPrompt: String): WidgetConfig? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or invalid.")
            return null
        }

        val systemInstruction = """
            You are an expert android custom widget (KWGT clone) designer. Your job is to output a single valid JSON object representing a beautiful custom widget based on the user's prompt.
            The JSON object MUST strictly conform to this schema:
            
            {
              "name": "Widget Title",
              "description": "Short explanation of style choices",
              "author": "AI Widget Designer",
              "widthRatio": 4,
              "heightRatio": 2,
              "backgroundColor": "#FF1E1E24", // standard AARRGGBB hex color code
              "borderRadius": 16.0,
              "layers": [
                {
                  "name": "Layer Name e.g. Background accent",
                  "type": "TEXT" | "SHAPE" | "ICON" | "PROGRESS",
                  "offsetX": Float (X pos relative to center, standard ranges: -180 to 180),
                  "offsetY": Float (Y pos relative to center, standard ranges: -90 to 90),
                  "rotation": 0.0,
                  "visible": true,
                  
                  // For TEXT type:
                  "textValue": "Normal text or formula: ${'$'}df(hh:mm)${'$'} for time, ${'$'}df(EEEE, dd MMM)${'$'} for date, ${'$'}bi(level)${'$'}% for battery percentage, ${'$'}wi(temp)${'$'} for weather temp, ${'$'}si(model)${'$'} for device name",
                  "textColor": "#FFFFFFFF",
                  "textSize": 14.0 (for minor labels/info) or 48.0 (for clock time text),
                  "textAlignment": "CENTER" | "LEFT" | "RIGHT",
                  "isBold": Boolean,
                  "isItalic": Boolean,
                  
                  // For SHAPE type:
                  "shapeType": "RECTANGLE" | "CIRCLE" | "ROUNDED_RECTANGLE",
                  "shapeWidth": Float (e.g. 100.0 to 360.0),
                  "shapeHeight": Float (e.g. 5.0 to 180.0),
                  "shapeColor": "#FF6200EE" or "#55FFFFFF" for semi-transparent overlay cards,
                  "shapeCornerRadius": 12.0,
                  
                  // For ICON type:
                  "iconName": "schedule" (clock) | "wb_sunny" (weather) | "battery_std" (battery) | "bolt" (electricity) | "android" | "person" | "music_note" | "star" | "favorite",
                  "iconSize": Float (e.g. 24.0 or 32.0),
                  "iconColor": "#FFFFFFFF",
                  
                  // For PROGRESS type:
                  "progressValueFormula": "${'$'}bi(level)${'$'}",
                  "progressStyle": "HORIZONTAL" | "CIRCULAR",
                  "progressTrackColor": "#33FFFFFF",
                  "progressColor": "#FF03DAC6",
                  "progressWidth": Float,
                  "progressHeight": Float
                }
              ]
            }
            
            IMPORTANT RULES:
            1. Use balanced negative space. Make it elegant, modern, high contrast.
            2. For a complete widget, combine:
               - A SHAPE background or accent highlights if needed.
               - A prominent TEXT for time, Date or major metric.
               - ICON elements beside labels for better visual scanning.
               - A PROGRESS indicator mapped to battery ${'$'}bi(level)${'$'} for circular battery tracks.
            3. Put elements in visual grid: e.g. group clock on the left (X=-80), metrics on the right (X=80), or stack them.
            4. Do not include markdown block backticks around the json, output pure parseable JSON content immediately.
        """.trimIndent()

        val fullPrompt = "Generate widget style: $userPrompt"

        try {
            // Build the body JSON manually with org.json
            val contentObj = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", fullPrompt) })
                })
            }
            val sysInsObj = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            }
            
            val parentObj = JSONObject().apply {
                put("contents", JSONArray().apply { put(contentObj) })
                put("systemInstruction", sysInsObj)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBodyJson = parentObj.toString()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toRequestBody(mediaType)

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Request failed with code: ${response.code}, body: ${response.body?.string()}")
                return null
            }

            val responseBodyString = response.body?.string() ?: return null
            val responseJson = JSONObject(responseBodyString)
            
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                Log.e(TAG, "No candidates returned from Gemini.")
                return null
            }
            
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            
            var generatedText = parts.getJSONObject(0).optString("text") ?: return null
            
            // Clean markdown wrap if any, though system instructions prohibit it
            if (generatedText.contains("```json")) {
                generatedText = generatedText.substringAfter("```json").substringBefore("```").trim()
            } else if (generatedText.contains("```")) {
                generatedText = generatedText.substringAfter("```").substringBefore("```").trim()
            }
            
            Log.d(TAG, "Parsing JSON response: $generatedText")
            val widgetConfigAdapter = moshi.adapter(WidgetConfig::class.java)
            return widgetConfigAdapter.fromJson(generatedText)
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            return null
        }
    }
}

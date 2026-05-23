package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.WidgetConfig
import com.example.model.WidgetLayer
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Entity(tableName = "saved_widgets")
data class WidgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val author: String,
    val widthRatio: Int,
    val heightRatio: Int,
    val backgroundColor: String,
    val borderRadius: Float,
    val borderStrokeWidth: Float,
    val borderStrokeColor: String,
    val layersJson: String // Serialized List<WidgetLayer>
) {
    fun toDomainModel(moshi: Moshi): WidgetConfig {
        val type = Types.newParameterizedType(List::class.java, WidgetLayer::class.java)
        val adapter = moshi.adapter<List<WidgetLayer>>(type)
        val layersList = try {
            adapter.fromJson(this.layersJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return WidgetConfig(
            id = this.id,
            name = this.name,
            description = this.description,
            author = this.author,
            widthRatio = this.widthRatio,
            heightRatio = this.heightRatio,
            backgroundColor = this.backgroundColor,
            borderRadius = this.borderRadius,
            borderStrokeWidth = this.borderStrokeWidth,
            borderStrokeColor = this.borderStrokeColor,
            layers = layersList
        )
    }

    companion object {
        fun fromDomainModel(config: WidgetConfig, moshi: Moshi): WidgetEntity {
            val type = Types.newParameterizedType(List::class.java, WidgetLayer::class.java)
            val adapter = moshi.adapter<List<WidgetLayer>>(type)
            val json = adapter.toJson(config.layers)
            return WidgetEntity(
                id = config.id,
                name = config.name,
                description = config.description,
                author = config.author,
                widthRatio = config.widthRatio,
                heightRatio = config.heightRatio,
                backgroundColor = config.backgroundColor,
                borderRadius = config.borderRadius,
                borderStrokeWidth = config.borderStrokeWidth,
                borderStrokeColor = config.borderStrokeColor,
                layersJson = json
            )
        }
    }
}

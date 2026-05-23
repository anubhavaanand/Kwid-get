package com.example.repository

import android.content.Context
import com.example.db.WidgetDao
import com.example.db.WidgetDatabase
import com.example.db.WidgetEntity
import com.example.model.WidgetConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WidgetRepository(private val widgetDao: WidgetDao) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun getAllWidgets(): Flow<List<WidgetConfig>> {
        return widgetDao.getAllWidgets().map { entities ->
            entities.map { it.toDomainModel(moshi) }
        }
    }

    suspend fun getWidgetById(id: Int): WidgetConfig? {
        return widgetDao.getWidgetById(id)?.toDomainModel(moshi)
    }

    suspend fun saveWidget(config: WidgetConfig): Int {
        val entity = WidgetEntity.fromDomainModel(config, moshi)
        return widgetDao.insertWidget(entity).toInt()
    }

    suspend fun deleteWidgetById(id: Int) {
        widgetDao.deleteWidgetById(id)
    }

    companion object {
        fun create(context: Context): WidgetRepository {
            val db = WidgetDatabase.getDatabase(context)
            return WidgetRepository(db.widgetDao())
        }
    }
}

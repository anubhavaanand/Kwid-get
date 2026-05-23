package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetDao {
    @Query("SELECT * FROM saved_widgets ORDER BY id DESC")
    fun getAllWidgets(): Flow<List<WidgetEntity>>

    @Query("SELECT * FROM saved_widgets WHERE id = :id LIMIT 1")
    suspend fun getWidgetById(id: Int): WidgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidget(widget: WidgetEntity): Long

    @Query("DELETE FROM saved_widgets WHERE id = :id")
    suspend fun deleteWidgetById(id: Int)
}

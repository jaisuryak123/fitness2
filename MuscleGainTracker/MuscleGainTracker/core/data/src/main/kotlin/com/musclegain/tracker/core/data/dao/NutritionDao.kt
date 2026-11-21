package com.musclegain.tracker.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.musclegain.tracker.core.data.entity.NutritionEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition_entries WHERE entryDate = :date ORDER BY entryTime, id")
    fun observeForDate(date: String): Flow<List<NutritionEntryEntity>>

    @Query("SELECT * FROM nutrition_entries WHERE entryDate BETWEEN :start AND :end ORDER BY entryDate, entryTime, id")
    fun observeBetween(start: String, end: String): Flow<List<NutritionEntryEntity>>

    @Query("SELECT * FROM nutrition_entries WHERE id = :id")
    suspend fun getById(id: Long): NutritionEntryEntity?

    @Insert
    suspend fun insert(entity: NutritionEntryEntity): Long

    @Update
    suspend fun update(entity: NutritionEntryEntity): Int

    @Query("DELETE FROM nutrition_entries WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}

package com.musclegain.tracker.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.musclegain.tracker.core.data.entity.WorkoutExerciseEntity
import com.musclegain.tracker.core.data.entity.WorkoutSessionEntity
import com.musclegain.tracker.core.data.entity.WorkoutSessionRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE activityDate = :date ORDER BY id")
    fun observeForDate(date: String): Flow<List<WorkoutSessionRelation>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE activityDate BETWEEN :start AND :end ORDER BY activityDate, id")
    fun observeBetween(start: String, end: String): Flow<List<WorkoutSessionRelation>>

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSessionRelation?

    @Insert
    suspend fun insertSession(entity: WorkoutSessionEntity): Long

    @Update
    suspend fun updateSession(entity: WorkoutSessionEntity): Int

    @Insert
    suspend fun insertExercises(entities: List<WorkoutExerciseEntity>)

    @Query("DELETE FROM workout_exercises WHERE sessionId = :sessionId")
    suspend fun deleteExercises(sessionId: Long)

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long): Int
}

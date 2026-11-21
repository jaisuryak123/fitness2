package com.musclegain.tracker.core.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.musclegain.tracker.core.data.entity.BodyWeightEntryEntity
import com.musclegain.tracker.core.data.entity.DailyGoalsEntity
import com.musclegain.tracker.core.data.entity.ReminderSettingsEntity
import com.musclegain.tracker.core.data.entity.SupplementEntryEntity
import com.musclegain.tracker.core.data.entity.TrainingDayEntity
import com.musclegain.tracker.core.data.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observe(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun get(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserProfileEntity)
}

@Dao
interface GoalsDao {
    @Query("SELECT * FROM daily_goals ORDER BY effectiveDate")
    fun observeAll(): Flow<List<DailyGoalsEntity>>

    @Query("SELECT * FROM daily_goals ORDER BY effectiveDate")
    suspend fun getAll(): List<DailyGoalsEntity>

    @Query("SELECT * FROM daily_goals WHERE effectiveDate = :date")
    suspend fun getByDate(date: String): DailyGoalsEntity?

    @Insert
    suspend fun insert(entity: DailyGoalsEntity): Long

    @Update
    suspend fun update(entity: DailyGoalsEntity): Int
}

@Dao
interface SupplementDao {
    @Query("SELECT * FROM supplement_entries WHERE entryDate = :date ORDER BY entryTime, id")
    fun observeForDate(date: String): Flow<List<SupplementEntryEntity>>

    @Query("SELECT * FROM supplement_entries WHERE entryDate BETWEEN :start AND :end ORDER BY entryDate, entryTime, id")
    fun observeBetween(start: String, end: String): Flow<List<SupplementEntryEntity>>

    @Query("SELECT DISTINCT name FROM supplement_entries ORDER BY name")
    fun observeNames(): Flow<List<String>>

    @Insert
    suspend fun insert(entity: SupplementEntryEntity): Long

    @Update
    suspend fun update(entity: SupplementEntryEntity): Int

    @Query("DELETE FROM supplement_entries WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}

@Dao
interface BodyWeightDao {
    @Query("SELECT * FROM body_weight_entries WHERE recordedDate = :date")
    fun observeForDate(date: String): Flow<BodyWeightEntryEntity?>

    @Query("SELECT * FROM body_weight_entries WHERE recordedDate BETWEEN :start AND :end ORDER BY recordedDate")
    fun observeBetween(start: String, end: String): Flow<List<BodyWeightEntryEntity>>

    @Query("SELECT * FROM body_weight_entries ORDER BY recordedDate DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<BodyWeightEntryEntity>>

    @Query("SELECT * FROM body_weight_entries WHERE recordedDate = :date")
    suspend fun getByDate(date: String): BodyWeightEntryEntity?

    @Query("SELECT * FROM body_weight_entries ORDER BY recordedDate DESC LIMIT 1")
    suspend fun getLatest(): BodyWeightEntryEntity?

    @Query("SELECT * FROM body_weight_entries WHERE recordedDate <= :date ORDER BY recordedDate DESC LIMIT 1")
    suspend fun getLatestOnOrBefore(date: String): BodyWeightEntryEntity?

    @Insert
    suspend fun insert(entity: BodyWeightEntryEntity): Long

    @Update
    suspend fun update(entity: BodyWeightEntryEntity): Int

    @Query("DELETE FROM body_weight_entries WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}

@Dao
interface TrainingScheduleDao {
    @Query("SELECT * FROM training_schedule ORDER BY weekday")
    fun observeAll(): Flow<List<TrainingDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<TrainingDayEntity>)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder_settings WHERE id = 1")
    fun observe(): Flow<ReminderSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReminderSettingsEntity)
}

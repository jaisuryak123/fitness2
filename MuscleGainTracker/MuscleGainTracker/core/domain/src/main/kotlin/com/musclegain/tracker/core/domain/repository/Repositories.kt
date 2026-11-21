package com.musclegain.tracker.core.domain.repository

import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.CsvTable
import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.model.ReminderSettings
import com.musclegain.tracker.core.domain.model.SupplementEntry
import com.musclegain.tracker.core.domain.model.TrainingDay
import com.musclegain.tracker.core.domain.model.UserProfile
import com.musclegain.tracker.core.domain.model.UserProfileInput
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import com.musclegain.tracker.core.domain.validation.SaveOutcome
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(): UserProfile?
    suspend fun saveProfile(input: UserProfileInput): Result<SaveOutcome>
}

interface GoalsRepository {
    fun observeAllGoals(): Flow<List<DailyGoals>>
    fun observeGoalsFor(date: LocalDate): Flow<DailyGoals>
    suspend fun getGoalsFor(date: LocalDate): DailyGoals
    suspend fun saveGoals(goals: DailyGoals): Result<SaveOutcome>
}

interface NutritionRepository {
    fun observeEntriesForDate(date: LocalDate): Flow<List<NutritionEntry>>
    fun observeEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<NutritionEntry>>
    suspend fun getEntry(id: Long): NutritionEntry?
    suspend fun upsert(entry: NutritionEntry): Result<SaveOutcome>
    suspend fun delete(id: Long): Boolean
}

interface WorkoutRepository {
    fun observeSessionsForDate(date: LocalDate): Flow<List<WorkoutSessionWithExercises>>
    fun observeSessionsBetween(start: LocalDate, end: LocalDate): Flow<List<WorkoutSessionWithExercises>>
    suspend fun getSession(id: Long): WorkoutSessionWithExercises?
    suspend fun saveSession(value: WorkoutSessionWithExercises): Result<SaveOutcome>
    suspend fun deleteSession(id: Long): Boolean
}

interface SupplementRepository {
    fun observeEntriesForDate(date: LocalDate): Flow<List<SupplementEntry>>
    fun observeEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<SupplementEntry>>
    fun observeKnownNames(): Flow<List<String>>
    suspend fun upsert(entry: SupplementEntry): Result<SaveOutcome>
    suspend fun delete(id: Long): Boolean
}

interface BodyWeightRepository {
    fun observeForDate(date: LocalDate): Flow<BodyWeightEntry?>
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<BodyWeightEntry>>
    fun observeRecent(limit: Int): Flow<List<BodyWeightEntry>>
    suspend fun getLatest(): BodyWeightEntry?
    suspend fun getLatestOnOrBefore(date: LocalDate): BodyWeightEntry?
    suspend fun upsert(entry: BodyWeightEntry): Result<SaveOutcome>
    suspend fun delete(id: Long): Boolean
}

interface TrainingScheduleRepository {
    fun observeSchedule(): Flow<List<TrainingDay>>
    suspend fun saveSchedule(days: List<TrainingDay>): Result<SaveOutcome>
}

interface ReminderRepository {
    fun observeSettings(): Flow<ReminderSettings>
    suspend fun saveSettings(settings: ReminderSettings)
}

interface AppPreferencesRepository {
    val onboardingCompleted: Flow<Boolean>
    val lastBackupAtMillis: Flow<Long?>
    suspend fun setOnboardingCompleted(value: Boolean)
    suspend fun setLastBackupAtMillis(value: Long)
}

interface BackupRepository {
    suspend fun exportJson(): String
    suspend fun restoreJson(json: String): Result<Unit>
    suspend fun exportCsv(table: CsvTable): String
    suspend fun deleteAllData()
}

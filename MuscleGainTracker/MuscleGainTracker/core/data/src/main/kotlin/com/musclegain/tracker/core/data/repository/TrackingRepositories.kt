package com.musclegain.tracker.core.data.repository

import androidx.room.withTransaction
import com.musclegain.tracker.core.data.MuscleGainDatabase
import com.musclegain.tracker.core.data.entity.toDb
import com.musclegain.tracker.core.data.entity.toDomain
import com.musclegain.tracker.core.data.entity.toEntity
import com.musclegain.tracker.core.domain.model.BodyWeightEntry
import com.musclegain.tracker.core.domain.model.NutritionEntry
import com.musclegain.tracker.core.domain.model.SupplementEntry
import com.musclegain.tracker.core.domain.model.WorkoutSessionWithExercises
import com.musclegain.tracker.core.domain.repository.BodyWeightRepository
import com.musclegain.tracker.core.domain.repository.NutritionRepository
import com.musclegain.tracker.core.domain.repository.SupplementRepository
import com.musclegain.tracker.core.domain.repository.WorkoutRepository
import com.musclegain.tracker.core.domain.validation.SaveOutcome
import com.musclegain.tracker.core.domain.validation.Validators
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class NutritionRepositoryImpl(database: MuscleGainDatabase) : NutritionRepository {
    private val dao = database.nutritionDao()

    override fun observeEntriesForDate(date: LocalDate): Flow<List<NutritionEntry>> =
        dao.observeForDate(date.toDb()).map { rows -> rows.map { it.toDomain() } }

    override fun observeEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<NutritionEntry>> =
        dao.observeBetween(start.toDb(), end.toDb()).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getEntry(id: Long): NutritionEntry? = dao.getById(id)?.toDomain()

    override suspend fun upsert(entry: NutritionEntry): Result<SaveOutcome> =
        saveWithValidation(Validators.nutrition(entry)) {
            if (entry.id == 0L) {
                dao.insert(entry.toEntity())
            } else {
                if (dao.update(entry.toEntity()) == 0) throw notFound()
                entry.id
            }
        }

    override suspend fun delete(id: Long): Boolean = dao.deleteById(id) > 0
}

class WorkoutRepositoryImpl(private val database: MuscleGainDatabase) : WorkoutRepository {
    private val dao = database.workoutDao()

    override fun observeSessionsForDate(date: LocalDate): Flow<List<WorkoutSessionWithExercises>> =
        dao.observeForDate(date.toDb()).map { rows -> rows.map { it.toDomain() } }

    override fun observeSessionsBetween(start: LocalDate, end: LocalDate): Flow<List<WorkoutSessionWithExercises>> =
        dao.observeBetween(start.toDb(), end.toDb()).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getSession(id: Long): WorkoutSessionWithExercises? = dao.getById(id)?.toDomain()

    override suspend fun saveSession(value: WorkoutSessionWithExercises): Result<SaveOutcome> =
        saveWithValidation(Validators.workout(value.session, value.exercises)) {
            database.withTransaction {
                val id = if (value.session.id == 0L) {
                    dao.insertSession(value.session.toEntity())
                } else {
                    if (dao.updateSession(value.session.toEntity()) == 0) throw notFound()
                    value.session.id
                }
                dao.deleteExercises(id)
                dao.insertExercises(value.exercises.map { it.copy(id = 0L, sessionId = id).toEntity() })
                id
            }
        }

    override suspend fun deleteSession(id: Long): Boolean = dao.deleteSession(id) > 0
}

class SupplementRepositoryImpl(database: MuscleGainDatabase) : SupplementRepository {
    private val dao = database.supplementDao()

    override fun observeEntriesForDate(date: LocalDate): Flow<List<SupplementEntry>> =
        dao.observeForDate(date.toDb()).map { rows -> rows.map { it.toDomain() } }

    override fun observeEntriesBetween(start: LocalDate, end: LocalDate): Flow<List<SupplementEntry>> =
        dao.observeBetween(start.toDb(), end.toDb()).map { rows -> rows.map { it.toDomain() } }

    override fun observeKnownNames(): Flow<List<String>> = dao.observeNames()

    override suspend fun upsert(entry: SupplementEntry): Result<SaveOutcome> =
        saveWithValidation(Validators.supplement(entry)) {
            if (entry.id == 0L) {
                dao.insert(entry.toEntity())
            } else {
                if (dao.update(entry.toEntity()) == 0) throw notFound()
                entry.id
            }
        }

    override suspend fun delete(id: Long): Boolean = dao.deleteById(id) > 0
}

class BodyWeightRepositoryImpl(private val database: MuscleGainDatabase) : BodyWeightRepository {
    private val dao = database.bodyWeightDao()

    override fun observeForDate(date: LocalDate): Flow<BodyWeightEntry?> =
        dao.observeForDate(date.toDb()).map { it?.toDomain() }

    override fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<BodyWeightEntry>> =
        dao.observeBetween(start.toDb(), end.toDb()).map { rows -> rows.map { it.toDomain() } }

    override fun observeRecent(limit: Int): Flow<List<BodyWeightEntry>> =
        dao.observeRecent(limit).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getLatest(): BodyWeightEntry? = dao.getLatest()?.toDomain()

    override suspend fun getLatestOnOrBefore(date: LocalDate): BodyWeightEntry? =
        dao.getLatestOnOrBefore(date.toDb())?.toDomain()

    override suspend fun upsert(entry: BodyWeightEntry): Result<SaveOutcome> =
        saveWithValidation(Validators.bodyWeight(entry.weightKg)) {
            database.withTransaction {
                val sameDay = dao.getByDate(entry.date.toDb())
                when {
                    sameDay != null && sameDay.id != entry.id -> {
                        if (entry.id != 0L) dao.deleteById(entry.id)
                        dao.update(entry.copy(id = sameDay.id).toEntity())
                        sameDay.id
                    }
                    entry.id == 0L -> dao.insert(entry.toEntity())
                    else -> {
                        if (dao.update(entry.toEntity()) == 0) throw notFound()
                        entry.id
                    }
                }
            }
        }

    override suspend fun delete(id: Long): Boolean = dao.deleteById(id) > 0
}

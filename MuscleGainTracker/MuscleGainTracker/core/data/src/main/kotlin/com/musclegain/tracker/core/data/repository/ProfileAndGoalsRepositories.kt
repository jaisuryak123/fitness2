package com.musclegain.tracker.core.data.repository

import com.musclegain.tracker.core.data.dao.GoalsDao
import com.musclegain.tracker.core.data.dao.ReminderDao
import com.musclegain.tracker.core.data.dao.TrainingScheduleDao
import com.musclegain.tracker.core.data.dao.UserProfileDao
import com.musclegain.tracker.core.data.entity.UserProfileEntity
import com.musclegain.tracker.core.data.entity.toDomain
import com.musclegain.tracker.core.data.entity.toEntity
import com.musclegain.tracker.core.domain.model.DailyGoals
import com.musclegain.tracker.core.domain.model.ReminderSettings
import com.musclegain.tracker.core.domain.model.TrainingDay
import com.musclegain.tracker.core.domain.model.UserProfile
import com.musclegain.tracker.core.domain.model.UserProfileInput
import com.musclegain.tracker.core.domain.model.effectiveOn
import com.musclegain.tracker.core.domain.repository.GoalsRepository
import com.musclegain.tracker.core.domain.repository.ReminderRepository
import com.musclegain.tracker.core.domain.repository.TrainingScheduleRepository
import com.musclegain.tracker.core.domain.repository.UserProfileRepository
import com.musclegain.tracker.core.domain.validation.SaveOutcome
import com.musclegain.tracker.core.domain.validation.Validators
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class UserProfileRepositoryImpl(private val dao: UserProfileDao) : UserProfileRepository {
    override fun observeProfile(): Flow<UserProfile?> = dao.observe().map { it?.toDomain() }

    override suspend fun getProfile(): UserProfile? = dao.get()?.toDomain()

    override suspend fun saveProfile(input: UserProfileInput): Result<SaveOutcome> =
        saveWithValidation(Validators.profile(input)) {
            val now = System.currentTimeMillis()
            val existing = dao.get()
            dao.upsert(
                UserProfileEntity(
                    id = 1L,
                    heightCm = input.heightCm,
                    weightKg = input.weightKg,
                    age = input.age,
                    goal = input.goal.name,
                    createdAtMillis = existing?.createdAtMillis ?: now,
                    updatedAtMillis = now
                )
            )
            1L
        }
}

class GoalsRepositoryImpl(private val dao: GoalsDao) : GoalsRepository {
    override fun observeAllGoals(): Flow<List<DailyGoals>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeGoalsFor(date: LocalDate): Flow<DailyGoals> = observeAllGoals().map { it.effectiveOn(date) }

    override suspend fun getGoalsFor(date: LocalDate): DailyGoals = dao.getAll().map { it.toDomain() }.effectiveOn(date)

    override suspend fun saveGoals(goals: DailyGoals): Result<SaveOutcome> =
        saveWithValidation(Validators.goals(goals)) {
            val existing = dao.getByDate(goals.effectiveDate.toString())
            if (existing != null) {
                dao.update(goals.copy(id = existing.id).toEntity())
                existing.id
            } else {
                dao.insert(goals.copy(id = 0L).toEntity())
            }
        }
}

class TrainingScheduleRepositoryImpl(private val dao: TrainingScheduleDao) : TrainingScheduleRepository {
    override fun observeSchedule(): Flow<List<TrainingDay>> = dao.observeAll().map { rows ->
        if (rows.size == 7) rows.map { it.toDomain() } else TrainingDay.defaultWeek()
    }

    override suspend fun saveSchedule(days: List<TrainingDay>): Result<SaveOutcome> =
        saveWithValidation(Validators.schedule(days)) {
            dao.upsertAll(days.map { it.toEntity() })
            0L
        }
}

class ReminderRepositoryImpl(private val dao: ReminderDao) : ReminderRepository {
    override fun observeSettings(): Flow<ReminderSettings> = dao.observe().map { it?.toDomain() ?: ReminderSettings() }

    override suspend fun saveSettings(settings: ReminderSettings) = dao.upsert(settings.toEntity())
}

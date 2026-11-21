package com.musclegain.tracker.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.musclegain.tracker.core.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appPreferencesStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferencesRepositoryImpl(context: Context) : AppPreferencesRepository {
    private val store = context.applicationContext.appPreferencesStore

    override val onboardingCompleted: Flow<Boolean> = store.data.map { it[ONBOARDING_COMPLETED] ?: false }

    override val lastBackupAtMillis: Flow<Long?> = store.data.map { it[LAST_BACKUP_AT] }

    override suspend fun setOnboardingCompleted(value: Boolean) {
        store.edit { it[ONBOARDING_COMPLETED] = value }
    }

    override suspend fun setLastBackupAtMillis(value: Long) {
        store.edit { it[LAST_BACKUP_AT] = value }
    }

    private companion object {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LAST_BACKUP_AT = longPreferencesKey("last_backup_at")
    }
}

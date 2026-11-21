package com.musclegain.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musclegain.tracker.core.domain.model.CsvTable
import com.musclegain.tracker.core.domain.repository.AppPreferencesRepository
import com.musclegain.tracker.core.domain.repository.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BackupViewModel(
    private val backupRepository: BackupRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val lastBackupAtMillis: StateFlow<Long?> = appPreferencesRepository.lastBackupAtMillis
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _exportedContent = MutableStateFlow<String?>(null)
    val exportedContent: StateFlow<String?> = _exportedContent.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun exportJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = backupRepository.exportJson()
                appPreferencesRepository.setLastBackupAtMillis(System.currentTimeMillis())
                _exportedContent.value = json
                _statusMessage.value = "Backup JSON generated successfully"
                onResult(json)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to export JSON: ${e.message}"
            }
        }
    }

    fun restoreJson(json: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = backupRepository.restoreJson(json)
            result.fold(
                onSuccess = {
                    _statusMessage.value = "Data restored successfully from backup"
                    onSuccess()
                },
                onFailure = { error ->
                    _errorMessage.value = "Restore failed: ${error.message}"
                }
            )
        }
    }

    fun exportCsv(table: CsvTable, onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val csv = backupRepository.exportCsv(table)
                onResult(csv)
                _statusMessage.value = "CSV exported for ${table.name}"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to export CSV: ${e.message}"
            }
        }
    }

    fun deleteAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            backupRepository.deleteAllData()
            _statusMessage.value = "All data cleared successfully"
            onComplete()
        }
    }

    fun clearMessages() {
        _statusMessage.value = null
        _errorMessage.value = null
    }
}

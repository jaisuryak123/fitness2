package com.musclegain.tracker.core.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.musclegain.tracker.core.domain.validation.SaveOutcome
import com.musclegain.tracker.core.domain.validation.Severity
import com.musclegain.tracker.core.domain.validation.ValidationException
import com.musclegain.tracker.core.domain.validation.ValidationIssue

internal fun notFound() = ValidationException(
    listOf(ValidationIssue("id", "NOT_FOUND", "Record does not exist", Severity.ERROR))
)

internal inline fun saveWithValidation(issues: List<ValidationIssue>, block: () -> Long): Result<SaveOutcome> {
    val errors = issues.filter { it.severity == Severity.ERROR }
    if (errors.isNotEmpty()) return Result.failure(ValidationException(errors))
    return try {
        Result.success(SaveOutcome(block(), issues))
    } catch (e: ValidationException) {
        Result.failure(e)
    } catch (e: SQLiteConstraintException) {
        Result.failure(
            ValidationException(
                listOf(ValidationIssue("record", "DUPLICATE_RECORD", "An identical record already exists", Severity.ERROR))
            )
        )
    }
}

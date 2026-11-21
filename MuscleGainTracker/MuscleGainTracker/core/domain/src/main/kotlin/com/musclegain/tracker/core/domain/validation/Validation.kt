package com.musclegain.tracker.core.domain.validation

enum class Severity { ERROR, WARNING }

data class ValidationIssue(
    val field: String,
    val code: String,
    val message: String,
    val severity: Severity = Severity.ERROR
)

class ValidationException(val issues: List<ValidationIssue>) :
    Exception(issues.joinToString("; ") { it.message })

data class SaveOutcome(
    val id: Long,
    val warnings: List<ValidationIssue> = emptyList()
)

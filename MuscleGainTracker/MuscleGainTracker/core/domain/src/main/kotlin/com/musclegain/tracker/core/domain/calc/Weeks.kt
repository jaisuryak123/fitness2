package com.musclegain.tracker.core.domain.calc

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

object Weeks {
    fun startOf(date: LocalDate): LocalDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    fun endOf(date: LocalDate): LocalDate = startOf(date).plusDays(6)

    fun datesOf(weekStart: LocalDate): List<LocalDate> = (0L..6L).map { weekStart.plusDays(it) }
}

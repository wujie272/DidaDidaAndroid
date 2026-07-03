package com.jaye.didadida.domain

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class SettingsConfig(
    val standardHoursPerDay: Double = 8.0,
    val breakRules: List<BreakRule> = listOf(
        BreakRule("午餐", LocalTime(12, 0), LocalTime(13, 0)),
        BreakRule("晚餐", LocalTime(18, 0), LocalTime(18, 30)),
    ),
    val hourlyRate: Double = 0.0,
    val overtimeRate: Double = 1.5,
)

@Serializable
data class BreakRule(
    val label: String,
    val start: LocalTime,
    val end: LocalTime,
) {
    val durationMinutes: Int get() {
        val s = start.toSecondOfDay()
        val e = end.toSecondOfDay()
        return (e - s) / 60
    }
}

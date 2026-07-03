package com.jaye.didadida.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class WorkLog(
    val id: String,
    val date: LocalDate,
    val clockIn: LocalTime? = null,
    val clockOut: LocalTime? = null,
    val note: String = "",
) {
    companion object {
        fun today(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
}

@Serializable
data class DailySummary(
    val date: LocalDate,
    val clockIn: LocalTime?,
    val clockOut: LocalTime?,
    val effectiveHours: Double,   // 有效工时（已扣休息）
    val overtimeHours: Double,    // 加班时长
    val rawHours: Double,         // 打卡总时长
    val note: String,
)

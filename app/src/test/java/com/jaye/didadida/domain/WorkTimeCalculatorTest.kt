package com.jaye.didadida.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.*
import org.junit.Test

class WorkTimeCalculatorTest {

    private val defaultSettings = SettingsConfig(
        standardHoursPerDay = 8.0,
        breakRules = listOf(
            BreakRule("午餐", LocalTime(12, 0), LocalTime(13, 0)),
            BreakRule("晚餐", LocalTime(18, 0), LocalTime(18, 30)),
        ),
    )

    private val refDate = LocalDate(2026, 7, 4)

    @Test
    fun `正常上班下班 9点到18点`() {
        val summary = WorkTimeCalculator.calculate(
            refDate, LocalTime(9, 0), LocalTime(18, 0), defaultSettings
        )
        assertEquals(9.0, summary.rawHours, 0.01)
        assertEquals(0.0, summary.overtimeHours, 0.01)
    }

    @Test
    fun `加班 - 9点到20点`() {
        val summary = WorkTimeCalculator.calculate(
            refDate, LocalTime(9, 0), LocalTime(20, 0), defaultSettings
        )
        // raw = 11h = 660min
        // break = lunch 60 + dinner 30 = 90min
        // effective = 660-90 = 570min = 9.5h
        // overtime = 9.5 - 8 = 1.5h
        assertEquals(9.5, summary.effectiveHours, 0.01)
        assertEquals(1.5, summary.overtimeHours, 0.01)
    }

    @Test
    fun `未打卡返回零`() {
        val summary = WorkTimeCalculator.calculate(refDate, null, null, defaultSettings)
        assertEquals(0.0, summary.effectiveHours, 0.01)
        assertEquals(0.0, summary.overtimeHours, 0.01)
    }

    @Test
    fun `只打卡上班未打卡下班`() {
        val summary = WorkTimeCalculator.calculate(refDate, LocalTime(9, 0), null, defaultSettings)
        assertEquals(0.0, summary.effectiveHours, 0.01)
    }

    @Test
    fun `跨天打卡 22点到次日2点`() {
        val summary = WorkTimeCalculator.calculate(
            refDate, LocalTime(22, 0), LocalTime(2, 0), defaultSettings
        )
        // raw = 4h (22->02 跨天), no break overlap
        assertEquals(4.0, summary.effectiveHours, 0.01)
        assertEquals(0.0, summary.overtimeHours, 0.01)
    }

    @Test
    fun `午休完全在打卡区间内`() {
        val minutes = WorkTimeCalculator.totalBreakMinutes(
            LocalTime(9, 0), LocalTime(18, 0),
            listOf(BreakRule("午餐", LocalTime(12, 0), LocalTime(13, 0)))
        )
        assertEquals(60, minutes)
    }

    @Test
    fun `午休不在区间内`() {
        val minutes = WorkTimeCalculator.totalBreakMinutes(
            LocalTime(9, 0), LocalTime(11, 0),
            listOf(BreakRule("午餐", LocalTime(12, 0), LocalTime(13, 0)))
        )
        assertEquals(0, minutes)
    }

    @Test
    fun `加班费计算`() {
        val summaries = listOf(
            DailySummary(refDate, LocalTime(9,0), LocalTime(18,0), 8.0, 0.0, 9.0, ""),
            DailySummary(refDate, LocalTime(9,0), LocalTime(20,0), 9.5, 1.5, 11.0, ""),
        )
        val settings = SettingsConfig(hourlyRate = 50.0, overtimeRate = 1.5)
        val pay = WorkTimeCalculator.estimateOvertimePay(summaries, settings)
        assertEquals(112.5, pay, 0.01)
    }

    @Test
    fun `时薪为零时不计算加班费`() {
        val summaries = listOf(
            DailySummary(refDate, LocalTime(9,0), LocalTime(20,0), 9.5, 1.5, 11.0, ""),
        )
        val settings = SettingsConfig(hourlyRate = 0.0, overtimeRate = 1.5)
        val pay = WorkTimeCalculator.estimateOvertimePay(summaries, settings)
        assertEquals(0.0, pay, 0.01)
    }

    @Test
    fun `minutesBetween 正常`() {
        val m = WorkTimeCalculator.minutesBetween(LocalTime(9, 0), LocalTime(12, 30))
        assertEquals(210, m)
    }

    @Test
    fun `minutesBetween 跨天`() {
        val m = WorkTimeCalculator.minutesBetween(LocalTime(22, 0), LocalTime(2, 0))
        assertEquals(240, m)
    }
}

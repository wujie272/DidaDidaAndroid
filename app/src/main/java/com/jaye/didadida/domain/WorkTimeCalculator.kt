package com.jaye.didadida.domain

import kotlinx.datetime.*

object WorkTimeCalculator {

    /**
     * 计算有效工时和加班时长
     */
    fun calculate(date: LocalDate, clockIn: LocalTime?, clockOut: LocalTime?, settings: SettingsConfig): DailySummary {
        if (clockIn == null || clockOut == null) {
            return DailySummary(date, null, null, 0.0, 0.0, 0.0)
        }

        val rawMinutes = minutesBetween(clockIn, clockOut)
        val breakMinutes = totalBreakMinutes(clockIn, clockOut, settings.breakRules)
        val effectiveMinutes = (rawMinutes - breakMinutes).coerceAtLeast(0)
        val effectiveHours = effectiveMinutes / 60.0
        val rawHours = rawMinutes / 60.0

        val standardMinutes = (settings.standardHoursPerDay * 60).toInt()
        val overtimeMinutes = (effectiveMinutes - standardMinutes).coerceAtLeast(0)
        val overtimeHours = overtimeMinutes / 60.0

        return DailySummary(
            date = date,
            clockIn = clockIn,
            clockOut = clockOut,
            effectiveHours = effectiveHours,
            overtimeHours = overtimeHours,
            rawHours = rawHours,
        )
    }

    /**
     * 计算两个时间之间的分钟差（支持跨天，如 22:00 → 02:00 = 4h）
     */
    fun minutesBetween(start: LocalTime, end: LocalTime): Int {
        val s = start.toSecondOfDay()
        val e = end.toSecondOfDay()
        return if (e >= s) (e - s) / 60
        else (24 * 3600 - s + e) / 60  // 跨天
    }

    /**
     * 计算在打卡区间内实际生效的休息分钟数。
     * 支持跨天打卡和跨天休息规则。
     *
     * 思路：将打卡区间和休息区间都映射到 [0, 48h) 的时间线上，
     * 休息规则如果 end <= start 则视为跨天（加24h）。
     * 打卡区间跨天的话也加24h。然后算重叠。
     */
    fun totalBreakMinutes(
        clockIn: LocalTime,
        clockOut: LocalTime,
        breakRules: List<BreakRule>,
    ): Int {
        val daySeconds = 24 * 3600
        val inSec = clockIn.toSecondOfDay()
        val outSec = clockOut.toSecondOfDay()

        // 把打卡区间标准化到 [0, 48h) 范围
        val workStart = inSec.toLong()
        val workEnd = if (outSec >= inSec) outSec.toLong() else outSec.toLong() + daySeconds

        return breakRules.sumOf { rule ->
            var rStart = rule.start.toSecondOfDay().toLong()
            var rEnd = rule.end.toSecondOfDay().toLong()

            // 如果休息结束 <= 开始，视为跨天
            if (rEnd <= rStart) rEnd += daySeconds

            // 把休息区间复制一份到下一个周期，确保和跨天打卡区间重叠
            var totalOverlap = 0L
            for (shift in 0L..daySeconds step daySeconds.toLong()) {
                val rs = rStart + shift
                val re = rEnd + shift
                val overlapStart = maxOf(workStart, rs)
                val overlapEnd = minOf(workEnd, re)
                if (overlapEnd > overlapStart) {
                    totalOverlap += overlapEnd - overlapStart
                }
            }
            (totalOverlap / 60).toInt()
        }
    }

    /**
     * 估算当月加班费
     */
    fun estimateOvertimePay(summaries: List<DailySummary>, settings: SettingsConfig): Double {
        if (settings.hourlyRate <= 0) return 0.0
        val totalOvertime = summaries.sumOf { it.overtimeHours }
        return totalOvertime * settings.hourlyRate * settings.overtimeRate
    }
}

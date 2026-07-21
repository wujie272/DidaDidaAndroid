package com.jaye.didadida.data

import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import com.jaye.didadida.domain.*

class WorkLogRepository(private val storage: WorkLogStorage) {

    fun allWorkLogs(): Flow<Map<String, WorkLog>> = storage.allWorkLogsFlow()

    fun settings(): Flow<SettingsConfig> = storage.settingsFlow()

    suspend fun getWorkLog(date: LocalDate): WorkLog? = storage.getWorkLog(date)

    suspend fun clockIn(date: LocalDate, time: LocalTime? = null) {
        val t = time ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        val existing = storage.getWorkLog(date)
        val log = existing?.copy(clockIn = t)
            ?: WorkLog(id = date.toString(), date = date, clockIn = t)
        storage.saveWorkLog(log)
    }

    suspend fun clockOut(date: LocalDate, time: LocalTime? = null) {
        val t = time ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        val existing = storage.getWorkLog(date)
        val log = existing?.copy(clockOut = t)
            ?: WorkLog(id = date.toString(), date = date, clockOut = t)
        storage.saveWorkLog(log)
    }

    suspend fun saveNote(date: LocalDate, note: String) {
        val existing = storage.getWorkLog(date)
        val log = existing?.copy(note = note)
            ?: WorkLog(id = date.toString(), date = date, note = note)
        storage.saveWorkLog(log)
    }

    suspend fun deleteWorkLog(date: LocalDate) = storage.deleteWorkLog(date)

    suspend fun saveSettings(config: SettingsConfig) = storage.saveSettings(config)
    suspend fun loadSettings(): SettingsConfig = storage.loadSettings()

    /**
     * 获取某个月的每日汇总
     */
    suspend fun monthlySummaries(year: Int, month: Int, settings: SettingsConfig): List<DailySummary> {
        val logs = storage.loadAllWorkLogs()
        return logs.values
            .filter { it.date.year == year && it.date.monthNumber == month }
            .sortedBy { it.date.toEpochDays() }
            .map { WorkTimeCalculator.calculate(it.date, it.clockIn, it.clockOut, settings) }
    }

    /**
     * 导出所有数据为 JSON 字符串
     */
    suspend fun exportAllData(): String = storage.exportAllData()

    suspend fun importAllData(jsonString: String): Boolean = storage.importAllData(jsonString)

    /**
     * 自动填今天：用标准工时自动生成打卡记录
     */
    suspend fun autoFillToday(settings: SettingsConfig) {
        val today = WorkLog.today()
        val existing = storage.getWorkLog(today)
        if (existing?.clockIn != null && existing.clockOut != null) return  // 已完整打卡，跳过

        val defaultIn = settings.defaultStartTime
        val totalBreak = settings.breakRules.sumOf { it.durationMinutes }
        val workSeconds = (settings.standardHoursPerDay * 3600).toInt() + totalBreak * 60
        val outSecond = defaultIn.toSecondOfDay() + workSeconds
        val defaultOut = if (outSecond < 86400) {
            LocalTime.fromSecondOfDay(outSecond)
        } else {
            LocalTime.fromSecondOfDay(outSecond - 86400)
        }

        val log = WorkLog(
            id = today.toString(),
            date = today,
            clockIn = existing?.clockIn ?: defaultIn,
            clockOut = existing?.clockOut ?: defaultOut,
            note = existing?.note ?: "",
        )
        storage.saveWorkLog(log)
    }
}

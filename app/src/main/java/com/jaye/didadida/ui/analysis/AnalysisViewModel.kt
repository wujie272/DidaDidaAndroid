package com.jaye.didadida.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaye.didadida.data.WorkLogRepository
import com.jaye.didadida.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class AnalysisViewModel(
    private val repository: WorkLogRepository
) : ViewModel() {

    data class UiState(
        val year: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year,
        val month: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber,
        val summaries: List<DailySummary> = emptyList(),
        val settings: SettingsConfig = SettingsConfig(),
        val stats: MonthlyStats = MonthlyStats(),
    )

    data class MonthlyStats(
        val totalDays: Int = 0,
        val totalEffectiveHours: Double = 0.0,
        val totalOvertimeHours: Double = 0.0,
        val avgDailyHours: Double = 0.0,
        val overtimePay: Double = 0.0,
        val maxDay: Double = 0.0,
        val minDay: Double = 0.0,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.allWorkLogs(),
                repository.settings(),
            ) { _, s -> s }
                .collect { settings ->
                    refresh(settings)
                }
        }
    }

    private suspend fun refresh(settings: SettingsConfig) {
        val s = _state.value
        val summaries = repository.monthlySummaries(s.year, s.month, settings)
        val stats = computeStats(summaries, settings)
        _state.value = s.copy(summaries = summaries, stats = stats, settings = settings)
    }

    fun selectMonth(year: Int, month: Int) {
        _state.update { it.copy(year = year, month = month) }
        viewModelScope.launch {
            val s = _state.value
            val settings = repository.loadSettings()
            refresh(settings)
        }
    }

    private fun computeStats(summaries: List<DailySummary>, settings: SettingsConfig): MonthlyStats {
        val worked = summaries.filter { it.clockIn != null && it.clockOut != null }
        if (worked.isEmpty()) return MonthlyStats()

        val totalEffective = worked.sumOf { it.effectiveHours }
        val totalOvertime = worked.sumOf { it.overtimeHours }
        val avg = if (worked.isNotEmpty()) totalEffective / worked.size else 0.0
        val maxDay = worked.maxOf { it.effectiveHours }
        val minDay = worked.minOf { it.effectiveHours }
        val pay = WorkTimeCalculator.estimateOvertimePay(worked, settings)

        return MonthlyStats(
            totalDays = worked.size,
            totalEffectiveHours = totalEffective,
            totalOvertimeHours = totalOvertime,
            avgDailyHours = avg,
            overtimePay = pay,
            maxDay = maxDay,
            minDay = minDay,
        )
    }
}

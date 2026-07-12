package com.jaye.didadida.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaye.didadida.data.WorkLogRepository
import com.jaye.didadida.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class TodayViewModel(
    private val repository: WorkLogRepository
) : ViewModel() {

    // 下班撒花触发器：每次下班打卡成功 +1
    private val _confettiTrigger = MutableStateFlow(0)
    val confettiTrigger: StateFlow<Int> = _confettiTrigger.asStateFlow()

    data class UiState(
        val todayLog: WorkLog? = null,
        val settings: SettingsConfig = SettingsConfig(),
        val summary: DailySummary? = null,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allWorkLogs().combine(repository.settings()) { logs, settings ->
                val today = WorkLog.today()
                val log = logs[today.toString()]
                val summary = if (log != null) {
                    WorkTimeCalculator.calculate(log.date, log.clockIn, log.clockOut, settings)
                } else null
                UiState(todayLog = log, settings = settings, summary = summary)
            }.collect { _state.value = it }
        }
    }

    fun clockIn() {
        viewModelScope.launch {
            repository.clockIn(WorkLog.today())
        }
    }

    fun clockOut() {
        viewModelScope.launch {
            repository.clockOut(WorkLog.today())
            _confettiTrigger.value = _confettiTrigger.value + 1
        }
    }

    fun autoFill() {
        viewModelScope.launch {
            val s = repository.loadSettings()
            repository.autoFillToday(s)
        }
    }

    fun updateNote(note: String) {
        viewModelScope.launch {
            repository.saveNote(WorkLog.today(), note)
        }
    }

    fun deleteToday() {
        viewModelScope.launch {
            repository.deleteWorkLog(WorkLog.today())
        }
    }
}

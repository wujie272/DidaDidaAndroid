package com.jaye.didadida.ui.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaye.didadida.data.WorkLogRepository
import com.jaye.didadida.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class RecordsViewModel(
    private val repository: WorkLogRepository
) : ViewModel() {

    data class UiState(
        val year: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year,
        val month: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber,
        val summaries: List<DailySummary> = emptyList(),
        val settings: SettingsConfig = SettingsConfig(),
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.allWorkLogs(),
                repository.settings(),
            ) { _, settings -> settings }
                .collect { settings ->
                    val s = _state.value
                    val summaries = repository.monthlySummaries(s.year, s.month, settings)
                    _state.value = s.copy(summaries = summaries, settings = settings)
                }
        }
    }

    fun previousMonth() {
        _state.update { s ->
            val m = if (s.month == 1) 12 else s.month - 1
            val y = if (s.month == 1) s.year - 1 else s.year
            s.copy(year = y, month = m)
        }
        reload()
    }

    fun nextMonth() {
        _state.update { s ->
            val m = if (s.month == 12) 1 else s.month + 1
            val y = if (s.month == 12) s.year + 1 else s.year
            s.copy(year = y, month = m)
        }
        reload()
    }

    private fun reload() {
        viewModelScope.launch {
            val s = _state.value
            val settings = repository.loadSettings()
            val summaries = repository.monthlySummaries(s.year, s.month, settings)
            _state.value = s.copy(summaries = summaries)
        }
    }
}

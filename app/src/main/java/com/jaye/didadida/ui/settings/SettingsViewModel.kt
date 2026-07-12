package com.jaye.didadida.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaye.didadida.data.WorkLogRepository
import com.jaye.didadida.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

class SettingsViewModel(
    private val repository: WorkLogRepository
) : ViewModel() {

    // 撒花预览触发器
    private val _previewTrigger = MutableStateFlow(0)
    val previewTrigger: StateFlow<Int> = _previewTrigger.asStateFlow()

    fun triggerConfettiPreview() {
        _previewTrigger.value = _previewTrigger.value + 1
    }

    private val _settings = MutableStateFlow(SettingsConfig())
    val settings: StateFlow<SettingsConfig> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settings().collect { _settings.value = it }
        }
    }

    fun updateStandardHours(hours: Double) {
        viewModelScope.launch {
            val s = _settings.value.copy(standardHoursPerDay = hours)
            _settings.value = s
            repository.saveSettings(s)
        }
    }

    fun updateBreakRule(index: Int, label: String, startHour: Int, startMin: Int, endHour: Int, endMin: Int) {
        viewModelScope.launch {
            val rules = _settings.value.breakRules.toMutableList()
            if (index in rules.indices) {
                rules[index] = BreakRule(
                    label = label,
                    start = LocalTime(startHour, startMin),
                    end = LocalTime(endHour, endMin),
                )
                val s = _settings.value.copy(breakRules = rules)
                _settings.value = s
                repository.saveSettings(s)
            }
        }
    }

    fun updateHourlyRate(rate: Double) {
        viewModelScope.launch {
            val s = _settings.value.copy(hourlyRate = rate)
            _settings.value = s
            repository.saveSettings(s)
        }
    }

    fun updateDefaultStartTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            val s = _settings.value.copy(defaultStartTime = LocalTime(hour, minute))
            _settings.value = s
            repository.saveSettings(s)
        }
    }

    fun exportData(block: (String) -> Unit) {
        viewModelScope.launch {
            val data = repository.exportAllData()
            block(data)
        }
    }

    fun importData(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.importAllData(jsonString)
            onResult(ok)
        }
    }

    fun updateOvertimeRate(rate: Double) {
        viewModelScope.launch {
            val s = _settings.value.copy(overtimeRate = rate)
            _settings.value = s
            repository.saveSettings(s)
        }
    }
}

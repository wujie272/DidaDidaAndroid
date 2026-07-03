package com.jaye.didadida.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("设置") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // 标准工时
            SectionTitle("标准工时")
            NumberField(
                label = "每日标准工时（小时）",
                value = settings.standardHoursPerDay,
                onValueChange = { viewModel.updateStandardHours(it.coerceIn(1.0, 24.0)) },
            )

            // 休息规则
            SectionTitle("休息扣除规则")
            settings.breakRules.forEachIndexed { index, rule ->
                BreakRuleEditor(
                    index = index,
                    rule = rule,
                    onUpdate = { label, sh, sm, eh, em ->
                        viewModel.updateBreakRule(index, label, sh, sm, eh, em)
                    },
                )
            }

            // 薪资
            SectionTitle("薪资与加班费")
            NumberField(
                label = "时薪（元）",
                value = settings.hourlyRate,
                onValueChange = { viewModel.updateHourlyRate(it.coerceAtLeast(0.0)) },
                prefix = "¥",
            )
            NumberField(
                label = "加班倍率",
                value = settings.overtimeRate,
                onValueChange = { viewModel.updateOvertimeRate(it.coerceAtLeast(1.0)) },
                suffix = "x",
            )

            Spacer(Modifier.height(16.dp))

            // 数据说明
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            ) {
                Text(
                    "所有数据仅存储在本地设备上，不会上传到任何服务器。\n" +
                            "支持备份：复制 /data/data/com.jaye.didadida/files/datastore/didadida.preferences_pb",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
fun NumberField(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    prefix: String = "",
    suffix: String = "",
) {
    var text by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            text = input
            input.toDoubleOrNull()?.let { onValueChange(it) }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        prefix = if (prefix.isNotEmpty()) {{ Text(prefix) }} else null,
        suffix = if (suffix.isNotEmpty()) {{ Text(suffix) }} else null,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun BreakRuleEditor(
    index: Int,
    rule: com.jaye.didadida.domain.BreakRule,
    onUpdate: (String, Int, Int, Int, Int) -> Unit,
) {
    var label by remember(rule) { mutableStateOf(rule.label) }
    var startHour by remember(rule) { mutableStateOf(rule.start.hour.toString()) }
    var startMin by remember(rule) { mutableStateOf(rule.start.minute.toString().padStart(2, '0')) }
    var endHour by remember(rule) { mutableStateOf(rule.end.hour.toString()) }
    var endMin by remember(rule) { mutableStateOf(rule.end.minute.toString().padStart(2, '0')) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it
                    onUpdate(it, startHour.toIntOrNull() ?: 0, startMin.toIntOrNull() ?: 0,
                        endHour.toIntOrNull() ?: 0, endMin.toIntOrNull() ?: 0) },
                label = { Text("名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = startHour,
                    onValueChange = { startHour = it
                        onUpdate(label, it.toIntOrNull() ?: 0, startMin.toIntOrNull() ?: 0,
                            endHour.toIntOrNull() ?: 0, endMin.toIntOrNull() ?: 0) },
                    label = { Text("开始(H)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Text(":")
                OutlinedTextField(
                    value = startMin,
                    onValueChange = { startMin = it
                        onUpdate(label, startHour.toIntOrNull() ?: 0, it.toIntOrNull() ?: 0,
                            endHour.toIntOrNull() ?: 0, endMin.toIntOrNull() ?: 0) },
                    label = { Text("开始(M)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = endHour,
                    onValueChange = { endHour = it
                        onUpdate(label, startHour.toIntOrNull() ?: 0, startMin.toIntOrNull() ?: 0,
                            it.toIntOrNull() ?: 0, endMin.toIntOrNull() ?: 0) },
                    label = { Text("结束(H)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Text(":")
                OutlinedTextField(
                    value = endMin,
                    onValueChange = { endMin = it
                        onUpdate(label, startHour.toIntOrNull() ?: 0, startMin.toIntOrNull() ?: 0,
                            endHour.toIntOrNull() ?: 0, it.toIntOrNull() ?: 0) },
                    label = { Text("结束(M)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

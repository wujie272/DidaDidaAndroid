package com.jaye.didadida.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            SectionTitle("默认上班时间")
            DefaultStartTimeEditor(
                time = settings.defaultStartTime,
                onUpdate = { h, m -> viewModel.updateDefaultStartTime(h, m) },
            )

            SectionTitle("标准工时")
            NumberField(
                label = "每日标准工时（小时）",
                value = settings.standardHoursPerDay,
                onValueChange = { viewModel.updateStandardHours(it.coerceIn(1.0, 24.0)) },
            )

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

            SectionTitle("数据管理")
            val clipboardManager = LocalClipboardManager.current
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "所有数据仅存储在本地设备上。导出为 JSON 后可复制到剪贴板，导入时从剪贴板读取恢复。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {
                            viewModel.exportData { data ->
                                clipboardManager.setText(AnnotatedString(data))
                                scope.launch { snackbarHostState.showSnackbar("数据已复制到剪贴板") }
                            }
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("导出到剪贴板")
                        }
                        OutlinedButton(onClick = {
                            val text = clipboardManager.getText()?.text
                            if (text != null) {
                                viewModel.importData(text) { ok ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (ok) "数据恢复成功" else "数据格式错误，恢复失败"
                                        )
                                    }
                                }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("剪贴板为空") }
                            }
                        }) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("从剪贴板导入")
                        }
                    }
                }
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

    fun clamp(v: String, max: Int): Int = v.toIntOrNull()?.coerceIn(0, max) ?: 0

    var startHour by remember(rule) { mutableStateOf(rule.start.hour.toString()) }
    var startMin by remember(rule) { mutableStateOf(rule.start.minute.toString().padStart(2, '0')) }
    var endHour by remember(rule) { mutableStateOf(rule.end.hour.toString()) }
    var endMin by remember(rule) { mutableStateOf(rule.end.minute.toString().padStart(2, '0')) }

    val fire = { onUpdate(label, clamp(startHour,23), clamp(startMin,59), clamp(endHour,23), clamp(endMin,59)) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it; fire() },
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
                    onValueChange = { startHour = it; fire() },
                    label = { Text("开始(H)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Text(":")
                OutlinedTextField(
                    value = startMin,
                    onValueChange = { startMin = it; fire() },
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
                    onValueChange = { endHour = it; fire() },
                    label = { Text("结束(H)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Text(":")
                OutlinedTextField(
                    value = endMin,
                    onValueChange = { endMin = it; fire() },
                    label = { Text("结束(M)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun DefaultStartTimeEditor(
    time: kotlinx.datetime.LocalTime,
    onUpdate: (Int, Int) -> Unit,
) {
    var hour by remember(time) { mutableStateOf(time.hour.toString()) }
    var minute by remember(time) { mutableStateOf(time.minute.toString().padStart(2, '0')) }

    fun fire() {
        val h = hour.toIntOrNull()?.coerceIn(0, 23) ?: 0
        val m = minute.toIntOrNull()?.coerceIn(0, 59) ?: 0
        onUpdate(h, m)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = hour,
                onValueChange = { hour = it; fire() },
                label = { Text("时") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Text(":")
            OutlinedTextField(
                value = minute,
                onValueChange = { minute = it; fire() },
                label = { Text("分") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

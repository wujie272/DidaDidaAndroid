package com.jaye.didadida.ui.today

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jaye.didadida.ui.common.ConfettiCelebration
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToSettings: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val trigger by viewModel.confettiTrigger.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("今日") },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "设置")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))

                // 进度环
                ProgressRing(
                    progress = (state.summary?.effectiveHours ?: 0.0) / state.settings.standardHoursPerDay,
                    hours = state.summary?.effectiveHours ?: 0.0,
                    totalHours = state.settings.standardHoursPerDay,
                )

                Spacer(Modifier.height(8.dp))

                // 有效工时 / 加班
                if (state.summary != null) {
                    Text(
                        "加班 ${"%.1f".format(state.summary!!.overtimeHours)}h",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }

                Spacer(Modifier.height(24.dp))

                // 打卡按钮区
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ClockButton(
                        label = "上班",
                        icon = Icons.Default.ArrowDownward,
                        time = state.todayLog?.clockIn,
                        onClick = viewModel::clockIn,
                        enabled = state.todayLog?.clockIn == null,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    ClockButton(
                        label = "下班",
                        icon = Icons.Default.ArrowUpward,
                        time = state.todayLog?.clockOut,
                        onClick = viewModel::clockOut,
                        enabled = state.todayLog?.clockOut == null && state.todayLog?.clockIn != null,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 快捷操作
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    OutlinedButton(onClick = viewModel::autoFill) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("自动填")
                    }
                    if (state.todayLog != null) {
                        OutlinedButton(
                            onClick = viewModel::deleteToday,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("删除")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 备注
                if (state.todayLog != null) {
                    OutlinedTextField(
                        value = state.todayLog!!.note,
                        onValueChange = viewModel::updateNote,
                        label = { Text("备注") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 详情卡片
                if (state.summary != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRow("打卡时长", "${"%.1f".format(state.summary!!.rawHours)}h")
                            DetailRow("休息扣除", "${"%.1f".format(state.summary!!.rawHours - state.summary!!.effectiveHours)}h")
                            DetailRow("有效工时", "${"%.1f".format(state.summary!!.effectiveHours)}h", bold = true)
                            DetailRow("加班时长", "${"%.1f".format(state.summary!!.overtimeHours)}h",
                                color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // 下班撒花覆盖层
        ConfettiCelebration(trigger = trigger)
    }
}

@Composable
fun ProgressRing(progress: Double, hours: Double, totalHours: Double) {
    val clampedProgress = progress.coerceIn(0.0, 1.0)
    val color = when {
        clampedProgress >= 1.0 -> MaterialTheme.colorScheme.tertiary
        clampedProgress >= 0.8 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Box(contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 16.dp.toPx()
            drawArc(
                color = Color.Gray.copy(alpha = 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (360f * clampedProgress).toFloat(),
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${"%.1f".format(hours)}h",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                "/ ${"%.1f".format(totalHours)}h",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
fun ClockButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    time: LocalTime?,
    onClick: () -> Unit,
    enabled: Boolean,
    color: Color,
) {
    val bgColor = if (time != null) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledTonalButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(96.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = bgColor),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = if (time != null) color else MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
        if (time != null) {
            Text(
                "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color,
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, bold: Boolean = false, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = color,
        )
    }
}

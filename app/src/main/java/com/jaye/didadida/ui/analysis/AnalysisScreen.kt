package com.jaye.didadida.ui.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(viewModel: AnalysisViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("分析") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            // 月度选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    val m = if (state.month == 1) 12 else state.month - 1
                    val y = if (state.month == 1) state.year - 1 else state.year
                    viewModel.selectMonth(y, m)
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "上月")
                }
                Text(
                    "${state.year}年${state.month}月",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = {
                    val m = if (state.month == 12) 1 else state.month + 1
                    val y = if (state.month == 12) state.year + 1 else state.year
                    viewModel.selectMonth(y, m)
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "下月")
                }
            }

            Spacer(Modifier.height(16.dp))

            // 统计卡片
            if (state.summaries.isNotEmpty()) {
                StatsGrid(state.stats)
                Spacer(Modifier.height(24.dp))
                // 每日工时柱状图
                Text("每日工时", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                DailyBarChart(
                    summaries = state.summaries,
                    standardHours = state.settings.standardHoursPerDay,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "本月暂无数据",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }
}

@Composable
fun StatsGrid(stats: AnalysisViewModel.MonthlyStats) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatCard("打卡天数", "${stats.totalDays}天", Modifier.weight(1f))
            StatCard("总有效工时", "${"%.1f".format(stats.totalEffectiveHours)}h", Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatCard("日均工时", "${"%.1f".format(stats.avgDailyHours)}h", Modifier.weight(1f))
            StatCard("总加班", "${"%.1f".format(stats.totalOvertimeHours)}h", Modifier.weight(1f),
                color = MaterialTheme.colorScheme.secondary)
        }
        if (stats.overtimePay > 0) {
            StatCard("预估加班费", "¥${"%.0f".format(stats.overtimePay)}", Modifier.fillMaxWidth(),
                color = Color(0xFF4CAF50))
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun DailyBarChart(
    summaries: List<com.jaye.didadida.domain.DailySummary>,
    standardHours: Double,
) {
    val primary = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (summaries.isEmpty()) return@Canvas
            val maxVal = summaries.maxOf { it.effectiveHours }.coerceAtLeast(standardHours) * 1.2
            val barWidth = size.width / (summaries.size * 2f + 1f)
            val barGap = barWidth

            summaries.forEachIndexed { i, s ->
                val barHeight = (s.effectiveHours / maxVal * size.height).toFloat()
                val x = barGap + i * (barWidth + barGap)
                val y = size.height - barHeight

                drawRect(
                    color = if (s.effectiveHours >= standardHours) primary else surfaceVariant,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                )

                // 标准工时线
                if (i == 0) {
                    val lineY = size.height - (standardHours / maxVal * size.height).toFloat()
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.5f),
                        start = Offset(0f, lineY),
                        end = Offset(size.width, lineY),
                        strokeWidth = 1f,
                    )
                }
            }
        }
    }
}

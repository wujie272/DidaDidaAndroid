package com.jaye.didadida.ui.records

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jaye.didadida.domain.DailySummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(viewModel: RecordsViewModel) {
    val state by viewModel.state.collectAsState()
    var deleteDialogDate by remember { mutableStateOf<kotlinx.datetime.LocalDate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("记录") })
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 月份选择
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "上月")
                }
                Text(
                    "${state.year}年${state.month}月",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "下月")
                }
            }

            if (state.summaries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "这个月还没有打卡记录\n去今日页面打卡吧",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.summaries, key = { it.date.toString() }) { summary ->
                        SummaryCard(
                            summary = summary,
                            onDelete = { deleteDialogDate = summary.date },
                        )
                    }
                }
            }
        }
    }
    // 删除确认对话框
    deleteDialogDate?.let { date ->
        AlertDialog(
            onDismissRequest = { deleteDialogDate = null },
            title = { Text("删除记录") },
            text = { Text("确定删除 ${date.monthNumber}月${date.dayOfMonth}日的打卡记录吗？\n此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWorkLog(date)
                        deleteDialogDate = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteDialogDate = null }) { Text("取消") }
            },
        )
    }
}

@Composable
fun SummaryCard(summary: DailySummary, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${summary.date.monthNumber}月${summary.date.dayOfMonth}日",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                if (summary.clockIn != null && summary.clockOut != null) {
                    Text(
                        "${fmtTime(summary.clockIn)} → ${fmtTime(summary.clockOut)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("有效工时", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(
                        "${"%.1f".format(summary.effectiveHours)}h",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
                if (summary.overtimeHours > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("加班", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary)
                        Text(
                            "${"%.1f".format(summary.overtimeHours)}h",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

private fun fmtTime(t: kotlinx.datetime.LocalTime): String =
    "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"

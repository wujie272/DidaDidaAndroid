package com.jaye.didadida.bridge

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.jaye.didadida.App
import com.jaye.didadida.domain.WorkLog
import com.jaye.didadida.domain.WorkTimeCalculator
import com.jaye.didadida.domain.DailySummary
import kotlinx.serialization.Serializable

/**
 * 外部 App 数据查询 BroadcastReceiver。
 *
 * 通过 sendBroadcast + PendingIntent 回传结果的方式，允许 RikkaHub 等 App 跨进程读取
 * DidaDida 的打卡数据。使用广播而非 Service 以绕过 Android 12+ 后台 startService 限制。
 *
 * 支持的 Action:
 *   - QUERY_LOGS      → 返回所有打卡记录 JSON
 *   - QUERY_SETTINGS  → 返回当前设置 JSON
 *   - QUERY_MONTH     → 返回某月汇总 JSON（需传 year, month 参数）
 *   - QUERY_TODAY     → 返回今日打卡摘要 JSON
 */
@Serializable
data class TodayQueryResponse(
    val log: WorkLog? = null,
    val summary: DailySummary? = null,
)

class DataQueryReceiver : BroadcastReceiver() {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("callback", PendingIntent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("callback")
        }
        if (pi == null) return

        val pendingResult = goAsync()
        val app = context.applicationContext as App
        val repo = app.repository

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val resultBundle = try {
                    when (action) {
                        "QUERY_LOGS" -> {
                            val logs = repo.allWorkLogs().first()
                            Bundle().apply {
                                putString("data_type", "work_logs")
                                putString("data", json.encodeToString(logs))
                            }
                        }
                        "QUERY_SETTINGS" -> {
                            val settings = repo.loadSettings()
                            Bundle().apply {
                                putString("data_type", "settings")
                                putString("data", json.encodeToString(settings))
                            }
                        }
                        "QUERY_MONTH" -> {
                            val year = intent.getIntExtra("year", 2026)
                            val month = intent.getIntExtra("month", 7)
                            val settings = repo.loadSettings()
                            val summaries = repo.monthlySummaries(year, month, settings)
                            Bundle().apply {
                                putString("data_type", "monthly_summaries")
                                putString("data", json.encodeToString(summaries))
                                putInt("year", year)
                                putInt("month", month)
                            }
                        }
                        "QUERY_TODAY" -> {
                            val today = WorkLog.today()
                            val log = repo.getWorkLog(today)
                            val settings = repo.loadSettings()
                            val summary = if (log != null) {
                                WorkTimeCalculator.calculate(log.date, log.clockIn, log.clockOut, settings)
                            } else null
                            Bundle().apply {
                                putString("data_type", "today_summary")
                                putString("data", json.encodeToString(
                                    TodayQueryResponse(log, summary)
                                ))
                            }
                        }
                        else -> Bundle().apply {
                            putString("error", "unknown_action")
                            putString("supported_actions",
                                "QUERY_LOGS, QUERY_SETTINGS, QUERY_MONTH, QUERY_TODAY")
                        }
                    }
                } catch (e: Exception) {
                    Bundle().apply {
                        putString("error", "query_failed")
                        putString("reason", "${e::class.simpleName}: ${e.message}")
                    }
                }

                try {
                    pi.send(context, 0, Intent().apply {
                        putExtra("result", resultBundle)
                    })
                } catch (_: PendingIntent.CanceledException) {}
            } finally {
                pendingResult.finish()
            }
        }
    }
}

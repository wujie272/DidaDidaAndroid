package com.jaye.didadida.bridge

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.jaye.didadida.App
import com.jaye.didadida.domain.WorkLog
import com.jaye.didadida.domain.WorkTimeCalculator

class DidaDataProvider : ContentProvider() {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        val app = context?.applicationContext as? App ?: return null
        val repo = app.repository

        return runBlocking(Dispatchers.IO) {
            when (method) {
                "query_logs" -> Bundle().apply {
                    putString("data_type", "work_logs")
                    putString("data", json.encodeToString(repo.allWorkLogs().first()))
                }
                "query_settings" -> Bundle().apply {
                    putString("data_type", "settings")
                    putString("data", json.encodeToString(repo.loadSettings()))
                }
                "query_month" -> {
                    val year = extras?.getInt("year", 2026) ?: 2026
                    val month = extras?.getInt("month", 7) ?: 7
                    val settings = repo.loadSettings()
                    val summaries = repo.monthlySummaries(year, month, settings)
                    Bundle().apply {
                        putString("data_type", "monthly_summaries")
                        putString("data", json.encodeToString(summaries))
                    }
                }
                "query_today" -> {
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
                    putString("error", "unknown_method")
                    putString("supported_methods",
                        "query_logs, query_settings, query_month, query_today")
                }
            }
        }
    }

    // ContentProvider abstract methods - not used for call()-based API
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun getType(uri: Uri): String? = null
}

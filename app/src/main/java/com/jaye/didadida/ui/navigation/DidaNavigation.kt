package com.jaye.didadida.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class DidaTab(
    val label: String,
    val icon: ImageVector,
    val route: String,
) {
    TODAY("今日", Icons.Default.Today, "today"),
    RECORDS("记录", Icons.Default.CalendarMonth, "records"),
    ANALYSIS("分析", Icons.Default.Analytics, "analysis"),
    SETTINGS("设置", Icons.Default.Settings, "settings"),
}

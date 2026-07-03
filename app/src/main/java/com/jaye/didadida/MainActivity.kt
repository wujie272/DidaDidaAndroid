package com.jaye.didadida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jaye.didadida.domain.WorkLog
import com.jaye.didadida.ui.analysis.AnalysisScreen
import com.jaye.didadida.ui.analysis.AnalysisViewModel
import com.jaye.didadida.ui.navigation.DidaTab
import com.jaye.didadida.ui.records.RecordsScreen
import com.jaye.didadida.ui.records.RecordsViewModel
import com.jaye.didadida.ui.settings.SettingsScreen
import com.jaye.didadida.ui.settings.SettingsViewModel
import com.jaye.didadida.ui.theme.DidaTheme
import com.jaye.didadida.ui.today.TodayScreen
import com.jaye.didadida.ui.today.TodayViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*

class MainActivity : ComponentActivity() {

    private val app: App get() = application as App

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 处理 shortcut 点击
        handleShortcutIntent(intent?.action)

        setContent {
            DidaTheme {
                DidaApp(app.repository)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleShortcutIntent(intent.action)
    }

    private fun handleShortcutIntent(action: String?) {
        if (action == null) return
        runBlocking {
            when (action) {
                "com.jaye.didadida.action.CLOCK_IN" -> {
                    app.repository.clockIn(WorkLog.today())
                }
                "com.jaye.didadida.action.CLOCK_OUT" -> {
                    app.repository.clockOut(WorkLog.today())
                }
                "com.jaye.didadida.action.AUTO_FILL_TODAY" -> {
                    val settings = app.repository.loadSettings()
                    app.repository.autoFillToday(settings)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DidaApp(repository: com.jaye.didadida.data.WorkLogRepository) {
    val navController = rememberNavController()
    val tabs = listOf(
        DidaTab.TODAY,
        DidaTab.RECORDS,
        DidaTab.ANALYSIS,
        DidaTab.SETTINGS,
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DidaTab.TODAY.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(DidaTab.TODAY.route) {
                val vm: TodayViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return TodayViewModel(repository) as T
                        }
                    }
                )
                TodayScreen(
                    viewModel = vm,
                    onNavigateToSettings = {
                        navController.navigate(DidaTab.SETTINGS.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(DidaTab.RECORDS.route) {
                val vm: RecordsViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return RecordsViewModel(repository) as T
                        }
                    }
                )
                RecordsScreen(viewModel = vm)
            }
            composable(DidaTab.ANALYSIS.route) {
                val vm: AnalysisViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return AnalysisViewModel(repository) as T
                        }
                    }
                )
                AnalysisScreen(viewModel = vm)
            }
            composable(DidaTab.SETTINGS.route) {
                val vm: SettingsViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            return SettingsViewModel(repository) as T
                        }
                    }
                )
                SettingsScreen(viewModel = vm)
            }
        }
    }
}

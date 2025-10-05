package com.matixandr09.procrastination_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.matixandr09.procrastination_app.data.AppViewModel
import com.matixandr09.procrastination_app.screens.AccountsScreen
import com.matixandr09.procrastination_app.screens.AppSelectionScreen
import com.matixandr09.procrastination_app.screens.MainScreen
import com.matixandr09.procrastination_app.screens.StreakScreen
import com.matixandr09.procrastination_app.ui.theme.ProcrastinationappTheme

class MainActivity : ComponentActivity() {
    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProcrastinationappTheme {
                AppNavigation(appViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(appViewModel: AppViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController, appViewModel) }
        composable("accounts") { AccountsScreen(navController) }
        composable("streak") { StreakScreen(navController, appViewModel) }
        composable("app_selection") { AppSelectionScreen(navController) }
    }
}

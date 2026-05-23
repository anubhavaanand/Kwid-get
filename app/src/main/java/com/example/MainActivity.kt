package com.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.provider.KwgtFreeWidgetProvider
import com.example.repository.WidgetRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.WidgetEditorScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy { WidgetRepository.create(applicationContext) }
    private val viewModel: WidgetViewModel by viewModels {
        WidgetViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF070A14)
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard"
                    ) {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToEditor = { id ->
                                    navController.navigate("editor/$id")
                                }
                            )
                        }
                        
                        composable(
                            route = "editor/{widgetId}",
                            arguments = listOf(navArgument("widgetId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val widgetId = backStackEntry.arguments?.getInt("widgetId") ?: -1
                            
                            // Load correct active edits in ViewModel
                            LaunchedEffect(widgetId) {
                                viewModel.selectWidget(widgetId)
                            }

                            WidgetEditorScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                    // Refresh states
                                    KwgtFreeWidgetProvider.forceUpdateAllWidgets(applicationContext)
                                }
                            )
                        }
                    }

                    // Handle deep link intents when clicked from homescreen widget
                    LaunchedEffect(intent) {
                        handleIncomingIntent(intent) { targetWidgetId ->
                            navController.navigate("editor/$targetWidgetId") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?, onDeepLink: (Int) -> Unit) {
        if (intent != null && intent.hasExtra("edit_widget_id")) {
            val targetWidgetId = intent.getIntExtra("edit_widget_id", 999999)
            Log.d("MainActivity", "Deep link check: clicked AppWidget mapped to edit config: $targetWidgetId")
            if (targetWidgetId != 999999) {
                onDeepLink(targetWidgetId)
            }
        }
    }
}

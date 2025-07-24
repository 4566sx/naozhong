package com.biblealarm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.biblealarm.app.ui.home.HomeScreen
import com.biblealarm.app.ui.alarm.AlarmListScreen
import com.biblealarm.app.ui.settings.SettingsScreen
import com.biblealarm.app.ui.theme.BibleAlarmTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主活动 - 应用程序入口点
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            BibleAlarmTheme {
                BibleAlarmApp()
            }
        }
    }
}

@Composable
fun BibleAlarmApp() {
    val navController = rememberNavController()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToAlarms = { navController.navigate("alarms") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }
            composable("alarms") {
                AlarmListScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BibleAlarmAppPreview() {
    BibleAlarmTheme {
        BibleAlarmApp()
    }
}
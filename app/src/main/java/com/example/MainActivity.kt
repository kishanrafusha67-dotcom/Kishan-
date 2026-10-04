package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.LabourViewModel
import com.example.ui.screens.MainScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: LabourViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by viewModel.isDarkMode.collectAsState()
            val currentUserId by viewModel.currentUserId.collectAsState()

            MyApplicationTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(
                        targetState = currentUserId > 0,
                        label = "auth_main_switch"
                    ) { isLoggedIn ->
                        if (isLoggedIn) {
                            MainScreen(
                                viewModel = viewModel,
                                onLogout = { /* Handled inside viewModel */ }
                            )
                        } else {
                            AuthScreen(
                                viewModel = viewModel,
                                onAuthSuccess = { /* Automatically flips currentUserId */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

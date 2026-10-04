package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LabourViewModel
import com.example.ui.screens.bill.BillCreateScreen
import com.example.ui.screens.bill.BillHistoryScreen
import com.example.ui.screens.hazari.HazariScreen
import com.example.ui.screens.pocket.PocketScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SuccessGreen

@Composable
fun MainScreen(
    viewModel: LabourViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    val onlineCount by viewModel.onlineUsersCount.collectAsState()
    val toastMsg by viewModel.toastMessage.collectAsState()

    // Handle toast messages
    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Android back handler: If on non-home screen, pressing Back returns to Create Bill (tab 0)
    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "બિલ બનાવો") },
                    label = { Text("બિલ બનાવો", fontSize = 11.sp, fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryIndigo,
                        selectedTextColor = PrimaryIndigo,
                        indicatorColor = PrimaryIndigo.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_create_bill")
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "પોકેટ") },
                    label = { Text("પોકેટ", fontSize = 11.sp, fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryIndigo,
                        selectedTextColor = PrimaryIndigo,
                        indicatorColor = PrimaryIndigo.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_pocket")
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (onlineCount > 0) {
                                    Badge(containerColor = SuccessGreen) {
                                        Text("$onlineCount", color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Group, contentDescription = "હાજરી")
                        }
                    },
                    label = { Text("હાજરી", fontSize = 11.sp, fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryIndigo,
                        selectedTextColor = PrimaryIndigo,
                        indicatorColor = PrimaryIndigo.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_hazari")
                )

                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = "બિલ યાદી") },
                    label = { Text("બિલ યાદી", fontSize = 11.sp, fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryIndigo,
                        selectedTextColor = PrimaryIndigo,
                        indicatorColor = PrimaryIndigo.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_bill_history")
                )

                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "સેટિંગ્સ") },
                    label = { Text("સેટિંગ્સ", fontSize = 11.sp, fontWeight = if (currentTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryIndigo,
                        selectedTextColor = PrimaryIndigo,
                        indicatorColor = PrimaryIndigo.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    0 -> BillCreateScreen(
                        viewModel = viewModel,
                        onNavigateToPocket = { currentTab = 1 },
                        onNavigateToHazari = { currentTab = 2 }
                    )
                    1 -> PocketScreen(
                        viewModel = viewModel,
                        onBack = { currentTab = 0 }
                    )
                    2 -> HazariScreen(
                        viewModel = viewModel
                    )
                    3 -> BillHistoryScreen(
                        viewModel = viewModel
                    )
                    4 -> SettingsScreen(
                        viewModel = viewModel,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}

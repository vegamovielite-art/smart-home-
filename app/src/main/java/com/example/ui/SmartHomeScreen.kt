package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ControlsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HardwareGuideScreen
import com.example.ui.screens.SensorsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.NavTab
import com.example.ui.viewmodel.SmartHomeViewModel

@Composable
fun SmartHomeScreen(viewModel: SmartHomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = state.activeTab != NavTab.DASHBOARD) {
        viewModel.selectTab(NavTab.DASHBOARD)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = NavyCard,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = state.activeTab == NavTab.DASHBOARD,
                    onClick = { viewModel.selectTab(NavTab.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Dashboard",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyDark,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = state.activeTab == NavTab.CONTROLS,
                    onClick = { viewModel.selectTab(NavTab.CONTROLS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Controls",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Controls", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyDark,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = state.activeTab == NavTab.SENSORS,
                    onClick = { viewModel.selectTab(NavTab.SENSORS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Sensors",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Sensors", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyDark,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = state.activeTab == NavTab.SETTINGS,
                    onClick = { viewModel.selectTab(NavTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyDark,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = state.activeTab == NavTab.HARDWARE_GUIDE,
                    onClick = { viewModel.selectTab(NavTab.HARDWARE_GUIDE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Guide",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = { Text("Guide", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyDark,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.activeTab) {
                NavTab.DASHBOARD -> DashboardScreen(
                    state = state,
                    onToggleLed = { viewModel.toggleLed(it) },
                    onOpenDoor = { viewModel.openDoor() },
                    onCloseDoor = { viewModel.closeDoor() },
                    onSetDoorAngle = { viewModel.setServoAngle(it) },
                    onToggleMotor = { viewModel.toggleMotor() },
                    onRefresh = { viewModel.refreshManually() },
                    onNavigateToSettings = { viewModel.selectTab(NavTab.SETTINGS) },
                    onNavigateToGuide = { viewModel.selectTab(NavTab.HARDWARE_GUIDE) }
                )

                NavTab.CONTROLS -> ControlsScreen(
                    state = state,
                    onToggleLed = { viewModel.toggleLed(it) },
                    onOpenDoor = { viewModel.openDoor() },
                    onCloseDoor = { viewModel.closeDoor() },
                    onSetDoorAngle = { viewModel.setServoAngle(it) },
                    onToggleMotor = { viewModel.toggleMotor() }
                )

                NavTab.SENSORS -> SensorsScreen(state = state)

                NavTab.SETTINGS -> SettingsScreen(
                    state = state,
                    onSaveSettings = { ip, port, sim, open, close, interval ->
                        viewModel.saveSettings(ip, port, sim, open, close, interval)
                    },
                    onTestConnection = { ip, port ->
                        viewModel.testConnection(ip, port)
                    }
                )

                NavTab.HARDWARE_GUIDE -> HardwareGuideScreen()
            }
        }
    }
}

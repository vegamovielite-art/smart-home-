package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ConnectionPill
import com.example.ui.components.DCMotorCard
import com.example.ui.components.LedControlCard
import com.example.ui.components.SensorsGridCard
import com.example.ui.components.ServoDoorCard
import com.example.ui.components.SystemTelemetryCard
import com.example.ui.components.UltrasonicCard
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NavyDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ConnectionState
import com.example.ui.viewmodel.NavTab
import com.example.ui.viewmodel.SmartHomeUiState

@Composable
fun DashboardScreen(
    state: SmartHomeUiState,
    onToggleLed: (Int) -> Unit,
    onOpenDoor: () -> Unit,
    onCloseDoor: () -> Unit,
    onSetDoorAngle: (Int) -> Unit,
    onToggleMotor: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = state.status

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Dashboard Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Smart Home",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "ESP32 Automation Hub",
                        fontSize = 12.sp,
                        color = ElectricCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ConnectionPill(
                        connectionState = state.connectionState,
                        isSimulationMode = state.isSimulationMode
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onRefresh,
                        enabled = !state.isRefreshing,
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = ElectricCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = ElectricCyan
                            )
                        }
                    }
                }
            }
        }

        // Connection Error / Offline Banner
        if (state.errorMessage != null && state.connectionState != ConnectionState.CONNECTED) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CrimsonRed.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ESP32 Connection Error",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonRed
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.errorMessage,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onRefresh,
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Retry Connection", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = onNavigateToSettings,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Change IP Settings", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 1. Lighting Control Card
        item {
            LedControlCard(
                led1 = status?.led1 ?: false,
                led2 = status?.led2 ?: false,
                led3 = status?.led3 ?: false,
                isLoadingMap = state.ledLoadingStates,
                onToggleLed = onToggleLed
            )
        }

        // 2. Smart Door Control Card
        item {
            ServoDoorCard(
                angle = status?.servoAngle ?: 0,
                doorState = status?.doorCommandedState ?: "CLOSED",
                isLoading = state.isServoLoading,
                onOpen = onOpenDoor,
                onClose = onCloseDoor,
                onSetAngle = onSetDoorAngle
            )
        }

        // 3. DC Motor Control Card
        item {
            DCMotorCard(
                isRunning = status?.dcMotor ?: false,
                isLoading = state.isMotorLoading,
                onToggle = onToggleMotor
            )
        }

        // 4. Ultrasonic Sensor Telemetry
        item {
            UltrasonicCard(
                distanceCm = status?.ultrasonicDistanceCm ?: 0.0f,
                isValid = status?.ultrasonicValid ?: false
            )
        }

        // 5. PIR Motion & Dual IR Proximity Sensors
        item {
            SensorsGridCard(
                pirMotion = status?.pirMotion ?: false,
                ir1Detected = status?.ir1Detected ?: false,
                ir2Detected = status?.ir2Detected ?: false
            )
        }

        // 6. Hardware System Diagnostics
        item {
            SystemTelemetryCard(
                esp32Ip = status?.esp32Ip ?: state.currentIp,
                uptimeMs = status?.uptimeMs ?: 0L,
                lastUpdatedText = state.lastUpdatedText,
                wifiConnected = status?.wifiConnected ?: false,
                isSimulationMode = state.isSimulationMode
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

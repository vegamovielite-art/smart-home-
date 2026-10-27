package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DCMotorCard
import com.example.ui.components.LedControlCard
import com.example.ui.components.ServoDoorCard
import com.example.ui.components.TechCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SmartHomeUiState

@Composable
fun ControlsScreen(
    state: SmartHomeUiState,
    onToggleLed: (Int) -> Unit,
    onOpenDoor: () -> Unit,
    onCloseDoor: () -> Unit,
    onSetDoorAngle: (Int) -> Unit,
    onToggleMotor: () -> Unit,
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
            Column {
                Text(
                    text = "Device Actuators",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Direct output control for LEDs, Servo Door, and DC Motor",
                    fontSize = 12.sp,
                    color = ElectricCyan
                )
            }
        }

        // Lighting Control
        item {
            LedControlCard(
                led1 = status?.led1 ?: false,
                led2 = status?.led2 ?: false,
                led3 = status?.led3 ?: false,
                isLoadingMap = state.ledLoadingStates,
                onToggleLed = onToggleLed
            )
        }

        // Servo Door
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

        // Quick Angle Presets Card
        item {
            TechCard {
                Column {
                    Text(
                        text = "Quick Servo Presets",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            0 to "0° (Closed)",
                            45 to "45° (Ajar)",
                            90 to "90° (Open)",
                            180 to "180° (Max)"
                        ).forEach { (angle, label) ->
                            Button(
                                onClick = { onSetDoorAngle(angle) },
                                modifier = Modifier.weight(1f),
                                enabled = !state.isServoLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (status?.servoAngle == angle) ElectricCyan else NavyDark,
                                    contentColor = if (status?.servoAngle == angle) Color.Black else TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(label, fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        // DC Motor Control
        item {
            DCMotorCard(
                isRunning = status?.dcMotor ?: false,
                isLoading = state.isMotorLoading,
                onToggle = onToggleMotor
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

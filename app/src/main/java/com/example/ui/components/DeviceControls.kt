package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LedControlCard(
    led1: Boolean,
    led2: Boolean,
    led3: Boolean,
    isLoadingMap: Map<Int, Boolean>,
    onToggleLed: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    TechCard(modifier = modifier) {
        Column {
            SectionHeader(
                title = "Lighting Control",
                subtitle = "3 Independent Digital LEDs",
                icon = Icons.Default.Lightbulb
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LedItem(
                    index = 1,
                    name = "Living Room",
                    isOn = led1,
                    isLoading = isLoadingMap[1] == true,
                    onToggle = { onToggleLed(1) },
                    modifier = Modifier.weight(1f)
                )
                LedItem(
                    index = 2,
                    name = "Bedroom",
                    isOn = led2,
                    isLoading = isLoadingMap[2] == true,
                    onToggle = { onToggleLed(2) },
                    modifier = Modifier.weight(1f)
                )
                LedItem(
                    index = 3,
                    name = "Kitchen",
                    isOn = led3,
                    isLoading = isLoadingMap[3] == true,
                    onToggle = { onToggleLed(3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LedItem(
    index: Int,
    name: String,
    isOn: Boolean,
    isLoading: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glowColor by animateColorAsState(
        targetValue = if (isOn) NeonAmber else NavyBorder,
        label = "ledGlow"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCardElevated)
            .padding(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = if (isOn) 0.25f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = ElectricCyan
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "LED $index",
                        tint = if (isOn) NeonAmber else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LED $index",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = name,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Switch(
                checked = isOn,
                onCheckedChange = { onToggle() },
                enabled = !isLoading,
                modifier = Modifier.testTag("led_${index}_switch"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonAmber,
                    checkedTrackColor = NeonAmber.copy(alpha = 0.35f),
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = NavyBorder
                )
            )
        }
    }
}

@Composable
fun ServoDoorCard(
    angle: Int,
    doorState: String,
    isLoading: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onSetAngle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOpen = angle >= 45 || doorState.equals("OPEN", ignoreCase = true)

    TechCard(
        modifier = modifier,
        borderColor = if (isOpen) EmeraldGreen.copy(alpha = 0.5f) else NavyBorder
    ) {
        Column {
            SectionHeader(
                title = "Smart Door Lock (Servo)",
                subtitle = "Configurable 0° - 180° Range",
                icon = Icons.Default.DoorFront,
                trailing = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isOpen) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isOpen) "UNLOCKED / OPEN" else "LOCKED / CLOSED",
                            color = if (isOpen) EmeraldGreen else CrimsonRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(NavyCardElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOpen) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isOpen) EmeraldGreen else NeonAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Current Angle: $angle°",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isLoading) "Moving servo..." else "Command: $doorState",
                            fontSize = 12.sp,
                            color = if (isLoading) ElectricCyan else TextSecondary
                        )
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = ElectricCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpen,
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("door_open_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("OPEN (90°)", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onClose,
                    enabled = !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("door_close_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CrimsonRed
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CrimsonRed)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CLOSE (0°)", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Angle Slider for exact manual degree control
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Manual Position Slider: $angle°",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Slider(
                    value = angle.toFloat(),
                    onValueChange = { onSetAngle(it.toInt()) },
                    valueRange = 0f..180f,
                    steps = 17,
                    enabled = !isLoading,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = NavyBorder
                    )
                )
            }
        }
    }
}

@Composable
fun DCMotorCard(
    isRunning: Boolean,
    isLoading: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    TechCard(
        modifier = modifier,
        borderColor = if (isRunning) ElectricCyan.copy(alpha = 0.5f) else NavyBorder
    ) {
        Column {
            SectionHeader(
                title = "DC Motor / Fan Control",
                subtitle = "Driver / Relay Protected Output",
                icon = Icons.Default.RotateRight,
                trailing = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isRunning) ElectricCyan.copy(alpha = 0.15f) else NavyCardElevated
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isRunning) "RUNNING" else "STOPPED",
                            color = if (isRunning) ElectricCyan else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isRunning) "Motor Output Active (HIGH)" else "Motor Output Inactive (LOW)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isRunning) TextPrimary else TextMuted
                    )
                    Text(
                        text = "Safety Note: Connected via L298N driver / Relay",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Button(
                    onClick = onToggle,
                    enabled = !isLoading,
                    modifier = Modifier.testTag("motor_toggle_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) CrimsonRed else ElectricCyan,
                        contentColor = if (isRunning) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isRunning) "STOP MOTOR" else "START MOTOR", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

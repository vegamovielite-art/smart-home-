package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SectionHeader
import com.example.ui.components.TechCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SmartHomeUiState

@Composable
fun SettingsScreen(
    state: SmartHomeUiState,
    onSaveSettings: (ip: String, port: Int, isSimulationMode: Boolean, openAngle: Int, closeAngle: Int, intervalMs: Long) -> Unit,
    onTestConnection: (ip: String, port: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var ipInput by remember(state.currentIp) { mutableStateOf(state.currentIp) }
    var portInput by remember(state.currentPort) { mutableStateOf(state.currentPort.toString()) }
    var isSimMode by remember(state.isSimulationMode) { mutableStateOf(state.isSimulationMode) }
    var openAngleInput by remember(state.doorOpenAngle) { mutableStateOf(state.doorOpenAngle.toString()) }
    var closeAngleInput by remember(state.doorCloseAngle) { mutableStateOf(state.doorCloseAngle.toString()) }
    var selectedInterval by remember { mutableLongStateOf(2000L) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "ESP32 Network Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Configure local Wi-Fi IP address, REST API port & parameters",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Test Feedback banner
        if (state.testResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (state.testResult.startsWith("Success")) EmeraldGreen.copy(alpha = 0.2f)
                        else NeonAmber.copy(alpha = 0.2f)
                    )
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (state.testResult.startsWith("Success")) EmeraldGreen else NeonAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.testResult,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Wi-Fi Connection Card
        TechCard {
            Column {
                SectionHeader(
                    title = "Connection Parameters",
                    subtitle = "ESP32 local network endpoints",
                    icon = Icons.Default.Lan
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    label = { Text("ESP32 IP Address") },
                    placeholder = { Text("e.g. 192.168.1.150") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ip_address_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = ElectricCyan,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = portInput,
                    onValueChange = { portInput = it },
                    label = { Text("HTTP Port") },
                    placeholder = { Text("Default 80") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("port_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = ElectricCyan,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = {
                        val portInt = portInput.toIntOrNull() ?: 80
                        onTestConnection(ipInput, portInt)
                    },
                    enabled = !state.isTestingConnection && ipInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_connection_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (state.isTestingConnection) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = ElectricCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testing ping to ESP32...")
                    } else {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TEST CONNECTION (PING)")
                    }
                }
            }
        }

        // Demo Simulation Mode Card
        TechCard {
            Column {
                SectionHeader(
                    title = "Simulation / Demo Mode",
                    subtitle = "Preview controls without physical hardware",
                    icon = Icons.Default.PlayCircle
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(NavyCardElevated)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSimMode) "Demo Mode Enabled" else "Live ESP32 Mode",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimMode) NeonAmber else ElectricCyan
                        )
                        Text(
                            text = if (isSimMode)
                                "Sensor telemetry and actuators respond interactively inside browser emulator."
                            else
                                "App sends actual HTTP REST calls to http://$ipInput:$portInput/",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = isSimMode,
                        onCheckedChange = { isSimMode = it },
                        modifier = Modifier.testTag("simulation_mode_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonAmber,
                            checkedTrackColor = NeonAmber.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }

        // Telemetry Polling Rate & Servo Angles
        TechCard {
            Column {
                SectionHeader(
                    title = "Telemetry & Hardware Tunables",
                    subtitle = "Sensor refresh interval and door angles",
                    icon = Icons.Default.Settings
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Telemetry Refresh Rate", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1000L to "1 Second", 2000L to "2 Seconds", 5000L to "5 Seconds").forEach { (interval, label) ->
                        FilterChip(
                            selected = selectedInterval == interval,
                            onClick = { selectedInterval = interval },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricCyan,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = openAngleInput,
                        onValueChange = { openAngleInput = it },
                        label = { Text("Open Angle (°)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = NavyBorder
                        )
                    )
                    OutlinedTextField(
                        value = closeAngleInput,
                        onValueChange = { closeAngleInput = it },
                        label = { Text("Close Angle (°)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = NavyBorder
                        )
                    )
                }
            }
        }

        // Save Button
        Button(
            onClick = {
                val port = portInput.toIntOrNull() ?: 80
                val open = openAngleInput.toIntOrNull()?.coerceIn(0, 180) ?: 90
                val close = closeAngleInput.toIntOrNull()?.coerceIn(0, 180) ?: 0
                onSaveSettings(ipInput, port, isSimMode, open, close, selectedInterval)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_settings_button"),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("SAVE SETTINGS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SensorsGridCard
import com.example.ui.components.SystemTelemetryCard
import com.example.ui.components.TechCard
import com.example.ui.components.UltrasonicCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NavyDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SmartHomeUiState

@Composable
fun SensorsScreen(
    state: SmartHomeUiState,
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
                    text = "Live Sensor Telemetry",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Real-time readings from Ultrasonic, PIR, and dual IR sensors",
                    fontSize = 12.sp,
                    color = ElectricCyan
                )
            }
        }

        // Ultrasonic Sensor
        item {
            UltrasonicCard(
                distanceCm = status?.ultrasonicDistanceCm ?: 0.0f,
                isValid = status?.ultrasonicValid ?: false
            )
        }

        // Ultrasonic Hardware Safety info
        item {
            TechCard {
                Column {
                    Text(
                        text = "HC-SR04 Proximity Tuning",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Range: 2 cm to 400 cm\n" +
                                "• Valid Range Threshold: Readings below 2 cm or timeouts are discarded.\n" +
                                "• Echo pin requires 1kΩ/2kΩ voltage divider to protect ESP32 3.3V GPIO.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // PIR Motion and IR Sensors
        item {
            SensorsGridCard(
                pirMotion = status?.pirMotion ?: false,
                ir1Detected = status?.ir1Detected ?: false,
                ir2Detected = status?.ir2Detected ?: false
            )
        }

        // Sensor Operation Guide
        item {
            TechCard {
                Column {
                    Text(
                        text = "Sensor Calibration Notes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• PIR Sensor requires 30-60 seconds warm-up calibration after ESP32 power-on.\n" +
                                "• IR Proximity Sensors feature onboard potentiometer for sensitivity distance adjustment.\n" +
                                "• Digital output polarity (Active LOW / Active HIGH) can be inverted in the firmware.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Hardware System Telemetry
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

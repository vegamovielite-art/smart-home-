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
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun UltrasonicCard(
    distanceCm: Float,
    isValid: Boolean,
    modifier: Modifier = Modifier
) {
    TechCard(modifier = modifier) {
        Column {
            SectionHeader(
                title = "Distance Measurement",
                subtitle = "HC-SR04 Ultrasonic Telemetry",
                icon = Icons.Default.Radar
            )
            Spacer(modifier = Modifier.height(12.dp))
            DistanceGauge(distanceCm = distanceCm, isValid = isValid)
        }
    }
}

@Composable
fun SensorsGridCard(
    pirMotion: Boolean,
    ir1Detected: Boolean,
    ir2Detected: Boolean,
    modifier: Modifier = Modifier
) {
    TechCard(modifier = modifier) {
        Column {
            SectionHeader(
                title = "Security & Motion Sensors",
                subtitle = "PIR Motion & Dual Infrared Proximity",
                icon = Icons.Default.Sensors
            )
            Spacer(modifier = Modifier.height(14.dp))

            // PIR Motion Sensor
            val pirColor by animateColorAsState(
                targetValue = if (pirMotion) CrimsonRed else EmeraldGreen,
                label = "pirColor"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyCardElevated)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(pirColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = pirColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PIR Motion Sensor",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (pirMotion) "Active Motion Alert!" else "Area Secure (No Motion)",
                            fontSize = 11.sp,
                            color = if (pirMotion) CrimsonRed else TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(pirColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (pirMotion) "MOTION DETECTED" else "CLEAR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = pirColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual IR Sensors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IrSensorItem(
                    name = "IR Sensor 1",
                    location = "Main Gate / Door",
                    isDetected = ir1Detected,
                    modifier = Modifier.weight(1f)
                )
                IrSensorItem(
                    name = "IR Sensor 2",
                    location = "Hallway / Window",
                    isDetected = ir2Detected,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun IrSensorItem(
    name: String,
    location: String,
    isDetected: Boolean,
    modifier: Modifier = Modifier
) {
    val statusColor = if (isDetected) NeonAmber else EmeraldGreen

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NavyCardElevated)
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = location,
                fontSize = 10.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isDetected) "Object Detected" else "No Obstacle",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = statusColor
            )
        }
    }
}

@Composable
fun SystemTelemetryCard(
    esp32Ip: String,
    uptimeMs: Long,
    lastUpdatedText: String,
    wifiConnected: Boolean,
    isSimulationMode: Boolean,
    modifier: Modifier = Modifier
) {
    TechCard(modifier = modifier) {
        Column {
            SectionHeader(
                title = "Hardware Telemetry",
                subtitle = "ESP32 DevKit Node Diagnostics",
                icon = Icons.Default.Info
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TelemetryItem(
                    label = "ESP32 IP",
                    value = if (isSimulationMode) "192.168.1.100 (Demo)" else esp32Ip,
                    subtext = if (wifiConnected) "Wi-Fi Connected" else "No Wi-Fi",
                    modifier = Modifier.weight(1f)
                )
                val uptimeSec = uptimeMs / 1000L
                val uptimeText = "${uptimeSec / 60}m ${uptimeSec % 60}s"
                TelemetryItem(
                    label = "Node Uptime",
                    value = uptimeText,
                    subtext = "Last sync: $lastUpdatedText",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TelemetryItem(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NavyCardElevated)
            .padding(10.dp)
    ) {
        Column {
            Text(label, fontSize = 11.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtext, fontSize = 10.sp, color = ElectricCyan, maxLines = 1)
        }
    }
}

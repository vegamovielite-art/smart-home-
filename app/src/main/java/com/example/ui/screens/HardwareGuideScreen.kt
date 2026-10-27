package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SectionHeader
import com.example.ui.components.TechCard
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyCardElevated
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HardwareGuideScreen(modifier: Modifier = Modifier) {
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
                text = "ESP32 Wiring & Safety Guide",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Hardware pin mapping, voltage safety, and Arduino sketch notes",
                fontSize = 12.sp,
                color = ElectricCyan
            )
        }

        // Critical Safety Notice
        TechCard(borderColor = CrimsonRed.copy(alpha = 0.6f)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CrimsonRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CRITICAL HARDWARE SAFETY RULES",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonRed
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. HC-SR04 Echo 5V Divider: The Echo pin outputs 5V logic. ESP32 GPIOs tolerate max 3.3V. Use a voltage divider (1kΩ in series + 2kΩ to GND) before GPIO 19.\n\n" +
                            "2. DC Motor Driver: NEVER power a DC motor or Servo directly from an ESP32 GPIO pin. Use an external 5V/9V supply and an L298N driver / relay with common GND connected to ESP32 GND.\n\n" +
                            "3. Do not use GPIO 6-11 (connected to internal SPI Flash). Avoid strapping pins GPIO 0, 2, 12, 15 for outputs.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // Pinout Mapping Table Card
        TechCard {
            Column {
                SectionHeader(
                    title = "Verified GPIO Pin Map",
                    subtitle = "ESP32 DevKit 30/38-pin Pinout",
                    icon = Icons.Default.DeviceHub
                )
                Spacer(modifier = Modifier.height(12.dp))

                val pinList = listOf(
                    Triple("LED 1 (Living Room)", "GPIO 25", "Output via 220Ω resistor"),
                    Triple("LED 2 (Bedroom)", "GPIO 26", "Output via 220Ω resistor"),
                    Triple("LED 3 (Kitchen)", "GPIO 27", "Output via 220Ω resistor"),
                    Triple("Servo Motor (Door)", "GPIO 13", "PWM Signal to Orange wire (External 5V)"),
                    Triple("DC Motor / Fan", "GPIO 14", "Output to L298N IN1 (External Power)"),
                    Triple("Ultrasonic Trig", "GPIO 18", "Output trigger pulse"),
                    Triple("Ultrasonic Echo", "GPIO 19", "Input via 1k/2k 3.3V voltage divider!"),
                    Triple("PIR Motion Sensor", "GPIO 4", "Digital Input (Active HIGH)"),
                    Triple("IR Sensor 1", "GPIO 32", "Digital Input (Active LOW/HIGH)"),
                    Triple("IR Sensor 2", "GPIO 33", "Digital Input (Active LOW/HIGH)"),
                    Triple("OLED SSD1306 SDA", "GPIO 21", "I2C Data line (Optional)"),
                    Triple("OLED SSD1306 SCL", "GPIO 22", "I2C Clock line (Optional)")
                )

                pinList.forEachIndexed { index, (component, pin, note) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (index % 2 == 0) NavyCardElevated else Color.Transparent)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(component, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ElectricCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(pin, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                            }
                        }
                        Text(note, fontSize = 10.sp, color = TextMuted)
                    }
                    if (index < pinList.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // REST API Reference Card
        TechCard {
            Column {
                SectionHeader(
                    title = "REST API Specification",
                    subtitle = "Matching Android App & ESP32 WebServer",
                    icon = Icons.Default.Code
                )
                Spacer(modifier = Modifier.height(10.dp))

                ApiItem(
                    method = "GET",
                    endpoint = "/api/status",
                    description = "Returns complete JSON telemetry (LEDs, Servo, Motor, IR1, IR2, Ultrasonic distance, PIR motion, Uptime)"
                )
                Spacer(modifier = Modifier.height(8.dp))
                ApiItem(
                    method = "POST",
                    endpoint = "/api/led",
                    description = "Body: {\"led\": 1, \"state\": true}\nControls individual LED 1, 2, or 3"
                )
                Spacer(modifier = Modifier.height(8.dp))
                ApiItem(
                    method = "POST",
                    endpoint = "/api/servo",
                    description = "Body: {\"angle\": 90}\nCommands door servo angle (0 to 180 degrees)"
                )
                Spacer(modifier = Modifier.height(8.dp))
                ApiItem(
                    method = "POST",
                    endpoint = "/api/motor",
                    description = "Body: {\"state\": true}\nTurns DC motor / fan ON or OFF"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ApiItem(
    method: String,
    endpoint: String,
    description: String
) {
    val methodColor = if (method == "GET") EmeraldGreen else ElectricCyan
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NavyCardElevated)
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(methodColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(method, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = methodColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = endpoint,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

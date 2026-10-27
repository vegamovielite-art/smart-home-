package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Real-time status returned by ESP32 via GET /api/status
 */
@JsonClass(generateAdapter = true)
data class Esp32Status(
    @Json(name = "esp32_ip")
    val esp32Ip: String = "0.0.0.0",

    @Json(name = "wifi_connected")
    val wifiConnected: Boolean = false,

    @Json(name = "led1")
    val led1: Boolean = false,

    @Json(name = "led2")
    val led2: Boolean = false,

    @Json(name = "led3")
    val led3: Boolean = false,

    @Json(name = "servo_angle")
    val servoAngle: Int = 0,

    @Json(name = "door_commanded_state")
    val doorCommandedState: String = "CLOSED",

    @Json(name = "dc_motor")
    val dcMotor: Boolean = false,

    @Json(name = "ir1_detected")
    val ir1Detected: Boolean = false,

    @Json(name = "ir2_detected")
    val ir2Detected: Boolean = false,

    @Json(name = "ultrasonic_distance_cm")
    val ultrasonicDistanceCm: Float = 0.0f,

    @Json(name = "ultrasonic_valid")
    val ultrasonicValid: Boolean = false,

    @Json(name = "pir_motion")
    val pirMotion: Boolean = false,

    @Json(name = "uptime_ms")
    val uptimeMs: Long = 0L
)

/**
 * Command payload for controlling LEDs via POST /api/led
 */
@JsonClass(generateAdapter = true)
data class LedCommand(
    @Json(name = "led")
    val led: Int,

    @Json(name = "state")
    val state: Boolean
)

/**
 * Command payload for controlling Servo Door via POST /api/servo
 */
@JsonClass(generateAdapter = true)
data class ServoCommand(
    @Json(name = "angle")
    val angle: Int
)

/**
 * Command payload for controlling DC Motor via POST /api/motor
 */
@JsonClass(generateAdapter = true)
data class MotorCommand(
    @Json(name = "state")
    val state: Boolean
)

/**
 * Generic API response from ESP32
 */
@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "success")
    val success: Boolean = true,

    @Json(name = "message")
    val message: String? = null,

    @Json(name = "error")
    val error: String? = null,

    @Json(name = "status")
    val status: String? = null
)

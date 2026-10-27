package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences

class SmartHomePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var esp32Ip: String
        get() = prefs.getString(KEY_ESP32_IP, DEFAULT_IP) ?: DEFAULT_IP
        set(value) = prefs.edit().putString(KEY_ESP32_IP, value.trim()).apply()

    var esp32Port: Int
        get() = prefs.getInt(KEY_ESP32_PORT, DEFAULT_PORT)
        set(value) = prefs.edit().putInt(KEY_ESP32_PORT, value).apply()

    var refreshIntervalMs: Long
        get() = prefs.getLong(KEY_REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL)
        set(value) = prefs.edit().putLong(KEY_REFRESH_INTERVAL, value).apply()

    var isSimulationMode: Boolean
        get() = prefs.getBoolean(KEY_SIMULATION_MODE, true)
        set(value) = prefs.edit().putBoolean(KEY_SIMULATION_MODE, value).apply()

    var doorOpenAngle: Int
        get() = prefs.getInt(KEY_DOOR_OPEN_ANGLE, DEFAULT_OPEN_ANGLE)
        set(value) = prefs.edit().putInt(KEY_DOOR_OPEN_ANGLE, value).apply()

    var doorCloseAngle: Int
        get() = prefs.getInt(KEY_DOOR_CLOSE_ANGLE, DEFAULT_CLOSE_ANGLE)
        set(value) = prefs.edit().putInt(KEY_DOOR_CLOSE_ANGLE, value).apply()

    fun getBaseUrl(): String {
        val ip = esp32Ip.ifBlank { DEFAULT_IP }
        val port = esp32Port
        return "http://$ip:$port/"
    }

    companion object {
        private const val PREFS_NAME = "smart_home_prefs"
        private const val KEY_ESP32_IP = "key_esp32_ip"
        private const val KEY_ESP32_PORT = "key_esp32_port"
        private const val KEY_REFRESH_INTERVAL = "key_refresh_interval"
        private const val KEY_SIMULATION_MODE = "key_simulation_mode"
        private const val KEY_DOOR_OPEN_ANGLE = "key_door_open_angle"
        private const val KEY_DOOR_CLOSE_ANGLE = "key_door_close_angle"

        const val DEFAULT_IP = "192.168.1.100"
        const val DEFAULT_PORT = 80
        const val DEFAULT_REFRESH_INTERVAL = 2000L
        const val DEFAULT_OPEN_ANGLE = 90
        const val DEFAULT_CLOSE_ANGLE = 0
    }
}

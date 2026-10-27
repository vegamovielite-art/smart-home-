package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Esp32Status
import com.example.data.preferences.SmartHomePreferences
import com.example.data.repository.SmartHomeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ConnectionState {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    ERROR
}

enum class NavTab {
    DASHBOARD,
    CONTROLS,
    SENSORS,
    SETTINGS,
    HARDWARE_GUIDE
}

data class SmartHomeUiState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val status: Esp32Status? = null,
    val lastUpdateTime: Long = 0L,
    val lastUpdatedText: String = "Never",
    val isRefreshing: Boolean = false,
    val ledLoadingStates: Map<Int, Boolean> = emptyMap(),
    val isServoLoading: Boolean = false,
    val isMotorLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentIp: String = "",
    val currentPort: Int = 80,
    val isSimulationMode: Boolean = true,
    val activeTab: NavTab = NavTab.DASHBOARD,
    val doorOpenAngle: Int = 90,
    val doorCloseAngle: Int = 0,
    val isTestingConnection: Boolean = false,
    val testResult: String? = null
)

class SmartHomeViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = SmartHomePreferences(application.applicationContext)
    private val repository = SmartHomeRepository(preferences)

    private val _uiState = MutableStateFlow(
        SmartHomeUiState(
            currentIp = preferences.esp32Ip,
            currentPort = preferences.esp32Port,
            isSimulationMode = preferences.isSimulationMode,
            doorOpenAngle = preferences.doorOpenAngle,
            doorCloseAngle = preferences.doorCloseAngle
        )
    )
    val uiState: StateFlow<SmartHomeUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    init {
        startPolling()
    }

    fun selectTab(tab: NavTab) {
        _uiState.update { it.copy(activeTab = tab, errorMessage = null) }
    }

    fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchStatus(isUserInitiated = false)
                delay(preferences.refreshIntervalMs.coerceAtLeast(1000L))
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun refreshManually() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            fetchStatus(isUserInitiated = true)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun fetchStatus(isUserInitiated: Boolean) {
        if (isUserInitiated) {
            _uiState.update { it.copy(connectionState = ConnectionState.CONNECTING) }
        }

        val result = repository.fetchStatus()
        result.fold(
            onSuccess = { status ->
                val now = System.currentTimeMillis()
                _uiState.update {
                    it.copy(
                        status = status,
                        connectionState = ConnectionState.CONNECTED,
                        lastUpdateTime = now,
                        lastUpdatedText = timeFormatter.format(Date(now)),
                        errorMessage = null
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        connectionState = ConnectionState.DISCONNECTED,
                        errorMessage = error.message ?: "Failed to connect to ESP32"
                    )
                }
            }
        )
    }

    fun toggleLed(ledIndex: Int) {
        val currentLedState = when (ledIndex) {
            1 -> _uiState.value.status?.led1 ?: false
            2 -> _uiState.value.status?.led2 ?: false
            3 -> _uiState.value.status?.led3 ?: false
            else -> false
        }
        val targetState = !currentLedState

        viewModelScope.launch {
            _uiState.update {
                it.copy(ledLoadingStates = it.ledLoadingStates + (ledIndex to true))
            }

            val result = repository.setLed(ledIndex, targetState)
            result.fold(
                onSuccess = { newState ->
                    _uiState.update { state ->
                        val updatedStatus = when (ledIndex) {
                            1 -> state.status?.copy(led1 = newState)
                            2 -> state.status?.copy(led2 = newState)
                            3 -> state.status?.copy(led3 = newState)
                            else -> state.status
                        }
                        state.copy(
                            status = updatedStatus,
                            ledLoadingStates = state.ledLoadingStates - ledIndex,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            ledLoadingStates = it.ledLoadingStates - ledIndex,
                            errorMessage = "LED $ledIndex failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun toggleMotor() {
        val currentMotorState = _uiState.value.status?.dcMotor ?: false
        val targetState = !currentMotorState

        viewModelScope.launch {
            _uiState.update { it.copy(isMotorLoading = true) }
            val result = repository.setMotor(targetState)
            result.fold(
                onSuccess = { newState ->
                    _uiState.update { state ->
                        state.copy(
                            status = state.status?.copy(dcMotor = newState),
                            isMotorLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isMotorLoading = false,
                            errorMessage = "Motor failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun openDoor() {
        setServoAngle(preferences.doorOpenAngle)
    }

    fun closeDoor() {
        setServoAngle(preferences.doorCloseAngle)
    }

    fun setServoAngle(angle: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isServoLoading = true) }
            val result = repository.setServo(angle)
            result.fold(
                onSuccess = { newAngle ->
                    _uiState.update { state ->
                        val doorState = if (newAngle >= 45) "OPEN" else "CLOSED"
                        state.copy(
                            status = state.status?.copy(
                                servoAngle = newAngle,
                                doorCommandedState = doorState
                            ),
                            isServoLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isServoLoading = false,
                            errorMessage = "Servo failed: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun testConnection(ip: String, port: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, testResult = null) }
            // Temporarily test with the requested IP/Port
            val prevIp = preferences.esp32Ip
            val prevPort = preferences.esp32Port
            val prevSim = preferences.isSimulationMode

            preferences.esp32Ip = ip
            preferences.esp32Port = port
            preferences.isSimulationMode = false
            repository.resetService()

            val result = repository.fetchStatus()
            val message = result.fold(
                onSuccess = { "Success! Connected to ESP32 at $ip:$port. Uptime: ${it.uptimeMs / 1000}s" },
                onFailure = { "Connection failed: ${it.message}" }
            )

            // Restore previous or keep based on user action
            preferences.esp32Ip = prevIp
            preferences.esp32Port = prevPort
            preferences.isSimulationMode = prevSim
            repository.resetService()

            _uiState.update {
                it.copy(
                    isTestingConnection = false,
                    testResult = message
                )
            }
        }
    }

    fun saveSettings(
        ip: String,
        port: Int,
        isSimulationMode: Boolean,
        openAngle: Int,
        closeAngle: Int,
        refreshIntervalMs: Long
    ) {
        preferences.esp32Ip = ip.trim()
        preferences.esp32Port = port
        preferences.isSimulationMode = isSimulationMode
        preferences.doorOpenAngle = openAngle
        preferences.doorCloseAngle = closeAngle
        preferences.refreshIntervalMs = refreshIntervalMs
        repository.resetService()

        _uiState.update {
            it.copy(
                currentIp = preferences.esp32Ip,
                currentPort = preferences.esp32Port,
                isSimulationMode = preferences.isSimulationMode,
                doorOpenAngle = preferences.doorOpenAngle,
                doorCloseAngle = preferences.doorCloseAngle,
                testResult = "Settings saved successfully!"
            )
        }

        refreshManually()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

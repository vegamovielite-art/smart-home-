package com.example.data.repository

import android.os.SystemClock
import com.example.data.model.ApiResponse
import com.example.data.model.Esp32Status
import com.example.data.model.LedCommand
import com.example.data.model.MotorCommand
import com.example.data.model.ServoCommand
import com.example.data.network.Esp32ApiService
import com.example.data.preferences.SmartHomePreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class SmartHomeRepository(private val preferences: SmartHomePreferences) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private var currentBaseUrl: String = ""
    private var cachedApiService: Esp32ApiService? = null

    // Simulation state for offline / demo testing
    private var simStatus = Esp32Status(
        esp32Ip = preferences.esp32Ip,
        wifiConnected = true,
        led1 = false,
        led2 = true,
        led3 = false,
        servoAngle = 0,
        doorCommandedState = "CLOSED",
        dcMotor = false,
        ir1Detected = false,
        ir2Detected = false,
        ultrasonicDistanceCm = 42.5f,
        ultrasonicValid = true,
        pirMotion = false,
        uptimeMs = 120000L
    )

    private fun getApiService(): Esp32ApiService {
        val baseUrl = preferences.getBaseUrl()
        if (cachedApiService != null && currentBaseUrl == baseUrl) {
            return cachedApiService!!
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .retryOnConnectionFailure(false)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        currentBaseUrl = baseUrl
        val service = retrofit.create(Esp32ApiService::class.java)
        cachedApiService = service
        return service
    }

    suspend fun fetchStatus(): Result<Esp32Status> = withContext(Dispatchers.IO) {
        if (preferences.isSimulationMode) {
            // Emulate slight realistic sensor fluctuation in simulation mode
            val jitter = (Random.nextFloat() * 1.6f) - 0.8f
            val newDist = (simStatus.ultrasonicDistanceCm + jitter).coerceIn(4.0f, 250.0f)
            val randomPir = if (Random.nextInt(10) == 0) !simStatus.pirMotion else simStatus.pirMotion
            val randomIr1 = if (Random.nextInt(15) == 0) !simStatus.ir1Detected else simStatus.ir1Detected

            simStatus = simStatus.copy(
                esp32Ip = preferences.esp32Ip,
                wifiConnected = true,
                ultrasonicDistanceCm = (newDist * 10).toInt() / 10f,
                ultrasonicValid = true,
                pirMotion = randomPir,
                ir1Detected = randomIr1,
                uptimeMs = simStatus.uptimeMs + 2000L
            )
            return@withContext Result.success(simStatus)
        }

        try {
            val status = getApiService().getStatus()
            Result.success(status)
        } catch (e: SocketTimeoutException) {
            Result.failure(IOException("Connection timed out to ${preferences.esp32Ip}. Check if ESP32 is powered on."))
        } catch (e: ConnectException) {
            Result.failure(IOException("Cannot reach ${preferences.esp32Ip}:${preferences.esp32Port}. Verify Wi-Fi network."))
        } catch (e: Exception) {
            Result.failure(IOException(e.message ?: "Network error communicating with ESP32"))
        }
    }

    suspend fun setLed(ledIndex: Int, state: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        if (preferences.isSimulationMode) {
            simStatus = when (ledIndex) {
                1 -> simStatus.copy(led1 = state)
                2 -> simStatus.copy(led2 = state)
                3 -> simStatus.copy(led3 = state)
                else -> simStatus
            }
            return@withContext Result.success(state)
        }

        try {
            val response = getApiService().controlLed(LedCommand(led = ledIndex, state = state))
            if (response.success) {
                Result.success(state)
            } else {
                Result.failure(IOException(response.error ?: "ESP32 rejected LED command"))
            }
        } catch (e: Exception) {
            Result.failure(IOException(e.message ?: "Failed to send LED command"))
        }
    }

    suspend fun setServo(angle: Int): Result<Int> = withContext(Dispatchers.IO) {
        val clampedAngle = angle.coerceIn(0, 180)
        if (preferences.isSimulationMode) {
            val doorState = if (clampedAngle >= 45) "OPEN" else "CLOSED"
            simStatus = simStatus.copy(
                servoAngle = clampedAngle,
                doorCommandedState = doorState
            )
            return@withContext Result.success(clampedAngle)
        }

        try {
            val response = getApiService().controlServo(ServoCommand(angle = clampedAngle))
            if (response.success) {
                Result.success(clampedAngle)
            } else {
                Result.failure(IOException(response.error ?: "ESP32 rejected servo command"))
            }
        } catch (e: Exception) {
            Result.failure(IOException(e.message ?: "Failed to send Servo command"))
        }
    }

    suspend fun setMotor(state: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        if (preferences.isSimulationMode) {
            simStatus = simStatus.copy(dcMotor = state)
            return@withContext Result.success(state)
        }

        try {
            val response = getApiService().controlMotor(MotorCommand(state = state))
            if (response.success) {
                Result.success(state)
            } else {
                Result.failure(IOException(response.error ?: "ESP32 rejected motor command"))
            }
        } catch (e: Exception) {
            Result.failure(IOException(e.message ?: "Failed to send Motor command"))
        }
    }

    fun resetService() {
        cachedApiService = null
    }
}

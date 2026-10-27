package com.example.data.network

import com.example.data.model.ApiResponse
import com.example.data.model.Esp32Status
import com.example.data.model.LedCommand
import com.example.data.model.MotorCommand
import com.example.data.model.ServoCommand
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface Esp32ApiService {

    @GET("api/status")
    suspend fun getStatus(): Esp32Status

    @POST("api/led")
    suspend fun controlLed(@Body command: LedCommand): ApiResponse

    @POST("api/servo")
    suspend fun controlServo(@Body command: ServoCommand): ApiResponse

    @POST("api/motor")
    suspend fun controlMotor(@Body command: MotorCommand): ApiResponse
}

package com.example.p

import AirQualityResponse
import SensorData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SensorApiService {
    @POST("https://0f6f-210-93-86-104.ngrok-free.app/echo")
    fun postSensorData(@Body sensorData: SensorData): Call<ServerResponse>
}

package com.example.p

import AirQualityResponse
import SensorData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SensorApiService {
    @POST("https://124d-210-93-86-104.ngrok-free.app/heart-rate")
    fun postSensorData(@Body sensorData: SensorData): Call<ServerResponse>
}

package com.example.p.ApiService

import SensorData
import com.example.p.Response.ServerResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SensorApiService {
    @POST("https://124d-210-93-86-104.ngrok-free.app/heart-rate")
    fun postSensorData(@Body sensorData: SensorData): Call<ServerResponse>
}

package com.example.p.ApiService

import SensorData
import com.example.p.Response.ServerResponse
import com.example.p.Response.YouTubeResponse
import com.example.p.Response.YouTubeUrlResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SensorApiService {
    @POST("https://1a4a-210-93-86-104.ngrok-free.app/heart-rate")
    fun postSensorData(@Body sensorData: SensorData): Call<YouTubeUrlResponse> // ServerResponse 대신 YouTubeUrlResponse 사용
}

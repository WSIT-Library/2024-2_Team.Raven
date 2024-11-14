package com.example.p

import AirQualityResponse
import SensorData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SensorApiService {
    @POST("https://124d-210-93-86-104.ngrok-free.app/echo")
    fun postSensorData(@Body sensorData: SensorData): Call<ServerResponse>
    @POST("https://124d-210-93-86-104.ngrok-free.app/echo") // 여기에 실제 API 엔드포인트를 입력하세요.
    fun sendDeviceId(@Body request: DeviceIdRequest): Call<Void> // 서버 응답 타입에 따라 Void 대신 적절한 클래스 사용
}

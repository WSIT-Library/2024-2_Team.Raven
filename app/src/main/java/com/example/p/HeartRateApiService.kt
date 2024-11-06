// HeartRateApiService.kt
package com.example.p

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.p.HeartRateResponse

interface HeartRateApiService {
    @POST("https://eb3d-210-93-86-104.ngrok-free.app/send-data") // 엔드포인트 경로를 적절히 설정
    fun postHeartRate(@Body heartRateData: HeartRateData): Call<HeartRateResponse>
}

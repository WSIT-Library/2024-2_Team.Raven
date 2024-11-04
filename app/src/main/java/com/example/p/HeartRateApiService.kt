// HeartRateApiService.kt
package com.example.p

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.p.HeartRateResponse

interface HeartRateApiService {
    @POST("https://0f6f-210-93-86-104.ngrok-free.app/echo") // 엔드포인트 경로를 적절히 설정
    fun postHeartRate(@Body heartRateData: HeartRateData): Call<HeartRateResponse>
}

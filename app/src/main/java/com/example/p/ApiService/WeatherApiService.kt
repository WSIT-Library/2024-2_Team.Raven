package com.example.p.ApiService

import com.example.p.Response.WeatherCondition
import com.example.p.Response.WeatherMain
import com.example.p.Response.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>  // 결과를 WeatherApiResult로 받기
}

data class WeatherApiResult(
    val main: WeatherMain,  // 온도
    val weather: List<WeatherCondition>  // 날씨 상태 설명
)
package com.example.p

import com.example.p.ApiService.AirQualityApiService
import com.example.p.ApiService.SensorApiService
import com.example.p.ApiService.WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherApiService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }

    val airQualityApiService: AirQualityApiService by lazy {
        retrofit.create(AirQualityApiService::class.java)
    }

    val sensorApiService: SensorApiService by lazy {
        retrofit.create(SensorApiService::class.java)
    }

    val youtubeRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/youtube/v3/") // 유튜브 API의 base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
package com.example.p

import com.example.p.ApiService.AirQualityApiService
import com.example.p.ApiService.SensorApiService
import com.example.p.ApiService.WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"
    private const val HEART_RATE_BASE_URL = "https://d557-210-93-86-104.ngrok-free.app/" // 적절한 URL로 변경


    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val heartRateRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(HEART_RATE_BASE_URL)
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
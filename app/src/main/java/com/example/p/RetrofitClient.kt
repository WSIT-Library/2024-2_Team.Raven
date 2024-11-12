package com.example.p

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"
    private const val HEART_RATE_BASE_URL = "https://cccd-210-93-86-104.ngrok-free.app/"  // 적절한 URL로 변경
    private const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"

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

    private val youtubeRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(YOUTUBE_BASE_URL)
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

    // 심박수 API 서비스
    val heartRateApiService: HeartRateApiService by lazy {
        heartRateRetrofit.create(HeartRateApiService::class.java)
    }

    // YouTube API 서비스
    val youtubeApiService: YouTubeApiService by lazy {
        youtubeRetrofit.create(YouTubeApiService::class.java)
    }
}

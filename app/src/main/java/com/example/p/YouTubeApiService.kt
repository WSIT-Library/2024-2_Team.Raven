package com.example.p

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("videos")
    fun getVideoDetails(
        @Query("part") part: String,
        @Query("id") videoId: String,
        @Query("key") apiKey: String
    ): Call<YouTubeResponse>
    fun getVideoUrl(): Call<YouTubeUrlResponse>
}


// 서버에서 유튜브 URL을 담아 반환하는 데이터 클래스
data class YouTubeUrlResponse(
    val videoUrl: String
)
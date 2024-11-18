package com.example.p.ApiService

import com.example.p.Response.YouTubeResponse
import com.example.p.Response.YouTubeUrlResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/*
interface YouTubeApiService {
    @GET("videos")
    fun getVideoDetails(
        @Query("part") part: String,
        @Query("id") videoId: String,
        @Query("key") apiKey: String
    ): Call<YouTubeResponse>
    // URL을 가져오는 메서드 추가
    @GET("echo") // 서버에서 URL을 반환하는 엔드포인트를 입력
    fun getVideoUrl(): Call<YouTubeUrlResponse>
}
*/

interface YouTubeApiService {
    @GET("https://124d-210-93-86-104.ngrok-free.app/echo")
    suspend fun getYouTubeUrl(): Response<YouTubeUrlResponse> // 서버에서 유튜브 URL을 받는 메서드

    @GET("videos")
    fun getVideoDetails(
        @Query("part") part: String,       // 원하는 정보 파트를 지정 (예: snippet)
        @Query("id") videoId: String,      // 비디오 ID
        @Query("key") apiKey: String       // API 키
    ): Call<YouTubeResponse>
}
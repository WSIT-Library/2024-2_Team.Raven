package com.example.p.Response

data class WeatherResponse(
    val main: WeatherMain,  // 온도 데이터
    val weather: List<WeatherCondition>  // 날씨 설명 리스트
)

data class WeatherMain(
    val temp: Double  // 온도 값
)


data class WeatherCondition(
    val description: String  // 날씨 설명 (예: 맑음, 비, 구름 등)
)
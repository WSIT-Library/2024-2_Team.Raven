package com.example.p

data class SensorData(
    val heartRate: String,
    val CO: String,
    val Alcohol: String,
    val CO2: String,
    val Tolueno: String,
    val NH4: String,
    val Acetona: String,
    val temperature: String,
    val humidity: String,
    val deviceId: String  // 추가된 deviceId 필드
)

data class ServerResponse(
    val success: Boolean,
    val message: String,
    val data: SensorData?  // 데이터가 있을 경우 SensorData로 파싱
)


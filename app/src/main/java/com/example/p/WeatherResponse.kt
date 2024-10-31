data class WeatherResponse(
    val main: WeatherMain
)

data class WeatherMain(
    val temp: Double // 온도 값
)
data class AirQualityResponse(
    val coord: Coord,
    val list: List<AirQualityData>
)

data class Coord(
    val lat: Double,
    val lon: Double
)

data class AirQualityData(
    val main: Main,
    val components: Components,
    val dt: Long
)

data class Main(
    val aqi: Int // Air Quality Index
)

data class Components(
    val co: Double, // Carbon Monoxide
    val no: Double, // Nitrogen Monoxide
    val no2: Double, // Nitrogen Dioxide
    val o3: Double, // Ozone
    val so2: Double, // Sulfur Dioxide
    val pm2_5: Double, // Particulate Matter 2.5
    val pm10: Double, // Particulate Matter 10
    val nh3: Double // Ammonia
)

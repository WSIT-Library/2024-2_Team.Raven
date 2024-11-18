package com.example.p.Fragments

import com.example.p.Response.AirQualityResponse
import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.p.R
import com.example.p.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class Fragment2 : Fragment() {
    private val apiKey = "9429534b80a3def05a32e862c426f83c"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var textViewBluetoothData: TextView
    private lateinit var textViewInside: TextView
    private lateinit var textViewOutside: TextView
    private var airQualityAQI: Int? = null
    private var bluetoothAQI: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment2_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val buttonGetAirQuality: Button = view.findViewById(R.id.button_get_air_quality)
        val textViewAirQuality: TextView = view.findViewById(R.id.text_view_air_quality)
        val textViewLocation: TextView = view.findViewById(R.id.text_view_location)
        textViewBluetoothData = view.findViewById(R.id.text_view_bluetooth_data)
        textViewInside = view.findViewById(R.id.text_view_inside)
        textViewOutside = view.findViewById(R.id.text_view_outside)

        buttonGetAirQuality.setOnClickListener {
            getCurrentLocation { lat, lon ->
                getAirQuality(lat, lon, textViewAirQuality)
                getLocationInfo(lat, lon, textViewLocation)
            }
        }
        // 블루투스 데이터 표시 초기화
        displayBluetoothData()
    }

    private fun displayBluetoothData() {
        val activity = activity as? MainActivity
        activity?.setBluetoothDataListener { data ->
            val co = try {
                data.CO.toFloat()
            } catch (e: NumberFormatException) {
                0f
            }

            bluetoothAQI = calculateAQI(co)

            // Bluetooth 데이터와 AQI 값을 텍스트뷰에 출력
            textViewBluetoothData.text = """
            AQI : $bluetoothAQI
            CO : $co
            Alcohol : ${data.Alcohol}
            CO2 : ${data.CO2}
            Tolueno : ${data.Tolueno}
            NH4 : ${data.NH4}
            Acetona : ${data.Acetona}
            온도 : ${data.temperature}
            습도  : ${data.humidity}
        """.trimIndent()

            // AQI 값 비교
            compareAQIValues()
        }
    }

    private fun calculateAQI(co: Float): Int {
        return when {
            co <= 4.4 -> ((co / 4.4) * 50).toInt()
            co <= 9.4 -> ((co - 4.4) / 5 * 50 + 50).toInt()
            co <= 12.4 -> ((co - 9.4) / 3 * 50 + 100).toInt()
            co <= 15.4 -> ((co - 12.4) / 3 * 50 + 150).toInt()
            co <= 30.4 -> ((co - 15.4) / 15 * 50 + 200).toInt()
            else -> 300
        }
    }

    private fun getCurrentLocation(onSuccess: (Double, Double) -> Unit) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(OnSuccessListener<Location?> { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                onSuccess(lat, lon)
            } else {
                Toast.makeText(requireContext(), "위치를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // getAirQuality 함수 수정
    private fun getAirQuality(lat: Double, lon: Double, textView: TextView) {
        val call = RetrofitClient.airQualityApiService.getAirQuality(lat, lon, apiKey)
        call.enqueue(object : Callback<AirQualityResponse> {
            override fun onResponse(call: Call<AirQualityResponse>, response: Response<AirQualityResponse>) {
                if (response.isSuccessful) {
                    val airQualityResponse = response.body()
                    val components = airQualityResponse?.list?.get(0)?.components
                    val aqi = airQualityResponse?.list?.get(0)?.main?.aqi
                    airQualityAQI = aqi // AQI 값을 저장

                    if (components != null) {
                        // 기존 데이터 출력
                        textView.text = """
                        AQI : $aqi
                        CO : ${components.co} ppm
                        NO : ${components.no} ppm
                        NO2 : ${components.no2} ppm
                        O3 : ${components.o3} ppm
                        SO2 : ${components.so2} ppm
                        PM2.5 : ${components.pm2_5} µg/m³
                        PM10 : ${components.pm10} µg/m³
                        NH3 : ${components.nh3} ppm
                    """.trimIndent()
                    } else {
                        textView.text = "No components data available."
                    }

                    // AQI 값 비교 호출
                    compareAQIValues()
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.code()} ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AirQualityResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to retrieve data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




    private fun getLocationInfo(lat: Double, lon: Double, textView: TextView) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(lat, lon, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val thoroughfare = address.thoroughfare ?: ""
            val subLocality = address.subLocality ?: ""
            val locality = address.locality ?: ""
            val adminArea = address.adminArea ?: ""
            val locationInfo = buildString {
                if (adminArea.isNotEmpty() && locality.isEmpty()) append("현재 $adminArea ")
                if (thoroughfare.isNotEmpty()) append("$thoroughfare ")
                if (locality.isNotEmpty()) append("$locality ")
                if (subLocality.isNotEmpty()) append("$subLocality 의 현재 공기질 상태에요.")
            }
            textView.text = locationInfo
        } else {
            textView.text = "주소 정보를 가져올 수 없습니다."
        }
    }

    private fun compareAQIValues() {
        if (airQualityAQI != null && bluetoothAQI != null) {
            val comparisonResult = when {
                bluetoothAQI!! > airQualityAQI!! -> "안 좋은 편이에요."
                bluetoothAQI!! < airQualityAQI!! -> "좋은 편이에요."
                else -> "양호한 편이에요."
            }
            textViewOutside.text = comparisonResult
        }
    }
}

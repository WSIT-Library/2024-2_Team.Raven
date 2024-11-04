package com.example.p

import AirQualityResponse
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
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
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
            textViewBluetoothData.text = """
                CO: ${data.CO}
                Alcohol: ${data.Alcohol}
                CO2: ${data.CO2}
                Tolueno: ${data.Tolueno}
                NH4: ${data.NH4}
                Acetona: ${data.Acetona}
                온도: ${data.temperature}
                습도: ${data.humidity}
            """.trimIndent()
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

    private fun getAirQuality(lat: Double, lon: Double, textView: TextView) {
        val call = RetrofitClient.airQualityApiService.getAirQuality(lat, lon, apiKey)
        call.enqueue(object : Callback<AirQualityResponse> {
            override fun onResponse(call: Call<AirQualityResponse>, response: Response<AirQualityResponse>) {
                Log.d("Request URL", "https://api.openweathermap.org/data/2.5/air_pollution?lat=$lat&lon=$lon&appid=$apiKey")
                if (response.isSuccessful) {
                    val airQualityResponse = response.body()
                    val components = airQualityResponse?.list?.get(0)?.components
                    if (components != null) {
                        val co = components.co
                        val no = components.no
                        val no2 = components.no2
                        val o3 = components.o3
                        val so2 = components.so2
                        val pm2_5 = components.pm2_5
                        val pm10 = components.pm10
                        val nh3 = components.nh3
                        textView.text = """
                            - 현재 공기질 상황 -
                            CO : $co
                            NO : $no
                            NO2 : $no2
                            O3 : $o3
                            SO2 : $so2
                            PM2.5 : $pm2_5
                            PM10 : $pm10
                            NH3 : $nh3
                        """.trimIndent()
                    } else {
                        textView.text = "No components data available."
                    }
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

            val thoroughfare = address.thoroughfare ?: "" // 도로명
            val subLocality = address.subLocality ?: "" // 동/읍/면
            val locality = address.locality ?: "" // 시/군/구
            val adminArea = address.adminArea ?: "" // 도/광역시
            val country = address.countryName ?: "" // 나라 이름

            // 필요한 정보를 조합해서 표시
            val locationInfo = buildString {
                if (adminArea.isNotEmpty() && locality.isEmpty()) append("현재 $adminArea ")
                if (thoroughfare.isNotEmpty()) append("$thoroughfare ")
                if (locality.isNotEmpty()) append("$locality ")
                if (subLocality.isNotEmpty()) append("$subLocality 의 현재 공기질 상태에요.") // + 제거
            }

            textView.text = locationInfo
        } else {
            textView.text = "주소 정보를 가져올 수 없습니다."
        }
    }

}


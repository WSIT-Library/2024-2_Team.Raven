package com.example.p

import WeatherResponse
import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class Fragment1 : Fragment() {

    private lateinit var viewModel: BluetoothViewModel
    private lateinit var textViewReceive: TextView
    private lateinit var textViewLocation: TextView // 위치 정보를 표시할 TextView
    private lateinit var textViewTemperature: TextView // 온도 표시할 TextView
    private lateinit var textViewTemperatureCar: TextView // 온도 표시할 TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val apiKey = "9429534b80a3def05a32e862c426f83c" // OpenWeather API 키

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment1_layout, container, false)
        textViewReceive = view.findViewById(R.id.textViewReceive) // 수신 데이터 표시할 TextView
        textViewLocation = view.findViewById(R.id.textViewLocation) // 위치 정보를 표시할 TextView
        textViewTemperature = view.findViewById(R.id.textViewTemperature) // 온도 표시할 TextView
        textViewTemperatureCar = view.findViewById(R.id.textViewTemperature_Car) // 온도 표시할 TextView

        viewModel = ViewModelProvider(requireActivity()).get(BluetoothViewModel::class.java)

        // ViewModel의 데이터 변경을 관찰하여 TextView에 업데이트
        viewModel.receivedData.observe(viewLifecycleOwner, Observer { data ->
            textViewReceive.text = data
        })

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 현재 위치와 온도를 가져옴
        getCurrentLocationAndTemperature()
        viewModel.heartRate.observe(viewLifecycleOwner, Observer { heartRate ->
            textViewReceive.text = "BPM: $heartRate"
        })
        // ViewModel의 temperature 데이터를 관찰하고 textViewTemperatureCar에 업데이트
        viewModel.temperature.observe(viewLifecycleOwner, Observer { temperature ->
            textViewTemperatureCar.text = "$temperature°C"
        })

        return view
    }

    private fun getCurrentLocationAndTemperature() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 권한이 없으면 권한 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1000
            )
            return
        }

        // 위치 정보 가져오기
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                getTemperature(latitude, longitude) // 온도 데이터 가져오기

                // 위치 정보 표시 수정
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val thoroughfare = address.thoroughfare // 도로명 또는 거리명 (예: 자양동)
                    val subLocality = address.subLocality // 동/읍/면 (예: 관저동)
                    val locality = address.locality // 시/군/구 (예: 대전광역시)
                    val adminArea = address.adminArea // 도/광역시 (예: 충청남도, 서울특별시)
                    val subAdminArea = address.subAdminArea // 시/군/구 (예: 중구, 서구)
                    val country = address.countryName // 나라 이름

                    // 시/군/구 정보를 locality, subAdminArea, adminArea 순서로 우선 사용
                    val city = locality ?: subAdminArea ?: adminArea ?: ""

                    // 표시할 위치 정보 생성
                    val locationInfo = when {
                        thoroughfare != null && subLocality != null -> "$country $city $subLocality $thoroughfare"
                        subLocality != null -> "$country $city $subLocality"
                        city.isNotEmpty() -> "$country $city"
                        else -> "현재 위치 정보를 가져올 수 없습니다."
                    }
                    textViewLocation.text = locationInfo
                } else {
                    textViewLocation.text = "주소 정보를 가져올 수 없습니다."
                }

            } else {
                textViewLocation.text = "위치 정보를 가져올 수 없습니다."
            }
        }.addOnFailureListener {
            textViewLocation.text = "위치 정보를 가져오는 중 오류가 발생했습니다."
        }
    }

    // 온도 데이터를 받아오는 함수
    private fun getTemperature(lat: Double, lon: Double) {
        val call = RetrofitClient.weatherApiService.getWeather(lat, lon, apiKey)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    val temperature = weatherResponse?.main?.temp // 섭씨 온도
                    if (temperature != null) {
                        // 켈빈에서 섭씨로 변환
                        val celsius = temperature - 273.15
                        textViewTemperature.text = String.format("%.1f°C", celsius) // 소수점 한자리까지 출력
                    } else {
                        textViewTemperature.text = "온도 정보를 가져올 수 없습니다."
                    }
                } else {
                    Log.e("API Error", "Error code: ${response.code()}, Message: ${response.message()}")
                    Toast.makeText(requireContext(), "Error: ${response.code()} ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to retrieve temperature: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
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
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.p.RetrofitClient.youtubeApiService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.inject.Provider
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.Locale

class Fragment1 : Fragment() {
    private var isUserSeeking = false
    private lateinit var viewModel: BluetoothViewModel
    private lateinit var textViewReceive: TextView
    private lateinit var textViewLocation: TextView
    private lateinit var textViewTemperature: TextView
    private lateinit var textViewTemperatureCar: TextView
    private lateinit var AirQualityValue: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var replayButton: Button

    private lateinit var seekBar: SeekBar
    private lateinit var currentTimeTextView: TextView
    private lateinit var totalTimeTextView: TextView
    private var youTubePlayer: YouTubePlayer? = null // nullable로 선언하여 초기화 전 null을 허용

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
        AirQualityValue = view.findViewById(R.id.airQualityValue) // 공기질 표시할 TextView

        youTubePlayerView = view.findViewById(R.id.youtube_player_view)
        playButton = view.findViewById(R.id.playButton)
        pauseButton = view.findViewById(R.id.pauseButton)
        replayButton = view.findViewById(R.id.replayButton)
        // SeekBar와 TextView 초기화
        seekBar = view.findViewById(R.id.seekBar)
        currentTimeTextView = view.findViewById(R.id.currentTime)
        totalTimeTextView = view.findViewById(R.id.totalTime)

        val youTubePlayerView = view.findViewById<YouTubePlayerView>(R.id.youtube_player_view)
        lifecycle.addObserver(youTubePlayerView)

        val songTitleTextView = view.findViewById<TextView>(R.id.songTitle)
        songTitleTextView.isSelected = true // 슬라이드 효과를 위해 텍스트에 포커스를 설정합니다.

        val youtubeapiKey = "AIzaSyCH3y8aM6R7z183txFBk0DkWerLAcCD0sQ"
        val videoUrl = "https://www.youtube.com/watch?v=R7L2QEm-BUY"
        val videoId = extractVideoId(videoUrl)
        val thumbnailUrl = "https://img.youtube.com/vi/$videoId/0.jpg"

        // ImageView에 썸네일 로드
        val imageView = view.findViewById<ImageView>(R.id.albumCover)
        Glide.with(this)
            .load(thumbnailUrl)
            .into(imageView)

        // YouTube Player 설정
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                // YouTubePlayer 객체 초기화
                this@Fragment1.youTubePlayer = youTubePlayer

                // 비디오가 로드된 후 총 시간을 가져오기
                youTubePlayer.addListener(object : AbstractYouTubePlayerListener() {
                    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                        super.onVideoDuration(youTubePlayer, duration)

                        // 비디오의 총 시간을 가져옴
                        val totalMinutes = (duration / 60).toInt()  // 분
                        val totalSeconds = (duration % 60).toInt() // 초
                        totalTimeTextView.text = String.format("%02d:%02d", totalMinutes, totalSeconds)

                        // SeekBar의 최대값을 비디오 총 시간으로 설정
                        seekBar.max = duration.toInt()
                    }

                    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                        super.onCurrentSecond(youTubePlayer, second)

                        val currentMinutes = (second / 60).toInt() // 분
                        val currentSeconds = (second % 60).toInt() // 초
                        currentTimeTextView.text = String.format("%02d:%02d", currentMinutes, currentSeconds)

                        // SeekBar의 진행 상황 업데이트 (비디오 진행에 맞춰 업데이트)
                        // 이 부분은 사용자 입력에 의한 SeekBar 변경 시 자동 업데이트를 방지
                        if (!isUserSeeking) {
                            seekBar.progress = second.toInt()
                        }
                    }
                })


                // 서버에서 URL 받아오는 메서드 호출
                fetchYouTubeUrlFromServer()
            }
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // 사용자 입력으로 값을 변경하면 isUserSeeking 플래그를 true로 설정
                    isUserSeeking = true
                    // 비디오 재생 위치를 SeekBar의 값에 맞게 변경
                    youTubePlayer?.seekTo(progress.toFloat())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // SeekBar 터치 시작 시 플래그를 true로 설정
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // SeekBar 터치 종료 시 플래그를 false로 설정
                isUserSeeking = false
            }
        })


        // 서버에서 유튜브 URL을 받아오고 해당 URL로 비디오를 재생
        playButton.setOnClickListener {
            youTubePlayer?.play()
        }

        pauseButton.setOnClickListener {
            youTubePlayer?.pause()
        }

        replayButton.setOnClickListener {
            youTubePlayer?.seekTo(0f)
            youTubePlayer?.play()
        }


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
        // ViewModel에서 brightnessValue를 관찰하여 TextView에 업데이트
        viewModel.brightnessValue.observe(viewLifecycleOwner, Observer { brightness ->
            // progress 값을 AirQualityValue에 표시
            AirQualityValue.text = "밝기 : $brightness"
        })
        return view
    }

    // 서버에서 유튜브 URL을 받아와서 비디오를 로드하는 메서드
    private fun fetchYouTubeUrlFromServer() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = youtubeApiService.getYouTubeUrl()
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Youtube to server 연결 성공", Toast.LENGTH_SHORT).show()
                    val responseBody = response.body()
                    Log.e("youtube Response", responseBody.toString())  // 서버 응답 본문 로그
                    val videoUrl = responseBody?.url
                   // val videoUrl = "https://www.youtube.com/watch?v=Y8YCGVDCpNY"

                    Log.e("youtube URL", "URL:" + videoUrl.toString())
                    videoUrl?.let {
                        handleYouTubeUrlFromServer(it)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch URL", Toast.LENGTH_SHORT).show()
                    Log.e("YouTube API", "Response not successful: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace() // 예외의 상세한 스택 트레이스를 로그에 출력
                Log.e("YouTube API", "Error fetching URL: ${e.message}")
                Toast.makeText(requireContext(), "Error fetching URL", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 서버에서 받은 유튜브 URL을 받아서 비디오 ID를 추출하고 재생하는 메서드
    fun handleYouTubeUrlFromServer(videoUrl: String) {
        Log.e("handleYouTubeUrlFromServer", "handleYouTubeUrlFromServer 연결 성공")
        val videoId = extractVideoId(videoUrl)
        Log.e("YouTube ID", videoId.toString())  // 추출된 비디오 ID 로그
        videoId?.let { id ->
            getVideoDetailsFromYouTube(id, "AIzaSyCH3y8aM6R7z183txFBk0DkWerLAcCD0sQ")
            youTubePlayer?.loadVideo(id, 0f) // 비디오 ID를 로드하여 재생
        }
    }

    // YouTube API로 비디오 정보 가져오기
    private fun getVideoDetailsFromYouTube(videoId: String, apiKey: String) {
        val service = RetrofitClient.youtubeApiService

        // 비디오 정보 API 호출
        service.getVideoDetails("snippet", videoId, apiKey).enqueue(object : Callback<YouTubeResponse> {
            override fun onResponse(call: Call<YouTubeResponse>, response: Response<YouTubeResponse>) {
                if (response.isSuccessful) {
                    val videoDetails = response.body()?.items?.firstOrNull()?.snippet
                    videoDetails?.let {
                        // 비디오 제목과 채널 이름 추출
                        val songTitle = it.title
                        val artistName = it.channelTitle

                        // 썸네일 URL 추출
                        val thumbnailUrl = it.thumbnails?.high?.url // high 품질 썸네일 URL

                        // 메인 스레드에서 UI 업데이트
                        activity?.runOnUiThread {
                            val songTitleTextView = view?.findViewById<TextView>(R.id.songTitle)
                            val artistNameTextView = view?.findViewById<TextView>(R.id.artistName)
                            val thumbnails = view?.findViewById<ImageView>(R.id.albumCover)

                            // 텍스트뷰에 제목과 아티스트 이름 설정
                            songTitleTextView?.text = songTitle
                            artistNameTextView?.text = artistName

                            // 썸네일 이미지 로드 (Glide 사용)
                            if (!thumbnailUrl.isNullOrEmpty()) {
                                if (thumbnails != null) {
                                    Glide.with(requireContext())
                                        .load(thumbnailUrl) // String 타입의 URL을 Glide에 전달
                                        .into(thumbnails)
                                } // ImageView에 썸네일 로드
                            }

                        }
                    }
                } else {
                    // API 호출 실패 시 처리
                    Log.e("YouTube API", "Error fetching video details")
                    Toast.makeText(requireContext(), "Error fetching video details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<YouTubeResponse>, t: Throwable) {
                // 실패 시 에러 처리
                Log.e("YouTube API", "Failure: ${t.message}")
                Toast.makeText(requireContext(), "Failed to load YouTube video details", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 비디오 ID 추출
    private fun extractVideoId(url: String): String? {
        val regex = "v=([^&]*)".toRegex()
        val match = regex.find(url)
        return match?.groupValues?.get(1)
    }
    //주변 공기질 및 온습도 가져오는 코드
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
                // 네트워크 오류 처리
                textViewTemperature.text = "온도 데이터를 가져오는 중 오류가 발생했습니다."
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

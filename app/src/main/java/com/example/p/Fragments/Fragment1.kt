package com.example.p.Fragments
import SensorData

import com.example.p.Response.WeatherResponse
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.p.ViewModel.BluetoothViewModel
import com.example.p.R
import com.example.p.RetrofitClient
import com.example.p.RetrofitClient.youtubeApiService
import com.example.p.Response.ServerResponse
import com.example.p.Response.YouTubeResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.UUID

class Fragment1 : Fragment() {

    private val HC06_MAC_ADDRESS = "98:D3:91:FD:F6:02"  // HC-06의 MAC 주소
    private val UUID_HC06: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var textViewReceive: TextView
    private lateinit var textViewComment: TextView
    private lateinit var requestBluetoothPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var readBuffer: ByteArray  // 버퍼 선언
    private var readBufferPosition: Int = 0  // 버퍼 위치 초기화
    private var workerThread: Thread? = null
    private lateinit var viewModel: BluetoothViewModel // ViewModel 선언
    private lateinit var bluetoothViewModel: BluetoothViewModel

    private var isUserSeeking = false
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

    @RequiresApi(Build.VERSION_CODES.S)
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

        viewModel = ViewModelProvider(requireActivity()).get(BluetoothViewModel::class.java)

        // ImageView에 썸네일 로드
        val imageView = view.findViewById<ImageView>(R.id.albumCover)
        Glide.with(this)
            .load(thumbnailUrl)
            .into(imageView)
        // ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity()).get(BluetoothViewModel::class.java)

        // 블루투스 어댑터 초기화
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show()
            activity?.finish()
            return view
        }
        // 권한 요청 런처 설정
        requestBluetoothPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val isBluetoothConnectGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: false
            val isBluetoothScanGranted = permissions[Manifest.permission.BLUETOOTH_SCAN] ?: false

            if (isBluetoothConnectGranted && isBluetoothScanGranted) {
                connectToBluetoothDevice()
            } else {
                Toast.makeText(context, "블루투스 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 권한 확인 후 연결 시도
        checkBluetoothPermission()

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

    private fun checkBluetoothPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBluetoothPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } else {
            connectToBluetoothDevice()
        }
    }

    private fun connectToBluetoothDevice() {
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(context, "블루투스를 활성화해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(HC06_MAC_ADDRESS)
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID_HC06)
            bluetoothSocket?.connect()

            Toast.makeText(context, "HC-06에 연결되었습니다.", Toast.LENGTH_SHORT).show()
            // Save the socket in the ViewModel
            viewModel.bluetoothSocket = bluetoothSocket
            viewModel.setBluetoothConnected(true) // 연결 상태 업데이트
            // Start reading data
            readDataFromBluetooth()

        } catch (e: SecurityException) {
            Log.e("Bluetooth", "권한 오류: ${e.message}")
            //   dismissConnectingDialog()  // 연결 성공 시 팝업창 닫기
            Toast.makeText(context, "블루투스 권한이 없습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Bluetooth", "연결 실패: ${e.message}")
            //  dismissConnectingDialog()  // 연결 성공 시 팝업창 닫기
            viewModel.setBluetoothConnected(false) // 연결 실패 시 업데이트
            Toast.makeText(context, "연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readDataFromBluetooth() {
        val inputStream: InputStream? = bluetoothSocket?.inputStream
        if (inputStream == null)
        {
            return
        }

        val handler = Handler(Looper.getMainLooper())
        readBuffer = ByteArray(1024)
        readBufferPosition = 0
        workerThread = Thread {
            try {
                while (!Thread.currentThread().isInterrupted) {
                    val bytesAvailable = inputStream.available()
                    if (bytesAvailable > 0) {
                        val buffer = ByteArray(bytesAvailable)
                        inputStream.read(buffer)

                        for (i in 0 until bytesAvailable) {
                            val byte = buffer[i]
                            if (byte == '\n'.code.toByte()) {
                                val message = String(readBuffer, 0, readBufferPosition, Charsets.UTF_8).trim()
                                readBufferPosition = 0  // 버퍼 초기화

                                // 데이터 파싱
                                val dataParts = message.split("|")
                                if (dataParts.size == 9) {
                                    val heartRate = dataParts[0]
                                    val CO = dataParts[1]
                                    val Alcohol = dataParts[2]
                                    val CO2 = dataParts[3]
                                    val Tolueno = dataParts[4]
                                    val NH4 = dataParts[5]
                                    val Acetona = dataParts[6]
                                    val temperature = dataParts[7]
                                    val humidity = dataParts[8]


                                    // 모든 센서 데이터를 서버로 전송
                                    sendDataToServer(heartRate, CO, Alcohol, CO2, Tolueno, NH4, Acetona, temperature, humidity)

                                    // 심박수 표시
                                    handler.post {
                                        textViewReceive.text = "BPM : $heartRate"
                                        // ViewModel에 심박수 값 설정
                                        viewModel.setHeartRate(heartRate)
                                        // ViewModel에 온도 값 설정
                                        viewModel.updateTemperature(temperature)

                                        // Fragment2로 데이터 전송
                                        val mainActivity = activity as? MainActivity
                                        mainActivity?.onBluetoothDataReceived(CO, Alcohol, CO2, Tolueno, NH4, Acetona, temperature, humidity)

                                    }

                                } else {
                                    Log.e("Bluetooth", "유효하지 않은 데이터 형식: $message")
                                }
                            } else if (readBufferPosition < readBuffer.size) {
                                readBuffer[readBufferPosition++] = byte
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                handler.post {
                    Toast.makeText(context, "데이터 수신 중 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        workerThread?.start()
    }

    private fun sendDataToServer(
        heartRate: String,
        CO: String,
        Alcohol: String,
        CO2: String,
        Tolueno: String,
        NH4: String,
        Acetona: String,
        temperature: String,
        humidity: String
    ) {
        val deviceId = Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        val sensorData = SensorData(heartRate, CO, Alcohol, CO2, Tolueno, NH4, Acetona, temperature, humidity, deviceId)

        RetrofitClient.sensorApiService.postSensorData(sensorData).enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                if (response.isSuccessful) {
                    //Toast.makeText(context, "서버에 데이터 전송 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "서버 응답 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Toast.makeText(context, "서버 전송 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            workerThread?.interrupt()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
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

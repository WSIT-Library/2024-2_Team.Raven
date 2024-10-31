package com.example.p

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.util.*

class Fragment3 : Fragment() {

    private val HC06_MAC_ADDRESS = "98:D3:91:FD:F6:02"  // HC-06의 MAC 주소
    private val UUID_HC06: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var textViewReceive: TextView
    private lateinit var requestBluetoothPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var readBuffer: ByteArray  // 버퍼 선언
    private var readBufferPosition: Int = 0  // 버퍼 위치 초기화
    private var workerThread: Thread? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment3_layout, container, false)

        textViewReceive = view.findViewById(R.id.textViewReceive)

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
            readDataFromBluetooth()
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "권한 오류: ${e.message}")
            Toast.makeText(context, "블루투스 권한이 없습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Bluetooth", "연결 실패: ${e.message}")
            Toast.makeText(context, "연결에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readDataFromBluetooth() {
        val inputStream: InputStream? = bluetoothSocket?.inputStream
        if (inputStream == null) {
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
                                val message = String(readBuffer, 0, readBufferPosition, Charsets.UTF_8)
                                readBufferPosition = 0

                                // 여기서 심박수 값을 서버에 전송
                                sendDataToServer(message)

                                handler.post {
                                    textViewReceive.text = message
                                }
                            } else {
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

    private fun parseAirQualityData(data: String): String {
        val mq135 = data.split(",").getOrNull(0) ?: "N/A"
        return mq135
    }
    private fun sendDataToServer(heartRate: String) {
        val heartRateData = HeartRateData(heartRate)

        RetrofitClient.heartRateApiService.postHeartRate(heartRateData).enqueue(object : Callback<HeartRateResponse> {
            override fun onResponse(call: Call<HeartRateResponse>, response: Response<HeartRateResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "서버에 심박수 전송 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "서버 응답 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<HeartRateResponse>, t: Throwable) {
                Toast.makeText(context, "서버 전송 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bluetoothSocket?.close()
    }
}

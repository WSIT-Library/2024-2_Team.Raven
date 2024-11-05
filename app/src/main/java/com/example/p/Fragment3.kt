package com.example.p

import SensorData
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice.getDeviceId
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
import android.provider.Settings

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

            // Fragment4에 bluetoothSocket 전달
            val fragment4 = Fragment4()
            fragment4.setBluetoothSocket(bluetoothSocket!!)

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
                                        textViewReceive.text = "심박수: $heartRate"
                                    }

                                    // Fragment2로 데이터 전송
                                    val mainActivity = activity as? MainActivity
                                    mainActivity?.onBluetoothDataReceived(CO, Alcohol, CO2, Tolueno, NH4, Acetona, temperature, humidity)
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
                    Toast.makeText(context, "서버에 데이터 전송 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "서버 응답 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                Toast.makeText(context, "서버 전송 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bluetoothSocket?.close()
    }
}

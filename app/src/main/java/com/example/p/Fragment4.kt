package com.example.p

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.io.IOException
import java.io.OutputStream

class Fragment4 : Fragment() {

    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var brightnessSeekBar: SeekBar

    // Bluetooth 소켓을 Fragment3에서 전달받는 메서드
    fun setBluetoothSocket(socket: BluetoothSocket) {
        this.bluetoothSocket = socket
        Log.e("Bluetooth", "setBluetoothSocket 호출됨. BluetoothSocket 상태: isInitialized=${::bluetoothSocket.isInitialized}, isConnected=${bluetoothSocket.isConnected}")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment4_layout, container, false)
        brightnessSeekBar = view.findViewById(R.id.brightnessSeekBar)

        // SeekBar 변경 리스너 설정
        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // RGB 값을 계산하여 전송
                val rgbValue = calculateRGBValue(progress)
                Log.e("Bluetooth", "rgbValue값 데이터 전송")
                sendRGBValue(rgbValue)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        return view
    }

    private fun calculateRGBValue(brightness: Int): String {
        // RGB 값을 계산합니다. 여기서는 예시로 단순히 brightness 값을 R, G, B에 할당합니다.
        val r = brightness
        val g = brightness
        val b = brightness
        return "$r,$g,$b" // "R,G,B" 형식으로 반환
    }

    private fun sendRGBValue(rgbValue: String) {
        try {
            // Bluetooth 소켓이 초기화되고 연결된 상태인지 확인
            if (::bluetoothSocket.isInitialized && bluetoothSocket.isConnected) {
                try {
                    val value = "$rgbValue\n"
                    bluetoothSocket.outputStream.write(value.toByteArray())
                    Log.e("Bluetooth", "Bluetooth 소켓을 통해 아두이노로 데이터 전송: $rgbValue")
                } catch (e: IOException) {
                    Log.e("Bluetooth", "IOException 발생: ${e.message}")
                    e.printStackTrace()
                    Toast.makeText(context, "데이터 전송 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("Bluetooth", "Bluetooth 소켓 연결이 되어 있지 않거나, 초기화되지 않음.")
                Toast.makeText(context, "Bluetooth 소켓이 연결되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Bluetooth", "데이터 전송 중 오류 발생: ${e.message}")
            Toast.makeText(context, "데이터 전송 중 오류 발생", Toast.LENGTH_SHORT).show()
        }
    }
}


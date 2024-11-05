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
            // Bluetooth 소켓을 통해 아두이노로 데이터 전송
            if (::bluetoothSocket.isInitialized && bluetoothSocket.isConnected) {
                val value = "$rgbValue\n" // 아두이노에서 파싱하기 쉽게 줄바꿈 추가
                bluetoothSocket.outputStream.write(value.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "데이터 전송 중 오류 발생", Toast.LENGTH_SHORT).show()
        }
    }
}


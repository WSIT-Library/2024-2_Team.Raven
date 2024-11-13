package com.example.p

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.io.IOException
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter

class Fragment4 : Fragment() {

    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var brightnessSeekBar: SeekBar
    private lateinit var textViewbrightnessValueText: TextView
    private lateinit var lightImage: ImageView // ImageView 추가
    private var currentProgress: Int = 0 // SeekBar의 현재 progress 값을 저장

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment4_layout, container, false)
        brightnessSeekBar = view.findViewById(R.id.brightnessSeekBar)
        textViewbrightnessValueText = view.findViewById(R.id.brightnessValueText)
        lightImage = view.findViewById(R.id.Light_Image) // ImageView 초기화

        // Get the ViewModel and use the shared BluetoothSocket
        val viewModel = ViewModelProvider(requireActivity()).get(BluetoothViewModel::class.java)
        brightnessSeekBar.max = 100 // SeekBar 최대값을 100으로 설정

        bluetoothSocket = viewModel.bluetoothSocket

        if (bluetoothSocket == null || !bluetoothSocket!!.isConnected) {
            Toast.makeText(context, "Bluetooth not connected", Toast.LENGTH_SHORT).show()
        } else {
            brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    // progress 값을 임시로 저장
                    currentProgress = progress
                    viewModel.setBrightnessValue(progress)

                    // brightness 값을 TextView에 표시
                    textViewbrightnessValueText.text = progress.toString()

                    // ImageView 색상 필터 적용
                    updateImageColor(progress)

                    // RGB 값을 TextView에 표시
                    textViewbrightnessValueText.text = "밝기 : $progress"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // Do nothing
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // SeekBar가 멈췄을 때만 데이터를 전송
                    val rgbValue = calculateRGBValue(currentProgress)
                    sendRGBValue(rgbValue)
                }
            })
        }

        return view
    }

    // RGB 값 계산
    private fun calculateRGBValue(brightness: Int): String {
        val baseR = 255
        val baseG = 255
        val baseB = 255

        val r = (baseR * brightness / 255).coerceIn(0, 255)
        val g = (baseG * brightness / 255).coerceIn(0, 255)
        val b = (baseB * brightness / 255).coerceIn(0, 255)

        return "$r,$g,$b"
    }

    // ImageView 색상 필터 업데이트
    private fun updateImageColor(progress: Int) {
        val colorMatrix = ColorMatrix()

        // progress가 0이면 흑백, progress가 100이면 원래 이미지 색상
        if (progress == 0) {
            // 흑백 변환: 색상 값을 모두 0으로 설정
            colorMatrix.setSaturation(0f)
        } else {
            // progress가 0이 아니면 색상을 점차 복원
            val saturation = progress / 100f
            colorMatrix.setSaturation(saturation)
        }

        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        lightImage.colorFilter = colorFilter
    }

    // RGB 값을 아두이노로 전송
    private fun sendRGBValue(rgbValue: String) {
        try {
            if (bluetoothSocket != null && bluetoothSocket!!.isConnected) {
                val value = "$rgbValue\n"
                bluetoothSocket!!.outputStream.write(value.toByteArray())
                Log.d("Fragment4", "Data sent: $value")
            } else {
                Log.e("Fragment4", "Bluetooth socket is not connected or is null")
                Toast.makeText(context, "Bluetooth not connected", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error sending data", Toast.LENGTH_SHORT).show()
            Log.e("Fragment4", "Error sending data: ${e.message}")
        }
    }
}

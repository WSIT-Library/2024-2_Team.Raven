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
import androidx.lifecycle.ViewModelProvider
import java.io.IOException

class Fragment4 : Fragment() {

    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var brightnessSeekBar: SeekBar
    private lateinit var textViewbrightnessValueText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment4_layout, container, false)
        brightnessSeekBar = view.findViewById(R.id.brightnessSeekBar)
        textViewbrightnessValueText = view.findViewById(R.id.brightnessValueText)

        // Get the ViewModel and use the shared BluetoothSocket
        val viewModel = ViewModelProvider(requireActivity()).get(BluetoothViewModel::class.java)
        brightnessSeekBar.max = 100  // SeekBar 최대값을 100으로 설정

        bluetoothSocket = viewModel.bluetoothSocket

        if (bluetoothSocket == null || !bluetoothSocket!!.isConnected) {
            Toast.makeText(context, "Bluetooth not connected", Toast.LENGTH_SHORT).show()
        } else {
            brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    // progress 값을 ViewModel에 저장
                    viewModel.setBrightnessValue(progress)

                    // brightness 값을 TextView에 표시
                    textViewbrightnessValueText.text = progress.toString()
                    val rgbValue = calculateRGBValue(progress)
                    val brightnessValue = progress  // RGB 대신 progress 값을 바로 사용
                    sendRGBValue(rgbValue)

                    // RGB 값을 TextView에 표시
                    textViewbrightnessValueText.text = "밝기 : " + brightnessValue.toString()

                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        return view
    }

    private fun calculateRGBValue(brightness: Int): String {
        val baseR = 255
        val baseG = 255
        val baseB = 255

        val r = (baseR * brightness / 255).coerceIn(0, 255)
        val g = (baseG * brightness / 255).coerceIn(0, 255)
        val b = (baseB * brightness / 255).coerceIn(0, 255)

        return "$r,$g,$b"
    }

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

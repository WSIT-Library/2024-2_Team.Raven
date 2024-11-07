package com.example.p

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.io.IOException

class Fragment4 : Fragment() {

    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var brightnessSeekBar: SeekBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment4_layout, container, false)
        brightnessSeekBar = view.findViewById(R.id.brightnessSeekBar)

        // Get the ViewModel and use the shared BluetoothSocket
        val viewModel = ViewModelProvider(requireActivity()).get(BluetoothViewModel::class.java)
        bluetoothSocket = viewModel.bluetoothSocket

        if (bluetoothSocket == null || !bluetoothSocket!!.isConnected) {
            Toast.makeText(context, "Bluetooth not connected", Toast.LENGTH_SHORT).show()
        } else {
            brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val rgbValue = calculateRGBValue(progress)
                    sendRGBValue(rgbValue)
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

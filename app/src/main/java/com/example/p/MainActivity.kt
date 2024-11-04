package com.example.p

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.EditText
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.logging.Handler
import AirQualityResponse
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    private var bluetoothDataListener: ((AirQualityData) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        val button1: Button = findViewById(R.id.button_main)
        button1.setOnClickListener { viewPager.currentItem = 0 }

        val button2: Button = findViewById(R.id.button_air)
        button2.setOnClickListener { viewPager.currentItem = 1 }

        val button3: Button = findViewById(R.id.button_heart)
        button3.setOnClickListener { viewPager.currentItem = 2 }

        val button4: Button = findViewById(R.id.button_light)
        button4.setOnClickListener { viewPager.currentItem = 3 }
    }

    // Bluetooth 데이터를 Fragment2로 전달하는 리스너 설정
    fun setBluetoothDataListener(listener: (AirQualityData) -> Unit) {
        bluetoothDataListener = listener
    }

    // 블루투스 데이터 수신 처리
    fun onBluetoothDataReceived(CO: String, Alcohol: String, CO2: String, Tolueno: String, NH4: String, Acetona: String, temperature: String, humidity: String) {
        val data = AirQualityData(CO, Alcohol, CO2, Tolueno, NH4, Acetona, temperature, humidity)
        bluetoothDataListener?.invoke(data)
    }
}

data class AirQualityData(val CO: String, val Alcohol: String, val CO2: String, val Tolueno: String, val NH4: String, val Acetona: String, val temperature: String, val humidity: String)
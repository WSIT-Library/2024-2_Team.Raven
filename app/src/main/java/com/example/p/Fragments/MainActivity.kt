package com.example.p.Fragments
import android.content.Context
import android.provider.Settings
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.p.R
import com.example.p.ViewPagerAdapter


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
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}

data class AirQualityData(val CO: String, val Alcohol: String, val CO2: String, val Tolueno: String, val NH4: String, val Acetona: String, val temperature: String, val humidity: String)
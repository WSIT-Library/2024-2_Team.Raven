package com.example.p.ViewModel

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothViewModel : ViewModel() {
    private val _receivedData = MutableLiveData<String>()
    val receivedData: LiveData<String> get() = _receivedData

    val isBluetoothConnected = MutableLiveData<Boolean>(false)
    fun setBluetoothConnected(connected: Boolean) {
        isBluetoothConnected.value = connected
    }

    private val _heartRate = MutableLiveData<String>()
    val heartRate: LiveData<String> get() = _heartRate

    // 심박수 값을 업데이트하는 메서드
    fun setHeartRate(heartRate: String) {
        _heartRate.value = heartRate
    }

    var bluetoothSocket: BluetoothSocket? = null

    fun setReceivedData(value: String) {
        _receivedData.value = value
    }
    val temperature: MutableLiveData<String> = MutableLiveData()
    fun updateTemperature(temp: String) {
        temperature.value = temp
    }


    // SeekBar progress 값을 저장할 LiveData 추가
    private val _brightnessValue = MutableLiveData<Int>()
    val brightnessValue: LiveData<Int> get() = _brightnessValue

    // progress 값 변경 시 호출되는 함수
    fun setBrightnessValue(value: Int) {
        _brightnessValue.value = value
    }

}

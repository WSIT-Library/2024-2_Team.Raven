package com.example.p

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothViewModel : ViewModel() {
    private val _receivedData = MutableLiveData<String>()
    val receivedData: LiveData<String> get() = _receivedData

    private val _heartRate = MutableLiveData<String>()
    val heartRate: LiveData<String> get() = _heartRate

    var bluetoothSocket: BluetoothSocket? = null
    fun setHeartRate(value: String) {
        _heartRate.value = value
    }

    fun setReceivedData(value: String) {
        _receivedData.value = value
    }
    val temperature: MutableLiveData<String> = MutableLiveData()
    fun updateTemperature(temp: String) {
        temperature.value = temp
    }

}

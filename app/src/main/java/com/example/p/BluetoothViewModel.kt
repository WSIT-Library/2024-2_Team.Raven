package com.example.p

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothViewModel : ViewModel() {
    private val _receivedData = MutableLiveData<String>()
    val receivedData: LiveData<String> get() = _receivedData

    fun updateReceivedData(data: String) {
        _receivedData.value = data
    }
}

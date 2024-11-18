package com.example.p.Fragments

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.util.*
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.p.ViewModel.BluetoothViewModel
import com.example.p.R

class Fragment3 : Fragment() {

    private val HC06_MAC_ADDRESS = "98:D3:91:FD:F6:02"  // HC-06의 MAC 주소
    private val UUID_HC06: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var textViewReceive: TextView
    private lateinit var textViewComment: TextView
    private lateinit var requestBluetoothPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var readBuffer: ByteArray  // 버퍼 선언
    private var readBufferPosition: Int = 0  // 버퍼 위치 초기화
    private var workerThread: Thread? = null
    private lateinit var viewModel: BluetoothViewModel // ViewModel 선언
    private lateinit var bluetoothViewModel: BluetoothViewModel


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment3_layout, container, false)

        textViewReceive = view.findViewById(R.id.textViewReceive)
        textViewComment = view.findViewById(R.id.textViewComment)

    // GIF용 ImageView 초기화 및 Glide로 로드
        val imageViewGif: ImageView = view.findViewById(R.id.imageViewGif)
        Glide.with(this).asGif().load(R.drawable.your_gif_file).into(imageViewGif)

        // ViewModel 초기화
        bluetoothViewModel = ViewModelProvider(requireActivity()).get(BluetoothViewModel::class.java)

        // LiveData를 관찰하여 값이 변경되면 UI 업데이트
        bluetoothViewModel.heartRate.observe(viewLifecycleOwner,
            androidx.lifecycle.Observer { heartRate ->
                textViewReceive.text = "BPM : $heartRate"
            })

        return view
    }

}

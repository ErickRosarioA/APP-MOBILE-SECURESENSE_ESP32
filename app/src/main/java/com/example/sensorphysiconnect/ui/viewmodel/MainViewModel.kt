package com.example.sensorphysiconnect.ui.viewmodel

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorphysiconnect.data.model.ConfigWifi
import com.example.sensorphysiconnect.data.model.SensorData
import com.example.sensorphysiconnect.data.repository.FirebaseRepository
import com.example.sensorphysiconnect.ui.screens.ScanSensorActivity.Companion.REQUEST_ENABLE_BT
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainViewModel : ViewModel() {
    private val repository = FirebaseRepository()

    private val _sensorData: MutableLiveData<SensorData> = MutableLiveData<SensorData>()
    val sensorData: LiveData<SensorData> = _sensorData

     private val _bluetoothDevices = MutableLiveData<List<BluetoothDevice>>()
    val bluetoothDevices: MutableLiveData<List<BluetoothDevice>> = _bluetoothDevices
    private val firestore = FirebaseFirestore.getInstance()

    // Crear un StateFlow para almacenar los datos de Firestore
    private val _data = MutableStateFlow<List<DocumentSnapshot>>(emptyList())
    val data: StateFlow<List<DocumentSnapshot>> = _data

    init {
        listenToSensorData()
        listenToDataFireStore()
    }

    private fun listenToDataFireStore(){
                    viewModelScope.launch {
                firestore.collection("SensorUpdates")
                    .get()
                    .addOnSuccessListener { result ->
                        _data.value = result.documents
                    }
            }

    }

    private fun listenToSensorData() {
        repository.getSensorData { data ->
            _sensorData.postValue(data)
        }
    }


    fun configSensorRedWifi(configWifi: ConfigWifi){
        //Codigo para enviar al sensor de la red wifi a usar
    }

    fun updateSensorData(sensorData: SensorData) {
        viewModelScope.launch {
            repository.updateSensorData(sensorData)
        }
    }



    suspend fun getPairedDevices(bluetoothAdapter: BluetoothAdapter?, activity: Activity): List<BluetoothDevice>? {
        return withContext(Dispatchers.IO) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {

                bluetoothAdapter?.bondedDevices?.toList()
            } else {
                withContext(Dispatchers.Main) {
                    // Si no tienes el permiso, solicítalo al usuario
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN
                        ),
                        REQUEST_ENABLE_BT
                    )
                }
                null
            }
        }
    }

    fun refreshBluetoothDevices(bluetoothAdapter: BluetoothAdapter?, activity: Activity) {
        viewModelScope.launch {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN)
                == PackageManager.PERMISSION_GRANTED
            ) {

                bluetoothAdapter?.let { adapter ->
                    if (adapter.isEnabled) {
                        _bluetoothDevices.value = adapter.bondedDevices.toList()
                    }
                }
            } else {
                // Si no tienes el permiso, solicítalo al usuario
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN
                    ),
                    REQUEST_ENABLE_BT
                )
            }
        }



    }


    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
    }
}


package com.example.sensorphysiconnect.ui.viewmodel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sensorphysiconnect.ui.viewmodel.MainViewModel.Companion.PERMISSION_REQUEST_CODE

class WifiViewModel(application: Application) : AndroidViewModel(application) {

    private val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _wifiNetworks = MutableLiveData<List<ScanResult>>()
    val wifiNetworks: LiveData<List<ScanResult>> = _wifiNetworks

    fun getAvailableWifiNetworks(activity: Activity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar permisos si no están concedidos
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Si los permisos están concedidos, obtener las redes WiFi disponibles
            fetchWifiNetworks(activity)
        }
    }

    private fun fetchWifiNetworks(activity: Activity) {
        val wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    if (ContextCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val scanResults = wifiManager.scanResults
                        _wifiNetworks.postValue(scanResults)
                    } else {
                        Log.e(TAG, "No se tienen los permisos necesarios para acceder a la ubicación.")
                    }
                    activity.unregisterReceiver(this)
                }
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        }

        activity.registerReceiver(wifiScanReceiver, intentFilter)
        wifiManager.startScan()
    }

}

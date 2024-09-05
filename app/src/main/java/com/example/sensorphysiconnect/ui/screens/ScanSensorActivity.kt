package com.example.sensorphysiconnect.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sensorphysiconnect.R
import com.example.sensorphysiconnect.data.model.ConfigWifi
import com.example.sensorphysiconnect.ui.viewmodel.MainViewModel
import com.example.sensorphysiconnect.ui.viewmodel.MainViewModel.Companion.PERMISSION_REQUEST_CODE
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

class ScanSensorActivity : ComponentActivity() {

    private val bluetoothViewModel: MainViewModel by viewModels()
    private lateinit var socket: BluetoothSocket


    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        BluetoothAdapter.getDefaultAdapter()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TopAppBarScreen()
        }

        bluetoothViewModel.refreshBluetoothDevices(bluetoothAdapter, this)


    }


    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBarScreen() {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    title = {
                        Text("Escanear Sensores ")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            finish()
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                tint = Color.White,
                                contentDescription = "Close"
                            )
                        }
                    },

                    actions = {
                        IconButton(onClick = {

                            bluetoothViewModel.refreshBluetoothDevices(
                                bluetoothAdapter, this@ScanSensorActivity
                            )
                            if (bluetoothAdapter?.isEnabled == true) {
                                Toast.makeText(
                                    this@ScanSensorActivity,
                                    "Lista actualizada",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                tint = Color.White,
                                contentDescription = "refrescar"
                            )
                        }

                        IconButton(onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    this@ScanSensorActivity, Manifest.permission.BLUETOOTH_ADMIN
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                // Si no tienes el permiso, solicítalo al usuario
                                ActivityCompat.requestPermissions(
                                    this@ScanSensorActivity,
                                    arrayOf(Manifest.permission.BLUETOOTH_ADMIN),
                                    REQUEST_ENABLE_BT
                                )
                            } else {
                                // Si ya tienes el permiso, puedes proceder a activar el Bluetooth

                                bluetoothAdapter?.takeIf { !it.isEnabled }?.apply {
                                    val enableBtIntent =
                                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                                }

                                if (bluetoothAdapter?.isEnabled == true) {
                                    Toast.makeText(
                                        this@ScanSensorActivity,
                                        "Bluetooth Activado",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }


                            }
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.bluetooth),
                                tint = Color.White,
                                contentDescription = "Bluetooth"
                            )
                        }
                    },
                )
            }) {
            Column {
                Spacer(modifier = Modifier.height(66.dp))
                BluetoothDeviceList()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun BluetoothDeviceList() {
        val devices = bluetoothViewModel.bluetoothDevices.observeAsState(emptyList())

        val pairedDevices: MutableState<List<BluetoothDevice>?> = remember { mutableStateOf(null) }
        val coroutineScope = rememberCoroutineScope()


        LaunchedEffect(Unit) {
            coroutineScope.launch {
                pairedDevices.value = bluetoothViewModel.getPairedDevices(
                    BluetoothAdapter.getDefaultAdapter(), this@ScanSensorActivity
                )
            }
        }

        Text(
            text = "Favor ir a Bluetooth en su móvil para agregar y emparejar el sensor deseado.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
        )
        Text(
            text = "Lista de Dispositivos Emparejados:",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )
        LazyColumn {
            if (devices.value.isEmpty()) {
                item {
                    Text(
                        text = "Por favor, Encender el Bluetooth",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(devices.value) { device ->
                    val isPaired = pairedDevices.value?.contains(device) ?: false
                    BluetoothDeviceListItem(device, isPaired)
                }
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.S)
    @Composable
    fun BluetoothDeviceListItem(device: BluetoothDevice, isPaired: Boolean) {

        val showDialog = remember { mutableStateOf(false) }

        BluetoothDialog(showDialog = showDialog,
            onDismiss = { showDialog.value = false })

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { pairBluetoothDevice(device, showDialog) },
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                if (ContextCompat.checkSelfPermission(
                        baseContext,
                        Manifest.permission.BLUETOOTH
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Text(
                        text = device.name ?: "Dispositivo sin nombre",

                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    )
                } else {
                    Text(text = "Permiso Bluetooth no concedido")
                }

                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = "Estado de emparejamiento",
                    tint = Color.Black
                )
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun pairBluetoothDevice(device: BluetoothDevice, showDialog: MutableState<Boolean>) {

        if (device.name == "MagnetSystem") {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Si no se tienen los permisos, solicitarlos
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    PERMISSION_REQUEST_CODE
                )
            } else {
                try {
                    val uuid: UUID =
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // UUID para SPP (Serial Port Profile)
                    socket = device.createRfcommSocketToServiceRecord(uuid)
                    socket.connect()
                    Toast.makeText(this, "Conexión Exitosa", Toast.LENGTH_SHORT).show()
                    showDialog.value = true

                } catch (e: IOException) {
                    Toast.makeText(this, "Error en conectarse", Toast.LENGTH_SHORT).show()
                }

            }
        } else {
            Toast.makeText(this, "Este dispositivo no es un sensor", Toast.LENGTH_SHORT).show()
        }


    }


    @Composable
    fun BluetoothDialog(
        showDialog: MutableState<Boolean>,
        onDismiss: () -> Unit,
    ) {
        val tempWifiRed = remember {
            mutableStateOf("")
        }
        val tempPasswordWifi = remember {
            mutableStateOf("")
        }
        if (showDialog.value) {


            AlertDialog(onDismissRequest = { onDismiss() }, // Cerrar el diálogo al hacer clic fuera de él
                title = {
                    Text(
                        text = "CONFIGURACIÓN DE RED WIFI",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .padding(5.dp)
                            .fillMaxWidth()
                    )
                },
                properties = DialogProperties(dismissOnClickOutside = false),
                text = {
                    Column {
                        OutlinedTextField(
                            value = tempWifiRed.value,
                            onValueChange = { tempWifiRed.value = it },
                            label = { Text("Escribir Red WiFi") },
                            modifier = Modifier.padding(bottom = 5.dp)
                        )

                        OutlinedTextField(
                            onValueChange = { tempPasswordWifi.value = it },
                            value = tempPasswordWifi.value,
                            label = { Text("Contraseña de la red WiFi") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        // Verificar si los campos no están vacíos
                        if (tempWifiRed.value.isNotBlank() && tempPasswordWifi.value.isNotBlank()) {
                            val configWifi = ConfigWifi(tempWifiRed.value, tempPasswordWifi.value)
                            if (socket.isConnected) {
                                try {
                                    socket.outputStream.write(configWifi.selectRedWifi.toByteArray())
                                    socket.outputStream.write(configWifi.passwordWifi.toByteArray())
                                } catch (e: IOException) {
                                    Toast.makeText(
                                        this,
                                        "Error Enviando datos de Red",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            socket.close()
                            onDismiss()
                        } else {
                            Toast.makeText(
                                this,
                                "Por favor, rellena todos los campos",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }) {
                        Text("Enviar")
                    }
                }, dismissButton = {
                    Button(onClick = { onDismiss() } // Cerrar el diálogo al hacer clic en Cancelar
                    ) {
                        Text("Cancelar")
                    }
                })
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bluetoothViewModel.refreshBluetoothDevices(
                    bluetoothAdapter, this@ScanSensorActivity
                )
            } else {
                // El usuario rechazó la activación del Bluetooth
            }
        }
    }

    companion object {
        const val REQUEST_ENABLE_BT = 1
    }
}
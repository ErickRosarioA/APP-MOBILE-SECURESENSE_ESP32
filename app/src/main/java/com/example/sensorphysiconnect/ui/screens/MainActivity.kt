package com.example.sensorphysiconnect.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast


import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels

import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width


import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Create

import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton


import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState

import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTimePickerState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext



import androidx.compose.ui.text.font.FontWeight


import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sensorphysiconnect.R
import com.example.sensorphysiconnect.data.model.SensorData
import com.example.sensorphysiconnect.service.RealTimeDataBaseService

import com.example.sensorphysiconnect.ui.viewmodel.MainViewModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    private val REQUEST_CODE_FOREGROUND_SERVICE = 1


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        setContent {
            TopAppBarScreen()
            BottomTextView()
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                MainViewModel.PERMISSION_REQUEST_CODE
            )
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.FOREGROUND_SERVICE), REQUEST_CODE_FOREGROUND_SERVICE)
        } else {
            startMyService()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_FOREGROUND_SERVICE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startMyService()
                } else {
                    // El permiso fue denegado, debes manejar esta situación
                }
                return
            }
            else -> {
                // Ignora todos los otros casos de solicitud de permisos
            }
        }
    }

    private fun startMyService() {
        val serviceIntent = Intent(this, RealTimeDataBaseService::class.java)
        startForegroundService(serviceIntent)
    }
    @SuppressLint("StringFormatInvalid")
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                val mainViewModel = MainViewModel()
                val sensorData = mainViewModel.sensorData.value
                // Get new FCM registration token
                val token = task.result

                if(task.result.isNullOrEmpty()){
                    try {
                        val sensorDataR = SensorData(
                            sensorData?.name,
                            sensorData?.networkConnection,
                            sensorData?.securityBroken,
                            sensorData?.startHour.toString().toInt(),
                            sensorData?.endHour.toString().toInt(),
                            task.result.toString()
                        )
                        mainViewModel.updateSensorData(sensorDataR)
                        Toast.makeText(
                            this,
                            "Token registrado",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "Error registrando token",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }else{
                    Toast.makeText(
                        this,
                        "No hay token disponible",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Log and toast
                val msg = getString(R.string.msg_token_fmt, token.toString())
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }


    }


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBarScreen() {
        val context = LocalContext.current
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    title = {
                        Text("Seguridad de Hogar ")
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                imageVector = Icons.Outlined.Home,
                                tint = Color.White,
                                contentDescription = "Logo Home"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {

                            context.startActivity(Intent(context, HistoryStoreActivity::class.java))

                        }) {
                            Icon(
                                imageVector = Icons.Outlined.List,
                                tint = Color.White,
                                contentDescription = "Historial"
                            )
                        }
                        IconButton(onClick = {

                            context.startActivity(Intent(context, ScanSensorActivity::class.java))

                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                tint = Color.White,
                                contentDescription = "Conexion Bluetho"
                            )
                        }
                    },
                )
            }
        ) {
            Column {
                Spacer(modifier = Modifier.height(56.dp)) // Altura de la TopAppBar
                MainActivityComposable(mainViewModel = MainViewModel())
            }
        }
    }


    @Composable
    fun MainActivityComposable(mainViewModel: MainViewModel) {

        val (dialogOpen, showDialog) = remember { mutableStateOf(false) }
        val onDismiss: () -> Unit = { showDialog(false) }

        val sensorData by mainViewModel.sensorData.observeAsState()


        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información de dispositivos",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp, top = 20.dp)
            )
            LazyColumn {
                item {
                    DeviceInfoCard(
                        deviceName = "Dispositivo 1",
                        sensorData = sensorData,
                        onClick = { showDialog(true) }
                    )
                }
//            item {
//                DeviceInfoCard(
//                    deviceName = "Dispositivo 2",
//                    sensorData = sensorData,
//                    onClick = { showDialog(true) }
//                )
//            }
            }
            if (dialogOpen) {
                DeviceInfoDialog(device = sensorData, onDismiss = onDismiss)
            }
        }

    }


    enum class RowType { START_HOUR, END_HOUR }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DeviceInfoDialog(device: SensorData?, onDismiss: () -> Unit) {
        val mainViewModel = MainViewModel()

        val lastClickedRow = remember { mutableStateOf<RowType?>(null) }

        val startHour = remember { mutableStateOf(device?.startHour ?: "") }
        val endHour = remember { mutableStateOf(device?.endHour ?: "") }

        val initialHour = remember { mutableStateOf(0) }

        val timePickerState = rememberTimePickerState(
            initialHour.value, 0, false
        )
        val showTimePicker = remember { mutableStateOf(false) }

        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {

                Text(
                    text = "Configurar Horario de Puerta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()

                )
            },
            properties = DialogProperties(dismissOnClickOutside = false),

            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clickable {
                                initialHour.value = startHour.value
                                    .toString()
                                    .toInt()
                                lastClickedRow.value = RowType.START_HOUR

                                showTimePicker.value = true
                            }
                    ) {
                        Text(text = "Hora de inicio:", modifier = Modifier.weight(1f))
                        Text(
                            text = startHour.value.toString(),
                        )
                        Icon(
                            imageVector = Icons.Outlined.Create,
                            tint = MaterialTheme.colorScheme.primary,// Icono de reloj
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(15.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clickable {
                                initialHour.value = endHour.value
                                    .toString()
                                    .toInt()
                                lastClickedRow.value = RowType.END_HOUR
                                showTimePicker.value = true
                            }
                    ) {
                        Text(text = "Hora de fin:", modifier = Modifier.weight(1f))
                        Text(
                            text = endHour.value.toString(),
                        )
                        Icon(
                            imageVector = Icons.Outlined.Create,
                            tint = MaterialTheme.colorScheme.primary,// Icono de reloj
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(15.dp)
                        )
                    }

                }
            },
            confirmButton = {
                Button(onClick = {

                    try {
                        val sensorData = SensorData(
                            device?.name,
                            device?.networkConnection,
                            device?.securityBroken,
                            startHour.value.toString().toInt(),
                            endHour.value.toString().toInt(),
                            device?.tokenAppMobile.toString()
                        )
                        mainViewModel.updateSensorData(sensorData)
                        Toast.makeText(
                            context,
                            "Enviados correctamente",
                            Toast.LENGTH_SHORT
                        ).show()
                        onDismiss()
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Error al enviar los datos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }) {
                    Text(text = "Enviar")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text(text = "Cancelar")
                }
            }
        )

        if (showTimePicker.value) {
            TimePickerDialog(
                onDismissRequest = { /*TODO*/ },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (lastClickedRow.value == RowType.START_HOUR) {
                                startHour.value = timePickerState.hour

                            } else {
                                endHour.value = timePickerState.hour

                            }

                            showTimePicker.value = false
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showTimePicker.value = false
                        }
                    ) { Text("Cancel") }
                }
            )
            {
                TimePicker(
                    state = timePickerState,
                )
            }
        }
    }

    @Composable
    fun TimePickerDialog(
        title: String = "Seleccionar hora",
        onDismissRequest: () -> Unit,
        confirmButton: @Composable (() -> Unit),
        dismissButton: @Composable (() -> Unit)? = null,
        containerColor: Color = MaterialTheme.colorScheme.surface,
        content: @Composable () -> Unit,
    ) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            ),
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .width(IntrinsicSize.Min)
                    .height(IntrinsicSize.Min)
                    .background(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = containerColor
                    ),
                color = containerColor
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        text = title,
                        style = MaterialTheme.typography.labelMedium
                    )
                    content()
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }

    @Composable
    fun DeviceInfoCard(deviceName: String, sensorData: SensorData?, onClick: () -> Unit) {
        val mainViewModel = MainViewModel()
        val backgroundColor =
            if (sensorData?.networkConnection == true) Color.White else Color.LightGray
        //var lockEnabled by remember { mutableStateOf(false) }
        val context = LocalContext.current
        Card(
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = deviceName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Nombre:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = sensorData?.name.toString(),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Conexión a la red:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (sensorData?.networkConnection == true) "Si" else "No",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Estado:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,

                    ) {

                    Text(
                        text = if (sensorData?.securityBroken == false) "Conectado" else "Desconectado",
                        fontSize = 16.sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(12.dp) // Tamaño del círculo
                            .drawBehind {
                                drawCircle(
                                    color = if (sensorData?.securityBroken == false) Color.Green else Color.Red // Color del círculo
                                )
                            }
                    )
                }


                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                }
                ConnectionButton(onClick = onClick)
            }
        }
    }

    @Composable
    fun ConnectionButton(onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(5.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Build,
                    contentDescription = "Ajustar dispositivo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Configurar dispositivo",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }



    }

    @Composable
    fun BottomTextView() {
        Box(
            modifier = Modifier.fillMaxSize().fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)

                    .padding(16.dp),
                text = "Grupo #1 de Física 3, 2024-1",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }



}


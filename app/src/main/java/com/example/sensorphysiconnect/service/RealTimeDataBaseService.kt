package com.example.sensorphysiconnect.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*
import java.time.LocalTime
import java.time.ZoneId

class RealTimeDataBaseService : Service() {

    private lateinit var database: FirebaseDatabase
    private lateinit var sensorRef: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreate() {
        super.onCreate()
        database = FirebaseDatabase.getInstance()
        sensorRef = database.getReference("Sensor")
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val securityBroken = snapshot.child("security_broken").getValue(Boolean::class.java)
                val startHour = snapshot.child("startHour").getValue(Int::class.java)
                val endHour = snapshot.child("endHour").getValue(Int::class.java)

                // Obtener la hora local
                val currentLocalTime = LocalTime.now(ZoneId.systemDefault())


                val startHourLocalTime =
                    startHour?.let { LocalTime.of(it, 0) } // Suponiendo que solo quieres la hora y no los minutos
                val endHourLocalTime = LocalTime.of(endHour!!.toInt(), 0)


                if (startHourLocalTime != null) {
                    if (currentLocalTime in startHourLocalTime..endHourLocalTime && securityBroken == true) {
                        showNotification()
                        playSound()
                        println("La hora local está dentro del rango y securityBroken es false.")
                    }
                }

//                if (securityBroken == false) {
//                    playSound()
//                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error
            }
        }
        sensorRef.addValueEventListener(valueEventListener)
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("!Servicio Seguridad Activo!")
            .setSmallIcon(com.example.sensorphysiconnect.R.drawable.sensor)
            .build()
        startForeground(1, notification)
        return START_NOT_STICKY
    }


    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            "CHANNEL_ID",
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager: NotificationManager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorRef.removeEventListener(valueEventListener)
    }

//    private fun playSound() {
//        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val r: Ringtone = RingtoneManager.getRingtone(applicationContext, notification)
//        r.play()
//    }

    private fun playSound() {
        try {
            // Obtener el servicio de Audio Manager
            val audioManager = getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager

            // Guardar el estado actual del volumen de notificaciones
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

            // Ajustar el volumen del sonido de notificación al máximo
            audioManager.setStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
                AudioManager.FLAG_PLAY_SOUND
            )

            // Reproducir el sonido de notificación
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r: Ringtone = RingtoneManager.getRingtone(applicationContext, notification)

            val numRepeats = 4 // Número de veces que quieres que se repita el sonido
            repeat(numRepeats) {
                r.play()
                Thread.sleep(1000) // Esperar 1 segundo entre cada reproducción
            }

            // Restaurar el volumen de notificación al valor anterior
            audioManager.setStreamVolume(
                AudioManager.STREAM_NOTIFICATION,
                currentVolume,
                AudioManager.FLAG_PLAY_SOUND
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun showNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "ALERT_NOTIFICATION_CHANNEL"

        val notificationChannel = NotificationChannel(notificationChannelId, "Alert Notifications", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(com.example.sensorphysiconnect.R.drawable.sensor)
            .setContentTitle("Alerta SensorPhysiConnect")
            .setContentText("Atención: Movimiento en la puerta principal")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notification = notificationBuilder.build()
        notificationManager.notify(1, notification)
    }
}

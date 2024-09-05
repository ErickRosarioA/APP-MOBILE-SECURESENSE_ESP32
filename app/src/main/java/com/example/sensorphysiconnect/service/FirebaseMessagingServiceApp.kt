package com.example.sensorphysiconnect.service

import android.annotation.SuppressLint
import android.media.AudioManager

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.grpc.Context

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseMessagingServiceApp : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            playSound()
        }

        Log.i("DATA_MESSAGE", remoteMessage.data.toString())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i("FCM_TOKEN", token)
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
            val numRepeats = 3 // Número de veces que quieres que se repita el sonido
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

}
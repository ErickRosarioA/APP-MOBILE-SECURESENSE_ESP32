package com.example.sensorphysiconnect.data.repository

import androidx.lifecycle.MutableLiveData
import com.example.sensorphysiconnect.data.model.SensorData
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository (
    private val rootRef: DatabaseReference = FirebaseDatabase.getInstance().reference,
    private val sensorRef: DatabaseReference = rootRef,
    private val fireStore : FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getSensorData(callback: (SensorData) -> Unit) {
        sensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorDataSnapshot = snapshot.child("Sensor")
                val name = sensorDataSnapshot.child("name").getValue(String::class.java)
                val networkConnection = sensorDataSnapshot.child("networkConnection").getValue(Boolean::class.java)
                val securityBroken = sensorDataSnapshot.child("security_broken").getValue(Boolean::class.java)
                val startHour = sensorDataSnapshot.child("startHour").getValue(Int::class.java)
                val endHour = sensorDataSnapshot.child("endHour").getValue(Int::class.java)
                val tokenAppMobile = sensorDataSnapshot.child("tokenAppMobile").getValue(String::class.java)
                val sensorData = SensorData(name,networkConnection,securityBroken,startHour,endHour,tokenAppMobile)


                callback(sensorData)
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores
            }
        })
    }


//    fun getSensorDataLiveData(): MutableLiveData<SensorData> {
//        val mutableLiveData = MutableLiveData<SensorData>()
//        sensorRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val sensorDataSnapshot = snapshot.child("Sensor")
//                val securityBroken = sensorDataSnapshot.child("security_broken").getValue(Boolean::class.java)
//                val sensorData = SensorData(securityBroken)
//                mutableLiveData.postValue(sensorData)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Manejar errores
//            }
//        })
//        return mutableLiveData
//    }

    fun updateSensorData(sensorData: SensorData) {
        sensorRef.child("Sensor").setValue(sensorData)
            .addOnSuccessListener {
                // Manejar éxito de la actualización
            }
            .addOnFailureListener { error ->

            }
    }
}
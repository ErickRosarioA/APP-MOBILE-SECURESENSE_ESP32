package com.example.sensorphysiconnect.data.model

import com.google.firebase.database.PropertyName


data class SensorData(
    @get:PropertyName("name")
    val name: String? = null,

    @get:PropertyName("networkConnection")
    val networkConnection: Boolean? = null,

    @get:PropertyName("security_broken")
    val securityBroken: Boolean? = null,

    @get:PropertyName("startHour")
    val startHour: Int? = 0,

    @get:PropertyName("endHour")
    val endHour: Int? = 0,

    @get:PropertyName("tokenAppMobile")
    val  tokenAppMobile: String? = null
)
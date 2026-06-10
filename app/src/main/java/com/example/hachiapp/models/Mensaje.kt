package com.example.hachiapp.models

import com.google.firebase.Timestamp

data class Mensaje(

    val remitenteId: String = "",

    val destinatarioId: String = "",

    val reporteId: String = "",

    val nombreMascota: String = "",

    val mensaje: String = "",

    val imagenUrl: String = "",

    val fecha: Timestamp? = null
)
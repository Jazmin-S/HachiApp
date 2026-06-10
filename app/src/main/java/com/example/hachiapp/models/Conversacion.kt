package com.example.hachiapp.models

import com.google.firebase.Timestamp

data class Conversacion(
    val reporteId: String = "",
    val otroUsuarioId: String = "",
    val nombreMascota: String = "",
    val ultimoMensaje: String = "",
    val fecha: Long = 0,
    val ultimoRemitenteId: String = ""
)
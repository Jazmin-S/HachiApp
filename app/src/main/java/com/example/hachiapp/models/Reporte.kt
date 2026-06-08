package com.example.hachiapp.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class Reporte(
    @get:Exclude val id: String = "",
    val usuarioId        : String    = "",
    val nombreMascota    : String    = "",
    val tipoMascota      : String    = "",
    val razaMascota      : String    = "",
    val colorMascota     : String    = "",
    val tamano           : String    = "",
    val edadMascota      : String    = "",
    val estadoMascota    : String    = "",
    val descripcion      : String    = "",
    val notaAdicional    : String    = "",
    val recompensa       : String    = "",
    val fechaExtravio    : String    = "",
    val direccion        : String    = "",
    val latitud          : Double    = 0.0,
    val longitud         : Double    = 0.0,
    val imagenesUrl      : List<String> = emptyList(),
    val fechaPublicacion : Timestamp? = null
) : Serializable // Clase serializable para pasar el objeto entre actividades
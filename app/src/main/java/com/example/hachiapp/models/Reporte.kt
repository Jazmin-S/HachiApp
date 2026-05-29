package com.example.hachiapp.models

data class Reporte(

    val nombreMascota: String = "",
    val razaMascota: String = "",
    val colorMascota: String = "",
    val tamano: String = "",
    val fechaExtravio: String = "",
    val descripcion: String = "",

    val imagenes: List<String> = emptyList(),

    val usuarioId: String = "",

    val tipoReporte: String = "",

    val recompensa: String = "",

    val ultimaVezVistoTexto: String = "",

    val fechaCreacion: Any? = null
)
package com.example.hachiapp.Model

import com.google.firebase.Timestamp

data class Alerta(

    var titulo: String = "",
    var descripcion: String = "",
    var tipo: String = "",
    var reporteId: String = "",
    var avistamientoId: String = "",
    var fecha: Timestamp? = null,
    var leida: Boolean = false

)
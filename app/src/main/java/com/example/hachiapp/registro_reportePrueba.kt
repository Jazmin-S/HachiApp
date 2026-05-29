package com.example.hachiapp

import android.net.Uri
import com.example.hachiapp.BD.ReporteRepository
import com.example.hachiapp.models.Reporte

class RegistroReportePrueba {

    private val repository = ReporteRepository()
    private var imagenUri: Uri? = null

    fun crearReportesPrueba() {

        val reporte1 = Reporte(

            nombreMascota = "Max",
            razaMascota = "Labrador",
            colorMascota = "Cafe",
            tamano = "Grande",
            fechaExtravio = "2026-05-28",
            descripcion = "Tiene collar rojo",

            imagenes = listOf(),

            usuarioId = "usuario123",

            tipoReporte = "perdido",

            recompensa = "500",

            ultimaVezVistoTexto = "Parque Juarez",

            fechaCreacion = System.currentTimeMillis()
        )

        val reporte2 = Reporte(

            nombreMascota = "Luna",
            razaMascota = "Husky",
            colorMascota = "Blanco",

            tamano = "Mediano",

            fechaExtravio = "2026-05-20",

            descripcion = "Muy amigable",

            imagenes = listOf(),

            usuarioId = "usuario456",

            tipoReporte = "visto",

            recompensa = "",

            ultimaVezVistoTexto = "Centro de Xalapa",

            fechaCreacion = System.currentTimeMillis()
        )

        repository.guardarReporte(reporte1)
        repository.guardarReporte(reporte2)
    }
}
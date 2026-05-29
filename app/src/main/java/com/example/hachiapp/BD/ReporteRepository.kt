package com.example.hachiapp.BD

import com.example.hachiapp.models.Reporte
import com.google.firebase.firestore.FirebaseFirestore

class ReporteRepository {

    private val db = FirebaseFirestore.getInstance()

    fun guardarReporte(reporte: Reporte) {

        db.collection("reportes")
            .add(reporte)
    }
}
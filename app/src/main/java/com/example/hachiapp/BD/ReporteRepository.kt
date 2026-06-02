package com.example.hachiapp.BD

import com.example.hachiapp.models.Reporte
import com.google.firebase.firestore.FirebaseFirestore

/*
 * Clase encargada de realizar operaciones
 * relacionadas con la colección "reportes"
 * dentro de Firebase Firestore.
 */
class ReporteRepository {

    private val db = FirebaseFirestore.getInstance()

    /* Guarda un reporte en la colección "reportes".
    * Se crearon 3 parametros
    * Parámetros:
    1. reporte: Objeto Reporte que contiene toda la información capturada desde el formulario.
    2. onSuccess: Función que se ejecuta cuando el reporte se guarda correctamente.
    3. onError: Función que se ejecuta cuando ocurre algún error al guardar.
    */

    fun guardarReporte(
        reporte: Reporte,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        /*Se accede a la colección "reportes". Si la colección no existe,
        Firestore la crea automáticamente.
        */
        db.collection("reportes")
            /*
            * Se ejecuta cuando el registro
            * se guarda correctamente.
            */
            .add(reporte)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }
}
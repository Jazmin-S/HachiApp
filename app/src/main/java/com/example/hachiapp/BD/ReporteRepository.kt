package com.example.hachiapp.BD

import com.example.hachiapp.models.Reporte
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/*
 * Clase encargada de realizar operaciones con "reportes" dentro de Firebase Firestore.
 */
class ReporteRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auntenticacion = FirebaseAuth.getInstance()

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

        db.collection("reportes")
            .add(reporte)
            .addOnSuccessListener { documentoReporte ->

                val alerta = hashMapOf(
                    "titulo" to "Nuevo reporte",
                    "descripcion" to "Se registró una mascota perdida",
                    "tipo" to "reporte",
                    "reporteId" to documentoReporte.id,
                    "fecha" to com.google.firebase.Timestamp.now(),
                    "leida" to false
                )

                db.collection("alertas")
                    .add(alerta)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener {
                        onError(it)
                    }
            }
    }
    /* Obtiene todos los reportes del usuario autenticado
     * desde la colección "reportes".
     * Se crearon 2 parámetros:
     * 1. onSuccess: Función que se ejecuta cuando los reportes se obtienen correctamente,
     *    recibe una lista de objetos Reporte.
     * 2. onError: Función que se ejecuta cuando ocurre algún error al obtener los reportes.
     */
    fun obtenerReportesUsuario(
        onSuccess: (List<Reporte>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        /*Se obtiene el UID del usuario actualmente autenticado.
         * Si no hay usuario autenticado, lanza un error y detiene la ejecución.
         */
        val uid = auntenticacion.currentUser?.uid
        if (uid == null) {
            onError(Exception("Usuario no autenticado"))
            return
        }

        /*Se accede a la colección "reportes" y se filtran
         * solo los reportes que pertenecen al usuario actual
         * usando su UID en el campo "usuarioId".
         */
        db.collection("reportes")
            .whereEqualTo("usuarioId", uid)
            .get()
            /*
             * Se ejecuta cuando los reportes
             * se obtienen correctamente.
             */
            .addOnSuccessListener { documentos ->
                /*Se convierte cada documento de Firestore
                 * a un objeto Reporte y se agrega a la lista.
                 */
                val reportes = documentos.mapNotNull { documento ->
                    documento.toObject(Reporte::class.java)
                        .copy(id = documento.id)
                }
                onSuccess(reportes)
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    /* Guarda un nuevo avistamiento en la colección "avistamientos".
 * Se crearon 3 parámetros:
 * 1. avistamiento: Mapa con los datos del avistamiento capturados desde el formulario.
 * 2. onSuccess: Función que se ejecuta cuando el avistamiento se guarda correctamente.
 * 3. onError: Función que se ejecuta cuando ocurre algún error al guardar.
 */
    fun guardarAvistamiento(
        avistamiento: HashMap<String, Any>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("avistamientos")
            .add(avistamiento)
            .addOnSuccessListener { documentoAvistamiento ->  // ✅ AQUÍ ESTÁ LA CLAVE

                val alerta = hashMapOf(
                    "titulo" to "Nuevo avistamiento",
                    "descripcion" to "Se encontró una mascota cerca de tu ubicación",
                    "tipo" to "avistamiento",
                    "reporteId" to avistamiento["reporteId"],
                    "avistamientoId" to documentoAvistamiento.id,
                    "fecha" to com.google.firebase.Timestamp.now(),
                    "leida" to false
                )

                db.collection("alertas")
                    .add(alerta)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener {
                        onError(it)
                    }

            }
            .addOnFailureListener {
                onError(it)
            }
    }
}
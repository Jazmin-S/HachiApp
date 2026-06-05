package com.example.hachiapp.BD

import com.example.hachiapp.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/*
 * Clase encargada de realizar operaciones
 * relacionadas con la colección "usuarios"
 * dentro de Firebase Firestore.
 */
class UsuarioRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /*
     * Guarda o actualiza el perfil del usuario en la colección "usuarios".
     * Se crearon 3 parámetros:
     * 1. usuario: Objeto Usuario que contiene toda la información capturada desde el formulario.
     * 2. onSuccess: Función que se ejecuta cuando el perfil se guarda correctamente.
     * 3. onError: Función que se ejecuta cuando ocurre algún error al guardar.
     */
    fun guardarPerfil(
        usuario: Usuario,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        /* Obtiene el UID del usuario actualmente autenticado.
         * Si no hay usuario autenticado, lanza un error y detiene la ejecución.
         */
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError(Exception("Usuario no autenticado"))
            return
        }

        /* Se accede a la colección "usuarios" y se usa el UID como ID del documento.
         * Así cada usuario tiene su propio documento con su información.
         * Si el documento no existe, Firestore lo crea automáticamente.
         */
        db.collection("usuarios")
            .document(uid)
            .set(usuario)
            /*
             * Se ejecuta cuando el perfil
             * se guarda correctamente.
             */
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it)
            }
    }

    /*
     * Lee el perfil del usuario autenticado desde la colección "usuarios".
     * Se crearon 2 parámetros:
     * 1. onSuccess: Función que se ejecuta cuando el perfil se obtiene correctamente,
     *    recibe el objeto Usuario con los datos.
     * 2. onError: Función que se ejecuta cuando ocurre algún error al leer.
     */
    fun obtenerPerfil(
        onSuccess: (Usuario) -> Unit,
        onError: (Exception) -> Unit
    ) {
        /* Obtiene el UID del usuario actualmente autenticado.
         * Si no hay usuario autenticado, lanza un error y detiene la ejecución.
         */
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onError(Exception("Usuario no autenticado"))
            return
        }

        /* Se accede al documento del usuario usando su UID.
         * Firestore convierte automáticamente los datos del documento
         * al objeto Usuario usando toObject().
         */
        db.collection("usuarios")
            .document(uid)
            .get()
            /*
             * Se ejecuta cuando el perfil
             * se obtiene correctamente.
             */
            .addOnSuccessListener { documento ->
                val usuario = documento.toObject(Usuario::class.java)
                if (usuario != null) {
                    onSuccess(usuario)
                } else {
                    onError(Exception("Perfil no encontrado"))
                }
            }
            .addOnFailureListener {
                onError(it)
            }
    }
}
package com.example.hachiapp.BD

// Importa el contexto de Android para acceder a recursos de la app
import android.content.Context
// Importa el MediaManager de Cloudinary para gestionar subidas de archivos
import com.cloudinary.android.MediaManager

// Declara una instancia global
object CloudinaryManager {

    // Bandera para evitar inicializar Cloudinary más de una vez
    private var initialized = false

    // Función pública que inicializa Cloudinary con el contexto de la app
    fun init(context: Context) {
        // Si ya se inicializó, no hace nada y sale
        if (initialized) return

        // Crea un mapa clave-valor para la configuración
        val config = HashMap<String, String>()
        // Asigna el nombre de la nube de Cloudinary (cloud name)
        config["cloud_name"] = "ddsvqtm52"

        // Inicializa MediaManager con el contexto y la configuración
        MediaManager.init(context, config)

        // Marca como inicializado para futuras llamadas
        initialized = true
    }
}
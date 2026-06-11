package com.example.hachiapp.Activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.hachiapp.BD.CloudinaryManager
import com.example.hachiapp.BD.ReporteRepository
import com.example.hachiapp.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

class Activity_registro_avistamiento : AppCompatActivity() {

    // Código de retorno para obtener datos desde el mapa (ubicación seleccionada)
    private val MAPS_REQUEST_CODE = 600

    // Coordenadas y dirección seleccionadas en el mapa
    private var latitud = 0.0
    private var longitud = 0.0
    private var direccion = ""

    // Datos del reporte recibido desde otra Activity
    private var reporteId = ""
    private var nombreMascota = ""

    // Imagen seleccionada localmente antes de subirla
    private var imagenSeleccionada: Uri? = null

    // URL final después de subir imagen a Cloudinary
    private var urlImagenCloudinary: String = ""

    // Vistas principales de la UI
    private lateinit var btnPerfil: ImageButton
    private lateinit var imgAvistamiento: ImageView
    private lateinit var etDescripcion: EditText
    private lateinit var cardMapa: CardView
    private lateinit var tvDireccion: TextView
    private lateinit var btnPublicar: Button

    // Repositorio que maneja guardado en Firestore
    private val repository = ReporteRepository()

    // Selector de imagen usando Storage Access Framework (abre galería)
    private val seleccionarImagen =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                // Se guarda la imagen seleccionada para posterior subida
                imagenSeleccionada = uri

                // Previsualización inmediata en pantalla
                imgAvistamiento.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_avistamiento)

        // Inicialización de Cloudinary (configuración global para subida de imágenes)
        CloudinaryManager.init(this)

        // Ajuste de UI para no chocar con barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        // Datos recibidos desde pantalla anterior (detalle del reporte)
        reporteId = intent.getStringExtra("reporteId") ?: ""
        nombreMascota = intent.getStringExtra("nombreMascota") ?: "Mascota"

        setupListeners()
    }

    // =========================
    // INICIALIZACIÓN DE VISTAS
    // =========================
    private fun initViews() {
        btnPerfil = findViewById(R.id.BtnPerfil)
        imgAvistamiento = findViewById(R.id.imgAvistamiento)
        etDescripcion = findViewById(R.id.etDescripcionAvistamiento)
        cardMapa = findViewById(R.id.cardMapaAvistamiento)
        tvDireccion = findViewById(R.id.tvDireccionAvistamiento)
        btnPublicar = findViewById(R.id.btnEnviarAvistamiento)
    }

    // =========================
    // EVENTOS DE INTERACCIÓN
    // =========================
    private fun setupListeners() {

        // Selección de imagen desde galería
        imgAvistamiento.setOnClickListener {
            seleccionarImagen.launch(arrayOf("image/*"))
        }

        // Apertura del mapa para seleccionar ubicación
        cardMapa.setOnClickListener {
            val intent = Intent(this, SeleccionarUbicacionActivity::class.java)

            // Se envían coordenadas actuales por si el mapa las necesita como referencia
            intent.putExtra("latitud", latitud)
            intent.putExtra("longitud", longitud)

            startActivityForResult(intent, MAPS_REQUEST_CODE)
        }

        // Publicar avistamiento (flujo principal)
        btnPublicar.setOnClickListener {
            validarYPublicarAvistamiento()
        }
    }

    // Resultado del mapa con ubicación seleccionada
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAPS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            latitud = data.getDoubleExtra("latitud", 0.0)
            longitud = data.getDoubleExtra("longitud", 0.0)
            direccion = data.getStringExtra("direccion") ?: ""

            // Se muestra dirección si existe, si no solo estado genérico
            tvDireccion.text = if (direccion.isNotEmpty()) direccion else "Ubicación fijada"

            tvDireccion.setTextColor(Color.BLACK)
        }
    }

    // =========================
    // VALIDACIÓN PRINCIPAL
    // =========================
    private fun validarYPublicarAvistamiento() {

        val description = etDescripcion.text.toString().trim()

        // Usuario actual autenticado (necesario para asociar el avistamiento)
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Validación de descripción obligatoria
        if (description.isEmpty()) {
            etDescripcion.error = "Agrega una descripción"
            return
        }

        // Validación de ubicación obligatoria
        if (latitud == 0.0 || longitud == 0.0) {
            Toast.makeText(this, "Selecciona ubicación en el mapa", Toast.LENGTH_LONG).show()
            return
        }

        // Bloquea botón para evitar publicaciones duplicadas
        btnPublicar.isEnabled = false
        Toast.makeText(this, "Publicando...", Toast.LENGTH_SHORT).show()

        // Si hay imagen, primero se sube a Cloudinary antes de guardar en Firebase
        if (imagenSeleccionada != null) {
            subirImagenYGuardar(usuarioId, description)
        } else {
            // Si no hay imagen, se guarda directamente en Firestore
            guardarEnFirebase(usuarioId, nombreMascota, description, "")
        }
    }

    // =========================
    // SUBIDA A CLOUDINARY
    // =========================
    private fun subirImagenYGuardar(uid: String, description: String) {

        val publicId = "avistamiento_${System.currentTimeMillis()}"

        MediaManager.get()
            .upload(imagenSeleccionada!!)
            .unsigned("hachiapp")
            .option("folder", "hachi_avistamientos")
            .option("public_id", publicId)

            .callback(object : UploadCallback {

                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onReschedule(requestId: String, error: ErrorInfo) {}

                override fun onSuccess(
                    requestId: String,
                    resultData: MutableMap<Any?, Any?>
                ) {
                    // URL final de la imagen subida a Cloudinary
                    val url = resultData["secure_url"]?.toString() ?: ""
                    urlImagenCloudinary = url

                    // Una vez subida la imagen, se guarda el reporte en Firebase
                    guardarEnFirebase(uid, nombreMascota, description, url)
                }

                override fun onError(requestId: String, error: ErrorInfo) {

                    // Reactiva botón si falla la subida
                    runOnUiThread {
                        btnPublicar.isEnabled = true

                        Toast.makeText(
                            this@Activity_registro_avistamiento,
                            "Error al subir imagen: ${error.description}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
            .dispatch()
    }

    // =========================
    // GUARDADO EN FIREBASE
    // =========================
    private fun guardarEnFirebase(
        uid: String,
        tipo: String,
        desc: String,
        urlFoto: String
    ) {

        // Estructura del documento que se guardará en Firestore
        val avistamientoMap = hashMapOf<String, Any>(
            "reporteId" to reporteId,
            "usuarioId" to uid,
            "tipoMascota" to tipo,
            "descripcion" to desc,
            "latitud" to latitud,
            "longitud" to longitud,
            "direccion" to direccion,
            "imagenUrl" to urlFoto,
            "fechaAvistamiento" to Timestamp.now()
        )

        // Inserción en base de datos mediante repositorio
        repository.guardarAvistamiento(
            avistamientoMap,

            onSuccess = {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "¡Avistamiento publicado con éxito!",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish() // Cierra pantalla al completar
                }
            },

            onError = { e ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Error en Firestore: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    btnPublicar.isEnabled = true
                }
            }
        )
    }
}
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

    private val MAPS_REQUEST_CODE = 600

    private var latitud = 0.0
    private var longitud = 0.0
    private var direccion = ""

    private var reporteId = ""
    private var nombreMascota = ""

    private var imagenSeleccionada: Uri? = null
    private var urlImagenCloudinary: String = ""

    private lateinit var btnPerfil: ImageButton
    private lateinit var imgAvistamiento: ImageView
    private lateinit var etDescripcion: EditText
    private lateinit var cardMapa: CardView
    private lateinit var tvDireccion: TextView
    private lateinit var btnPublicar: Button

    private val repository = ReporteRepository()

    // Selector de imagen
    private val seleccionarImagen =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                imagenSeleccionada = uri
                imgAvistamiento.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_avistamiento)

        CloudinaryManager.init(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        reporteId = intent.getStringExtra("reporteId") ?: ""
        nombreMascota = intent.getStringExtra("nombreMascota") ?: "Mascota"

        setupListeners()
    }

    private fun initViews() {
        btnPerfil = findViewById(R.id.BtnPerfil)
        imgAvistamiento = findViewById(R.id.imgAvistamiento)
        etDescripcion = findViewById(R.id.etDescripcionAvistamiento)
        cardMapa = findViewById(R.id.cardMapaAvistamiento)
        tvDireccion = findViewById(R.id.tvDireccionAvistamiento)
        btnPublicar = findViewById(R.id.btnEnviarAvistamiento)
    }

    private fun setupListeners() {
        imgAvistamiento.setOnClickListener {
            seleccionarImagen.launch(arrayOf("image/*"))
        }

        cardMapa.setOnClickListener {
            val intent = Intent(this, SeleccionarUbicacionActivity::class.java)
            intent.putExtra("latitud", latitud)
            intent.putExtra("longitud", longitud)
            startActivityForResult(intent, MAPS_REQUEST_CODE)
        }

        btnPublicar.setOnClickListener {
            validarYPublicarAvistamiento()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAPS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            latitud = data.getDoubleExtra("latitud", 0.0)
            longitud = data.getDoubleExtra("longitud", 0.0)
            direccion = data.getStringExtra("direccion") ?: ""

            tvDireccion.text = if (direccion.isNotEmpty())
                direccion
            else
                "Ubicación fijada"

            tvDireccion.setTextColor(Color.BLACK)
        }
    }

    // ==============================
    // VALIDACIÓN PRINCIPAL
    // ==============================
    private fun validarYPublicarAvistamiento() {

        val description = etDescripcion.text.toString().trim()
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        if (description.isEmpty()) {
            etDescripcion.error = "Agrega una descripción"
            return
        }

        if (latitud == 0.0 || longitud == 0.0) {
            Toast.makeText(this, "Selecciona ubicación en el mapa", Toast.LENGTH_LONG).show()
            return
        }

        btnPublicar.isEnabled = false
        Toast.makeText(this, "Publicando...", Toast.LENGTH_SHORT).show()

        if (imagenSeleccionada != null) {
            subirImagenYGuardar(usuarioId, description)
        } else {
            guardarEnFirebase(usuarioId, nombreMascota, description, "")
        }
    }

    // ==============================
    // SUBIR A CLOUDINARY
    // ==============================
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
                    val url = resultData["secure_url"]?.toString() ?: ""
                    urlImagenCloudinary = url

                    guardarEnFirebase(uid, nombreMascota, description, url)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
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

    // ==============================
    // FIREBASE
    // ==============================
    private fun guardarEnFirebase(
        uid: String,
        tipo: String,
        desc: String,
        urlFoto: String
    ) {
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

        repository.guardarAvistamiento(
            avistamientoMap,
            onSuccess = {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "¡Avistamiento publicado con éxito!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
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
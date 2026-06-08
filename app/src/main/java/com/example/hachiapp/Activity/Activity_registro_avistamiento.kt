package com.example.hachiapp.Activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hachiapp.BD.ReporteRepository
import com.example.hachiapp.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

class Activity_registro_avistamiento : AppCompatActivity() {

    // --- VARIABLES GLOBALES DE TRABAJO ---
    private val MAPS_REQUEST_CODE = 600
    private var latitud = 0.0
    private var longitud = 0.0
    private var direccion = ""
    private var imagenUri: Uri? = null

    private var reporteId = ""  // ID del reporte al que pertenece este avistamiento
    private var nombreMascota = "" // Almacenará el nombre o tipo heredado

    // --- DECLARACIÓN DE COMPONENTES DE LA INTERFAZ (VISTAS) ---
    private lateinit var btnPerfil: ImageButton
    private lateinit var imgAvistamiento: ImageView
    private lateinit var etDescripcion: EditText
    private lateinit var cardMapa: CardView
    private lateinit var tvDireccion: TextView
    private lateinit var btnPublicar: Button

    // --- INSTANCIAS DE SERVICIOS ---
    private val repository = ReporteRepository()

    // --- REGISTRO DEL SELECTOR DE IMÁGENES ---
    private val seleccionarImagen = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            imagenUri = uri
            imgAvistamiento.setImageURI(uri)
            imgAvistamiento.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_avistamiento)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        // Recibe el ID del reporte y el nombre de la mascota desde el mapa
        reporteId = intent.getStringExtra("reporteId") ?: ""
        nombreMascota = intent.getStringExtra("nombreMascota") ?: "Mascota"

        setupListeners()
    }

    // Vincula cada variable de Kotlin con su respectivo ID definido en el archivo XML
    private fun initViews() {
        btnPerfil = findViewById(R.id.BtnPerfil)
        imgAvistamiento = findViewById(R.id.imgAvistamiento)
        etDescripcion = findViewById(R.id.etDescripcionAvistamiento)
        cardMapa = findViewById(R.id.cardMapaAvistamiento)
        tvDireccion = findViewById(R.id.tvDireccionAvistamiento)
        btnPublicar = findViewById(R.id.btnEnviarAvistamiento)
        // 🛠️ Se eliminó la inicialización del Spinner que causaba el fallo
    }

    private fun setupListeners() {
        btnPerfil.setOnClickListener { }

        imgAvistamiento.setOnClickListener {
            abrirOrigenCamaraOGaleria()
        }

        cardMapa.setOnClickListener {
            abrirMapaSeleccion()
        }

        btnPublicar.setOnClickListener {
            validarYPublicarAvistamiento()
        }
    }

    private fun abrirOrigenCamaraOGaleria() {
        seleccionarImagen.launch(arrayOf("image/*"))
    }

    private fun abrirMapaSeleccion() {
        val intent = Intent(this, SeleccionarUbicacionActivity::class.java)
        intent.putExtra("latitud", latitud)
        intent.putExtra("longitud", longitud)
        startActivityForResult(intent, MAPS_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MAPS_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            latitud = data.getDoubleExtra("latitud", 0.0)
            longitud = data.getDoubleExtra("longitud", 0.0)
            direccion = data.getStringExtra("direccion") ?: ""

            tvDireccion.text = if (direccion.isNotEmpty()) "$direccion" else "Ubicación fijada en el mapa"
            tvDireccion.setTextColor(Color.BLACK)
        }
    }

    // Verifica que los datos obligatorios estén completos antes de iniciar el procesamiento en la nube
    private fun validarYPublicarAvistamiento() {
        val description = etDescripcion.text.toString().trim()
        val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Validaciones básicas de texto y mapa
        if (description.isEmpty()) {
            etDescripcion.error = "Por favor, añade una descripción"
            return
        }

        if (latitud == 0.0 || longitud == 0.0) {
            Toast.makeText(this, "Debes marcar la ubicación en el mapa", Toast.LENGTH_LONG).show()
            return
        }

        // Bloqueamos el botón para evitar doble envío
        btnPublicar.isEnabled = false

        Toast.makeText(this, "Publicando avistamiento...", Toast.LENGTH_SHORT).show()

        /* 🛠️ SOLUCIÓN: Usamos el nombre de la mascota vinculada
         * en lugar de intentar leer el Spinner inexistente
         */
        guardarEnFirebase(usuarioId, nombreMascota, description, "")
    }

    private fun guardarEnFirebase(uid: String, tipo: String, desc: String, urlFoto: String) {
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

        repository.guardarAvistamiento(avistamientoMap,
            onSuccess = {
                runOnUiThread {
                    Toast.makeText(this, "¡Avistamiento publicado con éxito!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            },
            onError = { e ->
                runOnUiThread {
                    Toast.makeText(this, "Error en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                    btnPublicar.isEnabled = true
                }
            }
        )
    }
}
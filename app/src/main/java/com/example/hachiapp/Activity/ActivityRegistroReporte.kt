package com.example.hachiapp.Activity

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.hachiapp.BD.CloudinaryManager
import com.example.hachiapp.BD.ReporteRepository
import com.example.hachiapp.R
import com.example.hachiapp.models.Reporte
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

class ActivityRegistroReporte : AppCompatActivity() {

    // Constantes
    private val REQUEST_SELECCIONAR_UBICACION = 500

    // ── Ubicación ─────────────────────────────────────────────────────────────
    private var direccionSeleccionada = ""
    private var latitudSeleccionada = 0.0
    private var longitudSeleccionada = 0.0

    // ── Imágenes ──────────────────────────────────────────────────────────────
    private val imagenesSeleccionadas = mutableListOf<Uri>()
    private val MAX_IMAGENES = 5

    // ── Scroll (fix mapa) ─────────────────────────────────────────────────────
    private lateinit var scrollViewPrincipal: ScrollView

    // ── ImageView principal ───────────────────────────────────────────────────
    private lateinit var imgPrincipal: ImageView

    // ── Seleccionar imágenes (una por una) ────────────────────────────────────
    private val seleccionarImagenes =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult

            if (imagenesSeleccionadas.size >= MAX_IMAGENES) {
                Toast.makeText(this, "Máximo $MAX_IMAGENES fotos permitidas", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { /* ignorar */ }

            imagenesSeleccionadas.add(uri)
            actualizarMiniaturas()
            actualizarImagenPrincipal()
        }

    // ── Referencias a vistas ──────────────────────────────────────────────────
    private lateinit var btnGuardar: View
    private lateinit var etNombreMascota: EditText
    private lateinit var etRazaMascota: EditText
    private lateinit var etColorMascota: EditText
    private lateinit var etTamanoMascota: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etNotaAdicional: EditText
    private lateinit var etRecompensa: EditText
    private lateinit var spinnerTipoMascota: Spinner
    private lateinit var spinnerEdad: Spinner
    private lateinit var spinnerEstado: Spinner
    private lateinit var tvFecha: TextView
    private lateinit var tvDireccionSeleccionada: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_reporte)

        CloudinaryManager.init(this)

        // ── ScrollView ────────────────────────────────────────────────────────
        scrollViewPrincipal = findViewById(R.id.scrollViewPrincipal)

        // ── Configurar el card del mapa para abrir la actividad de selección ───
        val cardMapa = findViewById<View>(R.id.cardMapa)
        cardMapa.setOnClickListener {
            val intent = Intent(this, SeleccionarUbicacionActivity::class.java)
            // Enviar ubicación actual si existe (para mostrar el marcador existente)
            intent.putExtra("latitud", latitudSeleccionada)
            intent.putExtra("longitud", longitudSeleccionada)
            startActivityForResult(intent, REQUEST_SELECCIONAR_UBICACION)
        }

        // Configurar el TouchListener para el scroll (evita conflictos)
        val mapTouchContainer = findViewById<View>(R.id.mapTouchContainer)
        mapTouchContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE ->
                    scrollViewPrincipal.requestDisallowInterceptTouchEvent(true)

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL ->
                    scrollViewPrincipal.requestDisallowInterceptTouchEvent(false)
            }
            false
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ── Campos del XML ────────────────────────────────────────────────────
        etNombreMascota = findViewById(R.id.etNombreMascota)
        etRazaMascota = findViewById(R.id.etRazaMascota)
        etColorMascota = findViewById(R.id.etColorMascota)
        etTamanoMascota = findViewById(R.id.etTamano)
        etDescripcion = findViewById(R.id.etDescripcion)
        etNotaAdicional = findViewById(R.id.etNotaAdicional)
        etRecompensa = findViewById(R.id.etRecompensa)
        spinnerTipoMascota = findViewById(R.id.spinnerTipoMascota)
        spinnerEdad = findViewById(R.id.spinnerEdad)
        spinnerEstado = findViewById(R.id.spinnerEstado)
        tvFecha = findViewById(R.id.tvFecha)
        tvDireccionSeleccionada = findViewById(R.id.tvDireccionSeleccionada)
        imgPrincipal = findViewById(R.id.btnFotoReporte)

        val btnCalendario = findViewById<ImageButton>(R.id.btnCalendario)
        val viewColorPreview = findViewById<View>(R.id.viewColorPreview)
        btnGuardar = findViewById(R.id.btnRealizarReporte)

        spinnerTipoMascota.setOnItemSelectedListener(spinnerItemSelectedListener)
        spinnerEdad.setOnItemSelectedListener(spinnerItemSelectedListener)
        spinnerEstado.setOnItemSelectedListener(spinnerItemSelectedListener)

        val repository = ReporteRepository()

        // ── Paleta de colores ─────────────────────────────────────────────────
        val colorMap = mapOf(
            R.id.colorNegro to Pair("#1A1A1A", "Negro"),
            R.id.colorGrisOscuro to Pair("#555555", "Gris oscuro"),
            R.id.colorGrisClaro to Pair("#BBBBBB", "Gris claro"),
            R.id.colorBlanco to Pair("#F5F5F5", "Blanco"),
            R.id.colorCafeOscuro to Pair("#4E2A04", "Café oscuro"),
            R.id.colorCafe to Pair("#8B4513", "Café"),
            R.id.colorBeige to Pair("#D2B48C", "Beige"),
            R.id.colorDorado to Pair("#DAA520", "Dorado"),
            R.id.colorNaranja to Pair("#E07020", "Naranja"),
            R.id.colorAmarillo to Pair("#F5D000", "Amarillo"),
            R.id.colorRojo to Pair("#CC2200", "Rojo"),
            R.id.colorRosa to Pair("#F4A0B0", "Rosa"),
            R.id.colorAzul to Pair("#1565C0", "Azul"),
            R.id.colorAzulClaro to Pair("#64B5F6", "Azul claro"),
            R.id.colorVerde to Pair("#2E7D32", "Verde"),
            R.id.colorVerdeClaro to Pair("#A5D6A7", "Verde claro")
        )

        for ((id, data) in colorMap) {
            findViewById<View>(id).setOnClickListener {
                val (hex, nombre) = data
                etColorMascota.setText(nombre)
                viewColorPreview.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(hex))
            }
        }

        // ── Fecha ─────────────────────────────────────────────────────────────
        btnCalendario.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    tvFecha.text = "$day/${month + 1}/$year"
                    tvFecha.setTextColor(Color.BLACK)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // ── Botón agregar fotos ───────────────────────────────────────────────
        val btnFoto = findViewById<View>(R.id.btnFoto)
        btnFoto.setOnClickListener {
            seleccionarImagenes.launch(arrayOf("image/*"))
        }

        // ── FIX RECREACIÓN: restaurar imágenes si el Activity fue recreado ────
        savedInstanceState?.getParcelableArrayList<Uri>("imagenesSeleccionadas")?.let { uris ->
            imagenesSeleccionadas.addAll(uris)
            actualizarMiniaturas()
            actualizarImagenPrincipal()
        }

        // ── Botón guardar ─────────────────────────────────────────────────────
        btnGuardar.setOnClickListener {
            val nombreMascota = etNombreMascota.text.toString().trim()
            val razaMascota = etRazaMascota.text.toString().trim()
            val colorMascota = etColorMascota.text.toString().trim()
            val tamano = etTamanoMascota.text.toString().trim()
            val fechaExtravio = tvFecha.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val notaAdicional = etNotaAdicional.text.toString().trim()
            val recompensa = etRecompensa.text.toString().trim()
            val tipoMascota = spinnerTipoMascota.selectedItem.toString()
            val edadMascota = spinnerEdad.selectedItem.toString()
            val estadoMascota = spinnerEstado.selectedItem.toString()

            if (nombreMascota.isEmpty() || razaMascota.isEmpty() ||
                colorMascota.isEmpty() || tamano.isEmpty() ||
                descripcion.isEmpty()
            ) {
                Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fechaExtravio.isEmpty() || fechaExtravio == "Fecha") {
                Toast.makeText(this, "Seleccione la fecha de extravío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (latitudSeleccionada == 0.0 && longitudSeleccionada == 0.0) {
                Toast.makeText(this, "Seleccione la ubicación en el mapa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                Toast.makeText(this, "Debes iniciar sesión para reportar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnGuardar.isEnabled = false
            Toast.makeText(this, "Subiendo imágenes…", Toast.LENGTH_SHORT).show()

            subirImagenes(
                uris = imagenesSeleccionadas.toList(),
                usuarioId = usuarioId,
                onComplete = { urlsImagenes ->
                    val reporte = Reporte(
                        usuarioId = usuarioId,
                        nombreMascota = nombreMascota,
                        tipoMascota = tipoMascota,
                        razaMascota = razaMascota,
                        colorMascota = colorMascota,
                        tamano = tamano,
                        edadMascota = edadMascota,
                        estadoMascota = estadoMascota,
                        descripcion = descripcion,
                        notaAdicional = notaAdicional,
                        recompensa = recompensa,
                        fechaExtravio = fechaExtravio,
                        direccion = direccionSeleccionada,
                        latitud = latitudSeleccionada,
                        longitud = longitudSeleccionada,
                        imagenesUrl = urlsImagenes,
                        fechaPublicacion = Timestamp.now()
                    )

                    repository.guardarReporte(
                        reporte = reporte,
                        onSuccess = {
                            runOnUiThread {
                                Toast.makeText(this, "Reporte guardado con éxito", Toast.LENGTH_SHORT).show()
                                btnGuardar.isEnabled = true
                                finish()
                            }
                        },
                        onError = { e ->
                            runOnUiThread {
                                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                                btnGuardar.isEnabled = true
                            }
                        }
                    )
                },
                onFailure = { e ->
                    runOnUiThread {
                        Toast.makeText(this, "Error al subir imágenes: ${e.message}", Toast.LENGTH_SHORT).show()
                        btnGuardar.isEnabled = true
                    }
                }
            )
        }
    }

    // ── Manejar el resultado de SeleccionarUbicacionActivity ──────────────────
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SELECCIONAR_UBICACION && resultCode == RESULT_OK && data != null) {
            latitudSeleccionada = data.getDoubleExtra("latitud", 0.0)
            longitudSeleccionada = data.getDoubleExtra("longitud", 0.0)
            direccionSeleccionada = data.getStringExtra("direccion") ?: ""

            if (direccionSeleccionada.isNotEmpty()) {
                tvDireccionSeleccionada.text = direccionSeleccionada
                tvDireccionSeleccionada.setTextColor(Color.BLACK)
            } else {
                tvDireccionSeleccionada.text = "Ubicación seleccionada (sin dirección)"
                tvDireccionSeleccionada.setTextColor(Color.BLACK)
            }
        }
    }

    // FIX RECREACIÓN: guardar lista de imágenes antes de que el Activity muera
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            "imagenesSeleccionadas",
            ArrayList(imagenesSeleccionadas)
        )
    }

    // ── Spinner listener ──────────────────────────────────────────────────────
    private val spinnerItemSelectedListener =
        object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                (view as? TextView)?.setTextColor(Color.BLACK)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

    // ── Imagen principal ──────────────────────────────────────────────────────
    private fun actualizarImagenPrincipal() {
        if (imagenesSeleccionadas.isNotEmpty()) {
            imgPrincipal.setImageURI(imagenesSeleccionadas.first())
            imgPrincipal.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            imgPrincipal.setImageResource(R.drawable.ic_camera_alt_24)
            imgPrincipal.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    // ── Subida a Cloudinary ───────────────────────────────────────────────────
    private fun subirImagenes(
        uris: List<Uri>,
        usuarioId: String,
        onComplete: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (uris.isEmpty()) {
            onComplete(emptyList())
            return
        }

        val urlsSubidas = mutableListOf<String>()
        val contador = java.util.concurrent.atomic.AtomicInteger(0)
        val huboError = java.util.concurrent.atomic.AtomicBoolean(false)

        for (uri in uris) {
            MediaManager.get()
                .upload(uri)
                .unsigned("hachiapp")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}

                    override fun onSuccess(requestId: String, resultData: MutableMap<Any?, Any?>) {
                        if (huboError.get()) return
                        val url = resultData["secure_url"]?.toString() ?: ""
                        synchronized(urlsSubidas) {
                            urlsSubidas.add(url)
                            if (contador.incrementAndGet() == uris.size) {
                                onComplete(urlsSubidas.toList())
                            }
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        if (huboError.compareAndSet(false, true)) {
                            onFailure(Exception(error.description ?: "Error desconocido al subir imagen"))
                        }
                    }
                })
                .dispatch(this@ActivityRegistroReporte)
        }
    }

    // ── Miniaturas con botón X para borrar ────────────────────────────────────
    private fun actualizarMiniaturas() {
        val layout = findViewById<LinearLayout>(R.id.layoutMiniaturas)
        layout.removeAllViews()

        for ((index, uri) in imagenesSeleccionadas.withIndex()) {
            // Contenedor relativo para imagen + botón X encima
            val contenedor = android.widget.RelativeLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(44), dpToPx(44)
                ).apply { marginEnd = dpToPx(4) }
            }

            // Miniatura
            val imageView = ImageView(this).apply {
                layoutParams = android.widget.RelativeLayout.LayoutParams(
                    dpToPx(40), dpToPx(40)
                ).apply {
                    addRule(android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM)
                    addRule(android.widget.RelativeLayout.ALIGN_PARENT_START)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
            }

            // Tap en miniatura → mostrar en imagen principal
            imageView.setOnClickListener {
                imgPrincipal.setImageURI(uri)
                imgPrincipal.scaleType = ImageView.ScaleType.CENTER_CROP
            }

            // Botón X rojo en esquina superior derecha
            val btnEliminar = TextView(this).apply {
                layoutParams = android.widget.RelativeLayout.LayoutParams(
                    dpToPx(16), dpToPx(16)
                ).apply {
                    addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP)
                    addRule(android.widget.RelativeLayout.ALIGN_PARENT_END)
                }
                text = "✕"
                textSize = 8f
                gravity = android.view.Gravity.CENTER
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#CC0000"))
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(Color.parseColor("#CC0000"))
                }
            }

            btnEliminar.setOnClickListener {
                imagenesSeleccionadas.removeAt(index)
                actualizarMiniaturas()
                actualizarImagenPrincipal()
            }

            contenedor.addView(imageView)
            contenedor.addView(btnEliminar)
            layout.addView(contenedor)
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}
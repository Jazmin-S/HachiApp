package com.example.hachiapp.Activity

import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.example.hachiapp.BD.ReporteRepository
import com.example.hachiapp.R
import com.example.hachiapp.models.Reporte
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class ActivityRegistroReporte :
    AppCompatActivity(),
    OnMapReadyCallback {

    // ── Mapa ──────────────────────────────────────────────────────────────────
    private lateinit var mMap: GoogleMap
    private var direccionSeleccionada = ""
    private var latitudSeleccionada   = 0.0
    private var longitudSeleccionada  = 0.0
    private val LOCATION_PERMISSION_REQUEST = 100

    // ── Imágenes ──────────────────────────────────────────────────────────────
    // Lista de URIs locales seleccionadas por el usuario (máximo 5)
    private val imagenesSeleccionadas = mutableListOf<Uri>()
    private val MAX_IMAGENES = 5

    // Lanzador para selección múltiple de imágenes desde la galería
    private val seleccionarImagenes =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris.isNullOrEmpty()) return@registerForActivityResult

            // Calcula cuántos espacios quedan disponibles
            val espacioDisponible = MAX_IMAGENES - imagenesSeleccionadas.size

            if (espacioDisponible <= 0) {
                Toast.makeText(this, "Máximo $MAX_IMAGENES fotos permitidas", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            // Solo agrega las que caben
            val nuevas = uris.take(espacioDisponible)
            imagenesSeleccionadas.addAll(nuevas)

            if (uris.size > espacioDisponible) {
                Toast.makeText(
                    this,
                    "Solo se agregaron $espacioDisponible foto(s). Límite: $MAX_IMAGENES",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Refresca la fila de miniaturas en pantalla
            actualizarMiniaturas()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_reporte)

        // Inicializa el mapa
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        pedirPermisoUbicacion()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ── Campos ───────────────────────────────────────────────────────────
        val etNombreMascota = findViewById<EditText>(R.id.etNombreMascota)
        val etRazaMascota   = findViewById<EditText>(R.id.etRazaMascota)
        val etColorMascota  = findViewById<EditText>(R.id.etColorMascota)
        val etTamanoMascota = findViewById<EditText>(R.id.etTamano)
        val etDescripcion   = findViewById<EditText>(R.id.etDescripcion)
        val etNotaAdicional = findViewById<EditText>(R.id.etNotaAdicional)
        val etRecompensa    = findViewById<EditText>(R.id.etRecompensa)

        val spinnerTipoMascota = findViewById<Spinner>(R.id.spinnerTipoMascota)
        val spinnerEdad        = findViewById<Spinner>(R.id.spinnerEdad)
        val spinnerEstado      = findViewById<Spinner>(R.id.spinnerEstado)

        val tvDireccionSeleccionada = findViewById<TextView>(R.id.tvDireccionSeleccionada)
        val tvFecha                 = findViewById<TextView>(R.id.tvFecha)
        val btnCalendario           = findViewById<ImageButton>(R.id.btnCalendario)
        val viewColorPreview        = findViewById<View>(R.id.viewColorPreview)

        val repository = ReporteRepository()

        // ── Paleta de colores ─────────────────────────────────────────────────
        val colorMap = mapOf(
            R.id.colorNegro      to Pair("#1A1A1A", "Negro"),
            R.id.colorGrisOscuro to Pair("#555555", "Gris oscuro"),
            R.id.colorGrisClaro  to Pair("#BBBBBB", "Gris claro"),
            R.id.colorBlanco     to Pair("#F5F5F5", "Blanco"),
            R.id.colorCafeOscuro to Pair("#4E2A04", "Café oscuro"),
            R.id.colorCafe       to Pair("#8B4513", "Café"),
            R.id.colorBeige      to Pair("#D2B48C", "Beige"),
            R.id.colorDorado     to Pair("#DAA520", "Dorado"),
            R.id.colorNaranja    to Pair("#E07020", "Naranja"),
            R.id.colorAmarillo   to Pair("#F5D000", "Amarillo"),
            R.id.colorRojo       to Pair("#CC2200", "Rojo"),
            R.id.colorRosa       to Pair("#F4A0B0", "Rosa"),
            R.id.colorAzul       to Pair("#1565C0", "Azul"),
            R.id.colorAzulClaro  to Pair("#64B5F6", "Azul claro"),
            R.id.colorVerde      to Pair("#2E7D32", "Verde"),
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
        // Reutilizamos el CardView btnFoto que ya existe en tu XML
        val btnFoto = findViewById<View>(R.id.btnFoto)
        btnFoto.setOnClickListener {
            pedirPermisoGaleria()
        }

        // ── Botón guardar ─────────────────────────────────────────────────────
        val btnGuardar = findViewById<View>(R.id.btnRealizarReporte)
        btnGuardar.setOnClickListener {

            val nombreMascota = etNombreMascota.text.toString().trim()
            val razaMascota   = etRazaMascota.text.toString().trim()
            val colorMascota  = etColorMascota.text.toString().trim()
            val tamano        = etTamanoMascota.text.toString().trim()
            val fechaExtravio = tvFecha.text.toString().trim()
            val descripcion   = etDescripcion.text.toString().trim()
            val notaAdicional = etNotaAdicional.text.toString().trim()
            val recompensa    = etRecompensa.text.toString().trim()
            val edadMascota   = spinnerEdad.selectedItem.toString()
            val estadoMascota = spinnerEstado.selectedItem.toString()

            if (nombreMascota.isEmpty() || razaMascota.isEmpty() ||
                colorMascota.isEmpty()  || tamano.isEmpty()      ||
                descripcion.isEmpty()
            ) {
                Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            // Deshabilita el botón para evitar doble envío mientras se suben las imágenes
            btnGuardar.isEnabled = false
            Toast.makeText(this, "Subiendo imágenes…", Toast.LENGTH_SHORT).show()

            // Sube las imágenes a Storage y luego guarda el reporte
            subirImagenesYGuardar(
                uris          = imagenesSeleccionadas,
                usuarioId     = usuarioId,
                onComplete    = { urls ->

                    val reporte = Reporte(
                        nombreMascota = nombreMascota,
                        razaMascota   = razaMascota,
                        colorMascota  = colorMascota,
                        tamano        = tamano,
                        edadMascota   = edadMascota,
                        estadoMascota = estadoMascota,
                        fechaExtravio = fechaExtravio,
                        descripcion   = descripcion,
                        notaAdicional = notaAdicional,
                        direccion     = direccionSeleccionada,
                        imagenes      = urls,           // URLs definitivas de Firebase Storage
                        usuarioId     = usuarioId,
                        tipoReporte   = spinnerTipoMascota.selectedItem.toString(),
                        recompensa    = recompensa,
                        fechaCreacion = Timestamp.now()
                    )

                    repository.guardarReporte(
                        reporte,
                        onSuccess = {
                            Toast.makeText(this, "Reporte guardado correctamente", Toast.LENGTH_SHORT).show()
                            btnGuardar.isEnabled = true
                        },
                        onError = { error ->
                            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                            btnGuardar.isEnabled = true
                        }
                    )
                },
                onError = { error ->
                    Toast.makeText(this, "Error al subir imagen: ${error.message}", Toast.LENGTH_LONG).show()
                    btnGuardar.isEnabled = true
                }
            )
        }
    }

    // ── Imágenes ──────────────────────────────────────────────────────────────

    /**
     * Sube todas las URIs a Firebase Storage en paralelo.
     * Cuando todas terminan llama a onComplete con la lista de URLs de descarga.
     * Si alguna falla llama a onError.
     */
    private fun subirImagenesYGuardar(
        uris: List<Uri>,
        usuarioId: String,
        onComplete: (List<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        // Si no hay imágenes, guarda el reporte sin URLs
        if (uris.isEmpty()) {
            onComplete(emptyList())
            return
        }

        val storage   = FirebaseStorage.getInstance()
        val urlsSubidas = mutableListOf<String>()
        var contador  = 0

        for (uri in uris) {
            // Ruta única en Storage: reportes/{usuarioId}/{uuid}.jpg
            val ref = storage.reference
                .child("reportes/$usuarioId/${UUID.randomUUID()}.jpg")

            ref.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) throw task.exception!!
                    ref.downloadUrl          // Obtiene la URL pública de descarga
                }
                .addOnSuccessListener { url ->
                    urlsSubidas.add(url.toString())
                    contador++
                    // Cuando todas las imágenes terminaron, notifica
                    if (contador == uris.size) onComplete(urlsSubidas)
                }
                .addOnFailureListener { error ->
                    onError(error)
                }
        }
    }

    /**
     * Refresca el LinearLayout de miniaturas (layoutMiniaturas en el XML).
     * Muestra cada imagen seleccionada en un ImageView cuadrado con una
     * X para eliminarla individualmente.
     */
    private fun actualizarMiniaturas() {
        val layout = findViewById<LinearLayout>(R.id.layoutMiniaturas)
        layout.removeAllViews()

        for ((index, uri) in imagenesSeleccionadas.withIndex()) {

            // Contenedor relativo para superponer la X sobre la miniatura
            val contenedor = android.widget.RelativeLayout(this).apply {
                val size = dpToPx(80)
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = dpToPx(8)
                }
            }

            // Miniatura de la imagen
            val imageView = ImageView(this).apply {
                layoutParams = android.widget.RelativeLayout.LayoutParams(
                    android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
                    android.widget.RelativeLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
                // Bordes redondeados mediante clipToOutline
                clipToOutline = true
                background = ContextCompat.getDrawable(context, R.drawable.circle_color_swatch)
            }

            // Botón X para eliminar esa imagen
            val btnEliminar = TextView(this).apply {
                val btnSize = dpToPx(22)
                layoutParams = android.widget.RelativeLayout.LayoutParams(btnSize, btnSize).apply {
                    addRule(android.widget.RelativeLayout.ALIGN_PARENT_TOP)
                    addRule(android.widget.RelativeLayout.ALIGN_PARENT_END)
                }
                text = "✕"
                textSize = 11f
                gravity = android.view.Gravity.CENTER
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.parseColor("#CC000000"))
            }

            btnEliminar.setOnClickListener {
                imagenesSeleccionadas.removeAt(index)
                actualizarMiniaturas()
            }

            contenedor.addView(imageView)
            contenedor.addView(btnEliminar)
            layout.addView(contenedor)
        }
    }

    /** Convierte dp a píxeles según la densidad de pantalla */
    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    // ── Permisos galería ──────────────────────────────────────────────────────

    /**
     * Solicita el permiso de lectura de imágenes según la versión de Android:
     * - Android 13+: READ_MEDIA_IMAGES
     * - Android 12 o inferior: READ_EXTERNAL_STORAGE
     * Si ya tiene el permiso abre la galería directamente.
     */
    private fun pedirPermisoGaleria() {
        val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            android.Manifest.permission.READ_MEDIA_IMAGES
        else
            android.Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED) {
            seleccionarImagenes.launch("image/*")
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permiso), 200)
        }
    }

    // ── Mapa ──────────────────────────────────────────────────────────────────

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        val mexicoDefault = LatLng(23.6345, -102.5528)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mexicoDefault, 5f))

        activarMiUbicacion()

        mMap.setOnMapClickListener { latLng ->
            latitudSeleccionada  = latLng.latitude
            longitudSeleccionada = latLng.longitude
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Última vez visto"))
            obtenerDireccion(latLng)
        }
    }

    private fun activarMiUbicacion() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            LocationServices.getFusedLocationProviderClient(this)
                .lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(location.latitude, location.longitude), 15f
                            )
                        )
                    }
                }
        }
    }

    private fun obtenerDireccion(latLng: LatLng) {
        val tvDireccion = findViewById<TextView>(R.id.tvDireccionSeleccionada)
        try {
            @Suppress("DEPRECATION")
            val resultados = Geocoder(this, Locale.getDefault())
                .getFromLocation(latLng.latitude, latLng.longitude, 1)
            direccionSeleccionada =
                if (!resultados.isNullOrEmpty()) resultados[0].getAddressLine(0) ?: ""
                else "${latLng.latitude}, ${latLng.longitude}"
        } catch (e: Exception) {
            direccionSeleccionada = "${latLng.latitude}, ${latLng.longitude}"
        }
        tvDireccion.text = direccionSeleccionada
        tvDireccion.setTextColor(Color.parseColor("#333333"))
    }

    // ── Permisos ubicación ────────────────────────────────────────────────────

    private fun pedirPermisoUbicacion() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            // Permiso de ubicación concedido: activar capa en el mapa
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) activarMiUbicacion()
            }
            // Permiso de galería concedido: abrir selector de imágenes
            200 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) seleccionarImagenes.launch("image/*")
                else Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
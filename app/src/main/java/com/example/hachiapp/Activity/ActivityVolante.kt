package com.example.hachiapp.Activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hachiapp.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityVolante : AppCompatActivity(), OnMapReadyCallback {

    private var latitud   = 0.0
    private var longitud  = 0.0
    private var nombre    = ""
    private var reporteId = ""
    private var usuarioId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volante)

        // Leer extras del Intent
        nombre    = intent.getStringExtra("nombreMascota") ?: ""
        latitud   = intent.getDoubleExtra("latitud", 0.0)
        longitud  = intent.getDoubleExtra("longitud", 0.0)
        usuarioId = intent.getStringExtra("usuarioId") ?: ""
        reporteId = intent.getStringExtra("reporteId") ?: ""

        // Poblar vistas
        findViewById<TextView>(R.id.txtNombre).text = nombre
        findViewById<TextView>(R.id.txtRaza).text = "Raza: ${intent.getStringExtra("razaMascota") ?: ""}"
        findViewById<TextView>(R.id.txtEdad).text = "Edad: ${intent.getStringExtra("edadMascota") ?: ""}"
        findViewById<TextView>(R.id.txtTamano).text = "Tamaño: ${intent.getStringExtra("tamano") ?: ""}"
        findViewById<TextView>(R.id.txtColor).text = "Color: ${intent.getStringExtra("colorMascota") ?: ""}"
        findViewById<TextView>(R.id.txtFecha).text = intent.getStringExtra("fechaExtravio") ?: ""
        findViewById<TextView>(R.id.TxtDireccion).text = intent.getStringExtra("direccion") ?: "Sin dirección"
        findViewById<TextView>(R.id.txtEspecificaciones).text = intent.getStringExtra("descripcion") ?: ""
        findViewById<TextView>(R.id.recompensa).text = "Recompensa: ${intent.getStringExtra("recompensa") ?: "Sin recompensa"}"

        // Imagen
        val imagenUrl = intent.getStringExtra("imagenUrl")
        val imgMascota = findViewById<ImageView>(R.id.imgMascota)
        if (!imagenUrl.isNullOrEmpty()) {
            Glide.with(this).load(imagenUrl).placeholder(R.drawable.images).into(imgMascota)
        }

        // Nombre del dueño desde Firestore
        val txtDueno = findViewById<TextView>(R.id.txtDueno)
        if (usuarioId.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(usuarioId)
                .get()
                .addOnSuccessListener { doc ->
                    txtDueno.text = doc.getString("nombre") ?: "Desconocido"
                }
        }

        // Botones
        val btnEncontrado = findViewById<LinearLayout>(R.id.btnEncontrado)
        val btnEditar     = findViewById<LinearLayout>(R.id.btnEditar)
        val btnMensajes   = findViewById<LinearLayout>(R.id.btnMensajes)

        val uidActual = FirebaseAuth.getInstance().currentUser?.uid

        if (uidActual != null && uidActual == usuarioId) {
            // Es el dueño → mostrar Encontrado y Editar, ocultar Mensajes
            btnEncontrado.visibility = View.VISIBLE
            btnEditar.visibility     = View.VISIBLE
            btnMensajes.visibility   = View.GONE

            // Encontrado → eliminar el reporte de Firestore
            btnEncontrado.setOnClickListener {
                if (reporteId.isEmpty()) {
                    Toast.makeText(this, "Error: ID del reporte no encontrado", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                FirebaseFirestore.getInstance()
                    .collection("reportes")
                    .document(reporteId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "¡Qué buena noticia! Reporte eliminado.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

            // Editar → abrir ActivityRegistroReporte con datos precargados
            btnEditar.setOnClickListener {
                val intent = Intent(this, ActivityRegistroReporte::class.java)
                intent.putExtra("modoEdicion", true)
                intent.putExtra("reporteId",    reporteId)
                intent.putExtra("nombreMascota", nombre)
                intent.putExtra("razaMascota",   this.intent.getStringExtra("razaMascota") ?: "")
                intent.putExtra("edadMascota",   this.intent.getStringExtra("edadMascota") ?: "")
                intent.putExtra("tamano",        this.intent.getStringExtra("tamano") ?: "")
                intent.putExtra("colorMascota",  this.intent.getStringExtra("colorMascota") ?: "")
                intent.putExtra("descripcion",   this.intent.getStringExtra("descripcion") ?: "")
                intent.putExtra("fechaExtravio", this.intent.getStringExtra("fechaExtravio") ?: "")
                intent.putExtra("estadoMascota", this.intent.getStringExtra("estadoMascota") ?: "")
                intent.putExtra("direccion",     this.intent.getStringExtra("direccion") ?: "")
                intent.putExtra("recompensa",    this.intent.getStringExtra("recompensa") ?: "")
                intent.putExtra("latitud",       latitud)
                intent.putExtra("longitud",      longitud)
                intent.putExtra("imagenUrl",     this.intent.getStringExtra("imagenUrl") ?: "")
                startActivity(intent)
            }

        } else {
            // No es el dueño → ocultar Encontrado y Editar, mostrar Mensajes
            btnEncontrado.visibility = View.GONE
            btnEditar.visibility     = View.GONE
            btnMensajes.visibility   = View.VISIBLE

            // Mensajes → ir a ActivityChat con el usuarioId del dueño
            btnMensajes.setOnClickListener {
                val intent = Intent(this, ActivityChat::class.java)
                intent.putExtra("receptorId",     usuarioId)
                intent.putExtra("nombreMascota",  nombre)
                intent.putExtra("reporteId",      reporteId)
                startActivity(intent)
            }
        }



        /*Se configura el botón de descarga
        * para capturar el ScrollView como imagen
        * y guardarlo en la galería del teléfono.
        */
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDescargarVolante)
            .setOnClickListener {
                descargarVolante()
            }
        /*Se carga la imagen estática del mapa
         * usando Glide con la URL de Static Maps.
         */
        if (latitud != 0.0 && longitud != 0.0) {
            val urlMapa = obtenerUrlMapaEstatico(latitud, longitud)
            Glide.with(this)
                .load(urlMapa)
                .into(findViewById(R.id.imgMapaEstatico))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (latitud != 0.0 || longitud != 0.0) {
            val ubicacion = LatLng(latitud, longitud)
            googleMap.addMarker(MarkerOptions().position(ubicacion).title(nombre))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15f))
        }
        googleMap.uiSettings.isScrollGesturesEnabled = false
    }
    /*
   * Captura el contenido del volante como bitmap
   * y lo guarda en la galería del teléfono.
   * No requiere parámetros.
   */
    private fun descargarVolante() {

        /*Se obtiene el LinearLayout interno del ScrollView
         * que contiene todo el contenido del volante.
         */
        val scrollView = findViewById<ScrollView>(R.id.ScrollView)
        val contenido = scrollView.getChildAt(0)

        /*Se mide y dibuja el contenido completo
         * para capturar todo aunque no sea visible en pantalla.
         */
        contenido.measure(
            View.MeasureSpec.makeMeasureSpec(scrollView.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        contenido.layout(0, 0, contenido.measuredWidth, contenido.measuredHeight)

        /*Se crea el bitmap con el tamaño completo del contenido.*/
        val bitmap = Bitmap.createBitmap(
            contenido.measuredWidth,
            contenido.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        contenido.draw(canvas)

        /*Se guarda la imagen en la galería.*/
        val nombreArchivo = "HachiApp_Volante_${System.currentTimeMillis()}.png"

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val valores = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, nombreArchivo)
                    put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(android.provider.MediaStore.Images.Media.RELATIVE_PATH,
                        android.os.Environment.DIRECTORY_PICTURES + "/HachiApp")
                }
                val uri = contentResolver.insert(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores
                )
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                }
            } else {
                val directorio = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES
                )
                val archivo = java.io.File(directorio, nombreArchivo)
                java.io.FileOutputStream(archivo).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                android.media.MediaScannerConnection.scanFile(
                    this, arrayOf(archivo.absolutePath), null, null
                )
            }
            Toast.makeText(this, "Volante guardado en Galería", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            android.util.Log.e("Volante", "Error al descargar: ", e)
        }
    }
    /*
     * Genera una URL de imagen estática del mapa
     * usando la API de Static Maps de Google.
     * Parámetros:
     */
    private fun obtenerUrlMapaEstatico(lat: Double, lng: Double): String {
        val apiKey = "AIzaSyArWl1AGIS3e4e88lD9LZV9jHa3lrdPoqA"
        return "https://maps.googleapis.com/maps/api/staticmap?" +
                "center=$lat,$lng" +
                "&zoom=15" +
                "&size=600x300" +
                "&markers=color:red%7C$lat,$lng" +
                "&key=$apiKey"
    }

}
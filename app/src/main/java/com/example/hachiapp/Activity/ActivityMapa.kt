package com.example.hachiapp.Activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.hachiapp.R
import com.example.hachiapp.adapters.ReporteInfoWindowAdapter
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityMapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)
        marcarMenuActivo("mapa")


        // INICIO
        val btnInicio = findViewById<LinearLayout>(R.id.BtnInicio)
        btnInicio.setOnClickListener {
            startActivity(Intent(this, ActivityInicio::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // ALERTAS
        val btnAlertas = findViewById<LinearLayout>(R.id.BtnAlertas)
        btnAlertas.setOnClickListener {
            startActivity(Intent(this, ActivityAlertas::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // HISTORIAL
        val btnHistorial = findViewById<LinearLayout>(R.id.BtnHistorial)
        btnHistorial.setOnClickListener {
            startActivity(Intent(this, ActivityHistorial::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // REPORTE MASCOTA PERDIDA
        val btnReporte = findViewById<LinearLayout>(R.id.btnReporte)
        btnReporte.setOnClickListener {
            startActivity(Intent(this, ActivityRegistroReporte::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // 🛠️ Se eliminó por completo el FloatingActionButton de avistamiento rápido

        // MAPA
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapa) as SupportMapFragment
        mapFragment.getMapAsync(this)

        pedirPermisoUbicacion()
    }

    override fun onResume() {
        super.onResume()
        if (::mMap.isInitialized) {
            mMap.clear()
            cargarReportesEnMapa()
            cargarAvistamientosEnMapa()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setInfoWindowAdapter(ReporteInfoWindowAdapter(this))

        mMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        mMap.setOnInfoWindowClickListener { marker ->
            val datos = marker.tag as? Map<*, *>
            val reporteId = datos?.get("reporteId") as? String
            if (reporteId != null) {
                val intent = Intent(this, Activity_registro_avistamiento::class.java)
                intent.putExtra("reporteId", reporteId)
                intent.putExtra("nombreMascota", datos?.get("nombre") as? String ?: "")
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val ubicacionActual = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))
                }
            }

            cargarReportesEnMapa()
            cargarAvistamientosEnMapa()
        }

        // ── Foto de perfil ────────────────────────────────────────────
        val btnPerfil = findViewById<ImageButton>(R.id.BtnPerfil)
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario != null) {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(usuario.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val fotoPerfil = doc.getString("fotoPerfil")  // ← nombre correcto del campo
                    if (!fotoPerfil.isNullOrEmpty() && fotoPerfil.startsWith("http")) {
                        Glide.with(this)
                            .load(fotoPerfil)
                            .transform(CircleCrop())
                            .placeholder(R.drawable.perfil)
                            .error(R.drawable.perfil)
                            .into(btnPerfil)
                    }
                }
        }

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ActivityPerfil::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun cargarReportesEnMapa() {
        db.collection("reportes")
            .get()
            .addOnSuccessListener { documentos ->
                for (documento in documentos) {
                    val lat = documento.getDouble("latitud")
                    val lng = documento.getDouble("longitud")
                    val nombre = documento.getString("nombreMascota") ?: "Sin nombre"
                    val estado = documento.getString("estadoMascota") ?: "Perdido"
                    val reporteId = documento.id

                    val imagenUrl = (documento.get("imagenesUrl") as? List<*>)
                        ?.firstOrNull()?.toString() ?: ""

                    if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                        val posicion = LatLng(lat, lng)

                        val colorMarcador = when (estado.lowercase()) {
                            "perdido" -> BitmapDescriptorFactory.HUE_RED
                            "visto" -> BitmapDescriptorFactory.HUE_YELLOW
                            "en resguardo" -> BitmapDescriptorFactory.HUE_GREEN
                            else -> BitmapDescriptorFactory.HUE_RED
                        }

                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(posicion)
                                .title(nombre)
                                .snippet("Estado: $estado")
                                .icon(BitmapDescriptorFactory.defaultMarker(colorMarcador))
                        )

                        marker?.tag = mapOf(
                            "reporteId" to reporteId,
                            "nombre" to nombre,
                            "estado" to estado,
                            "imagenesUrl" to imagenUrl
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("ActivityMapa", "Error al cargar reportes: ", exception)
            }
    }

    private fun cargarAvistamientosEnMapa() {
        db.collection("avistamientos")
            .get()
            .addOnSuccessListener { documentos ->
                for (documento in documentos) {
                    val lat = documento.getDouble("latitud")
                    val lng = documento.getDouble("longitud")
                    val tipo = documento.getString("tipoMascota") ?: "Avistamiento"
                    val desc = documento.getString("descripcion") ?: ""

                    if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {
                        val posicion = LatLng(lat, lng)

                        mMap.addMarker(
                            MarkerOptions()
                                .position(posicion)
                                .title("Pista: $tipo")
                                .snippet(desc)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                android.util.Log.e("ActivityMapa", "Error al cargar avistamientos: ", exception)
            }
    }

    private fun pedirPermisoUbicacion() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST
            )
        }
    }
    private fun marcarMenuActivo(seccion: String) {

        val inicio = findViewById<LinearLayout>(R.id.BtnInicio)
        val mapa = findViewById<LinearLayout>(R.id.BtnMapa)
        val alertas = findViewById<LinearLayout>(R.id.BtnAlertas)
        val historial = findViewById<LinearLayout>(R.id.BtnHistorial)
        val reporte = findViewById<LinearLayout>(R.id.btnReporte)

        // Colores
        val normalColor = getColor(android.R.color.transparent)
        val activoColor = ContextCompat.getColor(this, R.color.menu_activo)

        // Reset de todos los botones
        inicio.setBackgroundColor(normalColor)
        mapa.setBackgroundColor(normalColor)
        alertas.setBackgroundColor(normalColor)
        historial.setBackgroundColor(normalColor)
        reporte.setBackgroundColor(normalColor)

        // Activar el correcto
        when (seccion) {

            "inicio" -> {
                inicio.setBackgroundColor(activoColor)
            }

            "mapa" -> {
                mapa.setBackgroundColor(activoColor)
            }

            "alertas" -> {
                alertas.setBackgroundColor(activoColor)
            }

            "historial" -> {
                historial.setBackgroundColor(activoColor)
            }

            "reporte" -> {
                reporte.setBackgroundColor(activoColor)
            }
        }
    }
}
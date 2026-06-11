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

    // Constante para permiso de ubicación
    private val LOCATION_PERMISSION_REQUEST = 1

    // Firestore (base de datos principal del proyecto)
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_mapa)

        // Marca visual del menú activo
        marcarMenuActivo("mapa")

        // ================= NAVBAR =================
        // Navegación entre pantallas principales

        findViewById<LinearLayout>(R.id.BtnInicio).setOnClickListener {
            startActivity(Intent(this, ActivityInicio::class.java))
        }

        findViewById<LinearLayout>(R.id.BtnAlertas).setOnClickListener {
            startActivity(Intent(this, ActivityAlertas::class.java))
        }

        findViewById<LinearLayout>(R.id.BtnHistorial).setOnClickListener {
            startActivity(Intent(this, ActivityHistorial::class.java))
        }

        findViewById<LinearLayout>(R.id.btnReporte).setOnClickListener {
            startActivity(Intent(this, ActivityRegistroReporte::class.java))
        }

        // ================= MAPA =================

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapa) as SupportMapFragment

        mapFragment.getMapAsync(this)

        // Solicita permisos de ubicación al usuario
        pedirPermisoUbicacion()
    }

    override fun onResume() {
        super.onResume()

        // Recarga datos cada vez que regresa a la pantalla del mapa
        if (::mMap.isInitialized) {
            mMap.clear()
            cargarReportesEnMapa()
            cargarAvistamientosEnMapa()
        }
    }

    // ================= MAPA LISTO =================
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        // InfoWindow personalizada (cuando se toca un marcador)
        mMap.setInfoWindowAdapter(ReporteInfoWindowAdapter(this))

        // Permite abrir ventana de info al tocar marcador
        mMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        // Click en ventana de información del marcador
        mMap.setOnInfoWindowClickListener { marker ->

            val datos = marker.tag as? Map<*, *>
            val reporteId = datos?.get("reporteId") as? String

            if (reporteId != null) {

                val intent = Intent(
                    this,
                    Activity_registro_avistamiento::class.java
                )

                // Se envía información del reporte al registro de avistamiento
                intent.putExtra("reporteId", reporteId)
                intent.putExtra("nombreMascota", datos?.get("nombre") as? String ?: "")

                startActivity(intent)
            }
        }

        // ================= UBICACIÓN ACTUAL =================
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            mMap.isMyLocationEnabled = true

            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this)

            // Obtiene última ubicación del usuario
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->

                if (location != null) {

                    val ubicacionActual =
                        LatLng(location.latitude, location.longitude)

                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            ubicacionActual,
                            15f
                        )
                    )
                }
            }

            // Carga datos en el mapa
            cargarReportesEnMapa()
            cargarAvistamientosEnMapa()
        }

        // ================= FOTO DE PERFIL =================

        val btnPerfil = findViewById<ImageButton>(R.id.BtnPerfil)
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario != null) {

            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(usuario.uid)
                .get()
                .addOnSuccessListener { doc ->

                    val fotoPerfil = doc.getString("fotoPerfil")

                    if (!fotoPerfil.isNullOrEmpty()) {

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
        }
    }

    // ================= REPORTES EN MAPA =================
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

                    // Primera imagen del reporte
                    val imagenUrl =
                        (documento.get("imagenesUrl") as? List<*>)?.firstOrNull()?.toString() ?: ""

                    if (lat != null && lng != null && lat != 0.0 && lng != 0.0) {

                        val posicion = LatLng(lat, lng)

                        // Color del marcador según estado del reporte
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
                                .icon(
                                    BitmapDescriptorFactory.defaultMarker(colorMarcador)
                                )
                        )

                        // Se guarda información adicional en el marker
                        marker?.tag = mapOf(
                            "reporteId" to reporteId,
                            "nombre" to nombre,
                            "estado" to estado,
                            "imagenesUrl" to imagenUrl
                        )
                    }
                }
            }
    }

    // ================= AVISTAMIENTOS EN MAPA =================
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
                                .icon(
                                    BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_AZURE
                                    )
                                )
                        )
                    }
                }
            }
    }

    // ================= PERMISOS =================
    private fun pedirPermisoUbicacion() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    // ================= MENÚ ACTIVO =================
    private fun marcarMenuActivo(seccion: String) {

        val inicio = findViewById<LinearLayout>(R.id.BtnInicio)
        val mapa = findViewById<LinearLayout>(R.id.BtnMapa)
        val alertas = findViewById<LinearLayout>(R.id.BtnAlertas)
        val historial = findViewById<LinearLayout>(R.id.BtnHistorial)
        val reporte = findViewById<LinearLayout>(R.id.btnReporte)

        val normalColor = getColor(android.R.color.transparent)
        val activoColor = ContextCompat.getColor(this, R.color.menu_activo)

        // Reset de menú inferior
        inicio.setBackgroundColor(normalColor)
        mapa.setBackgroundColor(normalColor)
        alertas.setBackgroundColor(normalColor)
        historial.setBackgroundColor(normalColor)
        reporte.setBackgroundColor(normalColor)

        // Activar sección actual
        when (seccion) {
            "inicio" -> inicio.setBackgroundColor(activoColor)
            "mapa" -> mapa.setBackgroundColor(activoColor)
            "alertas" -> alertas.setBackgroundColor(activoColor)
            "historial" -> historial.setBackgroundColor(activoColor)
            "reporte" -> reporte.setBackgroundColor(activoColor)
        }
    }
}
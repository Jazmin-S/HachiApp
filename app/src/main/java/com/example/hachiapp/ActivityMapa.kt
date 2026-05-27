package com.example.hachiapp

// Librerías necesarias para permisos y el mapa
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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng

// La Activity implementa OnMapReadyCallback para saber cuando el mapa terminó de cargar
class ActivityMapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        // PERFIL (sin acción por ahora)
     //   val btnPerfil = findViewById<ImageButton>(R.id.BtnPerfil)

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
        // REPORTE
        val btnReporte = findViewById<LinearLayout>(R.id.btnReporte)
        btnReporte.setOnClickListener {
            startActivity(Intent(this, ActivityRegistroReporte::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }


        // MAPA
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapa) as SupportMapFragment

        mapFragment.getMapAsync(this)

        pedirPermisoUbicacion()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            mMap.isMyLocationEnabled = true
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
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
        }
    }

    // Función que verifica y pide permiso de GPS al usuario
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
}
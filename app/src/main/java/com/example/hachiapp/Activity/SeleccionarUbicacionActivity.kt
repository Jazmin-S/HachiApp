package com.example.hachiapp.Activity

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hachiapp.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class SeleccionarUbicacionActivity :
    AppCompatActivity(),
    OnMapReadyCallback,   // Se ejecuta cuando el mapa ya está listo
    GoogleMap.OnMyLocationButtonClickListener,  // Botón "mi ubicación"
    GoogleMap.OnMyLocationClickListener {       // Click en el punto azul del usuario

    private lateinit var mMap: GoogleMap

    // FAB para centrar ubicación actual
    private lateinit var fabMiUbicacion: FloatingActionButton

    // Coordenadas seleccionadas por el usuario
    private var latitud = 0.0
    private var longitud = 0.0

    // Dirección convertida con Geocoder (texto legible)
    private var direccion = ""

    // Código de permiso de ubicación
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_ubicacion)

        fabMiUbicacion = findViewById(R.id.fabMiUbicacion)

        // Fragment del mapa (Google Maps)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment)
                    as SupportMapFragment

        mapFragment.getMapAsync(this)

        // Botón confirmar: devuelve coordenadas a la Activity anterior
        findViewById<Button>(R.id.btnConfirmar).setOnClickListener {

            // Validación: evita enviar coordenadas vacías
            if (latitud == 0.0 && longitud == 0.0) {
                Toast.makeText(this, "Selecciona una ubicación en el mapa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Empaqueta datos para regresar a la pantalla anterior
            val result = Intent().apply {
                putExtra("latitud", latitud)
                putExtra("longitud", longitud)
                putExtra("direccion", direccion)
            }

            setResult(RESULT_OK, result)
            finish()
        }

        // Centra el mapa en ubicación actual
        fabMiUbicacion.setOnClickListener {
            irAUbicacionActual()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Activa capa de ubicación si hay permisos
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap.isMyLocationEnabled = true
                mMap.setOnMyLocationButtonClickListener(this)
                mMap.setOnMyLocationClickListener(this)
            } else {
                pedirPermisoUbicacion()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        // Coordenadas iniciales (si vienen de otra pantalla)
        var latInicial = intent.getDoubleExtra("latitud", 0.0)
        var lngInicial = intent.getDoubleExtra("longitud", 0.0)

        // Si no hay ubicación previa, intenta usar GPS
        if (latInicial == 0.0 && lngInicial == 0.0) {

            obtenerUltimaUbicacionConocida { location ->

                if (location != null) {

                    // Ubicación real del dispositivo
                    latInicial = location.latitude
                    lngInicial = location.longitude

                    latitud = latInicial
                    longitud = lngInicial

                    val punto = LatLng(latInicial, lngInicial)

                    mMap.addMarker(MarkerOptions().position(punto))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(punto, 15f))

                    obtenerDireccion(latInicial, lngInicial)

                } else {
                    // Fallback: CDMX si no hay GPS
                    val puntoPorDefecto = LatLng(19.4326, -99.1332)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoPorDefecto, 12f))

                    Toast.makeText(
                        this,
                        "Toca el mapa para seleccionar una ubicación",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        } else {
            // Si ya había ubicación guardada
            latitud = latInicial
            longitud = lngInicial

            val punto = LatLng(latInicial, lngInicial)

            mMap.addMarker(MarkerOptions().position(punto))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(punto, 15f))

            obtenerDireccion(latInicial, lngInicial)
        }

        // Selección manual en el mapa
        mMap.setOnMapClickListener { latLng ->

            latitud = latLng.latitude
            longitud = latLng.longitude

            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng))

            // Convierte coordenadas a dirección legible
            obtenerDireccion(latLng.latitude, latLng.longitude)
        }
    }

    // Centra el mapa en la ubicación actual del usuario
    private fun irAUbicacionActual() {

        try {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                pedirPermisoUbicacion()
                return
            }

            mMap.isMyLocationEnabled = true

            obtenerUltimaUbicacionConocida { location ->

                if (location != null) {

                    val miUbicacion = LatLng(location.latitude, location.longitude)

                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(miUbicacion, 17f)
                    )

                    // Actualiza selección actual
                    latitud = location.latitude
                    longitud = location.longitude

                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(miUbicacion))

                    obtenerDireccion(location.latitude, location.longitude)

                } else {
                    Toast.makeText(
                        this,
                        "No se pudo obtener tu ubicación actual",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // Obtiene última ubicación conocida del GPS
    private fun obtenerUltimaUbicacionConocida(callback: (Location?) -> Unit) {

        try {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                callback(null)
                return
            }

            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    callback(location)
                }
                .addOnFailureListener {
                    callback(null)
                }

        } catch (e: SecurityException) {
            callback(null)
        }
    }

    // Solicita permisos de ubicación al usuario
    private fun pedirPermisoUbicacion() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // Resultado de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

                try {
                    mMap.isMyLocationEnabled = true
                    irAUbicacionActual()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }

            } else {
                Toast.makeText(
                    this,
                    "Permiso de ubicación necesario para usar el mapa",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Botón nativo de ubicación (no lo sobrescribimos)
    override fun onMyLocationButtonClick(): Boolean = false

    // Click en punto azul de ubicación
    override fun onMyLocationClick(location: Location) {
        Toast.makeText(
            this,
            "Ubicación actual: ${location.latitude}, ${location.longitude}",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Convierte coordenadas a dirección (reverse geocoding)
    private fun obtenerDireccion(lat: Double, lng: Double) {

        try {
            val geocoder = Geocoder(this, Locale.getDefault())

            val direcciones = geocoder.getFromLocation(lat, lng, 1)

            direccion = if (!direcciones.isNullOrEmpty()) {
                direcciones[0].getAddressLine(0)
            } else {
                "Ubicación seleccionada"
            }

        } catch (e: Exception) {
            direccion = "Ubicación seleccionada"
            e.printStackTrace()
        }
    }
}
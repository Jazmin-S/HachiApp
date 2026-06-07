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
    OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fabMiUbicacion: FloatingActionButton

    private var latitud = 0.0
    private var longitud = 0.0
    private var direccion = ""

    // Constantes
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_ubicacion)

        fabMiUbicacion = findViewById(R.id.fabMiUbicacion)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment)
                    as SupportMapFragment

        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.btnConfirmar).setOnClickListener {
            if (latitud == 0.0 && longitud == 0.0) {
                Toast.makeText(this, "Selecciona una ubicación en el mapa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = Intent()
            result.putExtra("latitud", latitud)
            result.putExtra("longitud", longitud)
            result.putExtra("direccion", direccion)

            setResult(RESULT_OK, result)
            finish()
        }

        // Botón para ir a la ubicación actual
        fabMiUbicacion.setOnClickListener {
            irAUbicacionActual()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configurar botón de mi ubicación nativo de Google Maps
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

        // Obtener ubicación inicial desde el intent (viene del reporte)
        var latInicial = intent.getDoubleExtra("latitud", 0.0)
        var lngInicial = intent.getDoubleExtra("longitud", 0.0)

        // Si no hay ubicación guardada, intentar obtener ubicación actual
        if (latInicial == 0.0 && lngInicial == 0.0) {
            // Intentar obtener última ubicación conocida
            obtenerUltimaUbicacionConocida { location ->
                if (location != null) {
                    latInicial = location.latitude
                    lngInicial = location.longitude
                    latitud = latInicial
                    longitud = lngInicial
                    val punto = LatLng(latInicial, lngInicial)
                    mMap.addMarker(MarkerOptions().position(punto))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(punto, 15f))
                    obtenerDireccion(latInicial, lngInicial)
                } else {
                    // Ubicación por defecto (CDMX)
                    val puntoPorDefecto = LatLng(19.4326, -99.1332)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoPorDefecto, 12f))
                    Toast.makeText(this, "Toca el mapa para seleccionar una ubicación", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            // Ya había una ubicación guardada
            latitud = latInicial
            longitud = lngInicial
            val punto = LatLng(latInicial, lngInicial)
            mMap.addMarker(MarkerOptions().position(punto))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(punto, 15f))
            obtenerDireccion(latInicial, lngInicial)
        }

        mMap.setOnMapClickListener { latLng ->
            latitud = latLng.latitude
            longitud = latLng.longitude

            mMap.clear()
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
            )

            obtenerDireccion(latLng.latitude, latLng.longitude)
        }
    }

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

            // Obtener la última ubicación conocida y mover la cámara
            obtenerUltimaUbicacionConocida { location ->
                if (location != null) {
                    val miUbicacion = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 17f))

                    // También actualizar la ubicación seleccionada
                    latitud = location.latitude
                    longitud = location.longitude
                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(miUbicacion))
                    obtenerDireccion(location.latitude, location.longitude)
                } else {
                    Toast.makeText(this, "No se pudo obtener tu ubicación actual", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, "Error al acceder a la ubicación", Toast.LENGTH_SHORT).show()
        }
    }

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

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                callback(location)
            }.addOnFailureListener {
                callback(null)
            }
        } catch (e: SecurityException) {
            callback(null)
        }
    }

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido, habilitar ubicación en el mapa
                    try {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            mMap.isMyLocationEnabled = true
                        }
                        irAUbicacionActual()
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this, "Permiso de ubicación necesario para usar el mapa", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        // El botón nativo de ubicación ya maneja esto
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Ubicación actual: ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
    }

    private fun obtenerDireccion(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val direcciones = geocoder.getFromLocation(lat, lng, 1)

            direccion = if (direcciones != null && direcciones.isNotEmpty()) {
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
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
    OnMapReadyCallback,   // Callback cuando el mapa está listo
    GoogleMap.OnMyLocationButtonClickListener,  // Click en botón de ubicación nativo
    GoogleMap.OnMyLocationClickListener {// Click en el punto de ubicación

    private lateinit var mMap: GoogleMap
    private lateinit var fabMiUbicacion: FloatingActionButton

    private var latitud = 0.0
    private var longitud = 0.0
    private var direccion = ""

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001   // Código para solicitar permiso

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_ubicacion)

        fabMiUbicacion = findViewById(R.id.fabMiUbicacion)

        // Obtiene el fragmento del mapa
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment)
                    as SupportMapFragment

        mapFragment.getMapAsync(this)  // Inicia carga asíncrona del mapa

        // Botón confirmar: valida y devuelve ubicación al activity anterior
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

        // Habilita el botón de mi ubicación si hay permiso
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

        // Obtiene ubicación inicial desde el intent
        var latInicial = intent.getDoubleExtra("latitud", 0.0)
        var lngInicial = intent.getDoubleExtra("longitud", 0.0)

        // Si no hay ubicación guardada, intenta obtener ubicación actual
        if (latInicial == 0.0 && lngInicial == 0.0) {
            obtenerUltimaUbicacionConocida { location ->
                if (location != null) {
                    // Usa ubicación real del dispositivo
                    latInicial = location.latitude
                    lngInicial = location.longitude
                    latitud = latInicial
                    longitud = lngInicial
                    val punto = LatLng(latInicial, lngInicial)
                    mMap.addMarker(MarkerOptions().position(punto))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(punto, 15f))
                    obtenerDireccion(latInicial, lngInicial)
                } else {
                    // Ubicación por defecto: CDMX
                    val puntoPorDefecto = LatLng(19.4326, -99.1332)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(puntoPorDefecto, 12f))
                    Toast.makeText(this, "Toca el mapa para seleccionar una ubicación", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            // Ya había ubicación guardada, la muestra en el mapa
            latitud = latInicial
            longitud = lngInicial
            val punto = LatLng(latInicial, lngInicial)
            mMap.addMarker(MarkerOptions().position(punto))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(punto, 15f))
            obtenerDireccion(latInicial, lngInicial)
        }

        // Escucha clicks en el mapa para seleccionar ubicación
        mMap.setOnMapClickListener { latLng ->
            latitud = latLng.latitude
            longitud = latLng.longitude

            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng))

            obtenerDireccion(latLng.latitude, latLng.longitude)
        }
    }

    // Navega la cámara a la ubicación actual del dispositivo
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
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 17f))

                    // Actualiza la ubicación seleccionada también
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

    // Obtiene la última ubicación conocida del dispositivo
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

    // Solicita permiso de ubicación al usuario
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

    // Resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso concedido
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

    // Click en botón de ubicación nativo (false = permite comportamiento por defecto)
    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    // Click sobre el ícono de mi ubicación en el mapa
    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Ubicación actual: ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
    }

    // Convierte coordenadas a dirección legible usando Geocoder
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
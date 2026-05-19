package com.example.hachiapp

// Librerías necesarias para permisos y el mapa
import android.Manifest                                    // Para usar los nombres de permisos
import android.annotation.SuppressLint
import android.content.pm.PackageManager                  // Para verificar si el permiso fue otorgado
import android.os.Bundle                                   // Para manejar el estado de la Activity
import androidx.appcompat.app.AppCompatActivity            // Clase base de la Activity
import androidx.core.app.ActivityCompat                    // Para pedir permisos al usuario
import androidx.core.content.ContextCompat                 // Para verificar permisos
import com.google.android.gms.maps.GoogleMap              // La clase principal del mapa
import com.google.android.gms.maps.OnMapReadyCallback     // Interfaz que avisa cuando el mapa está listo
import com.google.android.gms.maps.SupportMapFragment     // El fragmento que muestra el mapa en pantalla
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng


// La Activity implementa OnMapReadyCallback para saber cuando el mapa terminó de cargar
class ActivityMapa : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap   // Variable que guardará el mapa cuando esté listo
    private val LOCATION_PERMISSION_REQUEST = 1  // Código identificador de la solicitud de permiso

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)  // Conecta esta Activity con su XML
        // Busca el fragmento del mapa en el XML por su ID
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapa) as SupportMapFragment
        // Le dice al fragmento que avise cuando el mapa esté listo
        // Cuando esté listo llama automáticamente a onMapReady()
        mapFragment.getMapAsync(this)
        // Llama a la función que pide permiso de ubicación al usuario
        pedirPermisoUbicacion()
    }

    @SuppressLint("MissingPermission")
    // Esta función se ejecuta automáticamente cuando el mapa terminó de cargar
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap  // Guarda el mapa en la variable para usarlo después

        // Si tiene permiso muestra el punto azul de ubicación actual en el mapa
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.isMyLocationEnabled = true
            // El mapa automáticamente se centrará en tu ubicación real
            // Obtener la ubicación actual y centrar el mapa
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val ubicacionActual = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 15f))
                }
            }
        }
    }

    // Función que verifica y pide permiso de GPS al usuario
    private fun pedirPermisoUbicacion() {

        // Verifica si el permiso de ubicación NO ha sido otorgado todavía
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Si no tiene permiso, muestra el popup al usuario pidiendo acceso al GPS
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), // Permiso que se pide
                LOCATION_PERMISSION_REQUEST)  // Código para identificar esta solicitud
        }
    }
}
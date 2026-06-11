package com.example.hachiapp.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.hachiapp.Activity.ActivityRegistroReporte
import com.example.hachiapp.BD.ReporteRepository
import com.example.hachiapp.R
import com.example.hachiapp.adapters.HistorialReportesAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityHistorial : AppCompatActivity() {

    // Instancias del repositorio y el adaptador
    private val reporteRepository = ReporteRepository()
    private lateinit var historialAdapter: HistorialReportesAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)
        marcarMenuActivo("historial")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ── 1. CONFIGURACIÓN DEL RECYCLERVIEW ─────────────────────────────────
        recyclerView = findViewById(R.id.recyclerViewHistorial)
        recyclerView.layoutManager = LinearLayoutManager(this)

        historialAdapter = HistorialReportesAdapter()
        recyclerView.adapter = historialAdapter

        // ── 2. CARGAR LOS REPORTES DESDE FIREBASE ─────────────────────────────
        cargarHistorial()


        //val btnPerfil = findViewById<ImageButton>(R.id.BtnPerfil)

        // MAPA
        val btnMapa = findViewById<LinearLayout>(R.id.BtnMapa)
        btnMapa.setOnClickListener {
            startActivity(Intent(this, ActivityMapa::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        // ALERTAS
        val btnAlertas = findViewById<LinearLayout>(R.id.BtnAlertas)
        btnAlertas.setOnClickListener {
            startActivity(Intent(this, ActivityAlertas::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Inicio
        val btnInicio = findViewById<LinearLayout>(R.id.BtnInicio)
        btnInicio.setOnClickListener {
            startActivity(Intent(this, ActivityInicio::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // REPORTE
        val btnReporte = findViewById<LinearLayout>(R.id.BtnReporte)
        btnReporte.setOnClickListener {
            startActivity(Intent(this, ActivityRegistroReporte::class.java))
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
        // Métod encargado de consultar al repositorio
        private fun cargarHistorial() {
            reporteRepository.obtenerReportesUsuario(
                onSuccess = { listaReportes ->
                    if (listaReportes.isEmpty()) {
                        Toast.makeText(this, "No tienes reportes realizados", Toast.LENGTH_SHORT).show()
                    } else {
                        // Pasamos la lista de reportes reales al adaptador
                        historialAdapter.actualizarLista(listaReportes)
                    }
                },
                onError = { excepcion ->
                    Toast.makeText(this, "Error al cargar historial: ${excepcion.message}", Toast.LENGTH_LONG).show()

                }
            )
        }
    private fun marcarMenuActivo(seccion: String) {

        val inicio = findViewById<LinearLayout>(R.id.BtnInicio)
        val mapa = findViewById<LinearLayout>(R.id.BtnMapa)
        val alertas = findViewById<LinearLayout>(R.id.BtnAlertas)
        val historial = findViewById<LinearLayout>(R.id.BtnHistorial)
        val reporte = findViewById<LinearLayout>(R.id.BtnReporte)

        // Colores
        val normalColor = getColor(android.R.color.transparent)
        val activoColor = getColor(R.color.menu_activo)

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
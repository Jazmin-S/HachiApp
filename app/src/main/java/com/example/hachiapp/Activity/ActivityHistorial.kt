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

    // Repositorio encargado de traer los reportes desde Firestore
    private val reporteRepository = ReporteRepository()

    // Adaptador del RecyclerView (historial de reportes del usuario)
    private lateinit var historialAdapter: HistorialReportesAdapter

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_historial)

        // Marca visualmente el botón activo del menú inferior
        marcarMenuActivo("historial")

        // Ajuste de padding para evitar superposición con system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ================= RECYCLER VIEW =================

        recyclerView = findViewById(R.id.recyclerViewHistorial)

        // Lista vertical de reportes
        recyclerView.layoutManager = LinearLayoutManager(this)

        historialAdapter = HistorialReportesAdapter()
        recyclerView.adapter = historialAdapter

        // Carga inicial de datos desde Firebase
        cargarHistorial()

        // ================= NAVBAR =================
        // Navegación entre pantallas principales

        findViewById<LinearLayout>(R.id.BtnMapa).setOnClickListener {
            startActivity(Intent(this, ActivityMapa::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<LinearLayout>(R.id.BtnAlertas).setOnClickListener {
            startActivity(Intent(this, ActivityAlertas::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<LinearLayout>(R.id.BtnInicio).setOnClickListener {
            startActivity(Intent(this, ActivityInicio::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<LinearLayout>(R.id.BtnReporte).setOnClickListener {
            startActivity(Intent(this, ActivityRegistroReporte::class.java))
        }

        // ================= FOTO DE PERFIL =================

        val btnPerfil = findViewById<ImageButton>(R.id.BtnPerfil)
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario != null) {

            // Obtiene foto de perfil desde Firestore
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(usuario.uid)
                .get()
                .addOnSuccessListener { doc ->

                    val fotoPerfil = doc.getString("fotoPerfil")

                    // Validación para evitar URLs inválidas
                    if (!fotoPerfil.isNullOrEmpty() && fotoPerfil.startsWith("http")) {

                        // Carga imagen circular con Glide
                        Glide.with(this)
                            .load(fotoPerfil)
                            .transform(CircleCrop())
                            .placeholder(R.drawable.perfil)
                            .error(R.drawable.perfil)
                            .into(btnPerfil)
                    }
                }
        }

        // Acceso a perfil del usuario
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ActivityPerfil::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    // ================= CARGA DEL HISTORIAL =================
    private fun cargarHistorial() {

        // Consulta al repositorio (encapsula lógica de Firestore)
        reporteRepository.obtenerReportesUsuario(
            onSuccess = { listaReportes ->

                if (listaReportes.isEmpty()) {
                    Toast.makeText(this, "No tienes reportes realizados", Toast.LENGTH_SHORT).show()
                } else {
                    // Actualiza RecyclerView con datos reales del usuario
                    historialAdapter.actualizarLista(listaReportes)
                }
            },

            onError = { excepcion ->

                // Manejo de error al consultar Firestore
                Toast.makeText(
                    this,
                    "Error al cargar historial: ${excepcion.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    // ================= MENÚ ACTIVO =================
    private fun marcarMenuActivo(seccion: String) {

        val inicio = findViewById<LinearLayout>(R.id.BtnInicio)
        val mapa = findViewById<LinearLayout>(R.id.BtnMapa)
        val alertas = findViewById<LinearLayout>(R.id.BtnAlertas)
        val historial = findViewById<LinearLayout>(R.id.BtnHistorial)
        val reporte = findViewById<LinearLayout>(R.id.BtnReporte)

        val normalColor = getColor(android.R.color.transparent)
        val activoColor = getColor(R.color.menu_activo)

        // Reset de todos los estados del menú inferior
        inicio.setBackgroundColor(normalColor)
        mapa.setBackgroundColor(normalColor)
        alertas.setBackgroundColor(normalColor)
        historial.setBackgroundColor(normalColor)
        reporte.setBackgroundColor(normalColor)

        // Activa solo la sección actual
        when (seccion) {
            "inicio" -> inicio.setBackgroundColor(activoColor)
            "mapa" -> mapa.setBackgroundColor(activoColor)
            "alertas" -> alertas.setBackgroundColor(activoColor)
            "historial" -> historial.setBackgroundColor(activoColor)
            "reporte" -> reporte.setBackgroundColor(activoColor)
        }
    }
}
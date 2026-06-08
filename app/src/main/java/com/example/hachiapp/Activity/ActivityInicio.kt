package com.example.hachiapp.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.hachiapp.R
import com.example.hachiapp.models.Reporte
import androidx.recyclerview.widget.RecyclerView
import com.example.hachiapp.adapters.ReporteAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore

class ActivityInicio : AppCompatActivity() {

    // Lista maestra con TODOS los reportes traídos de Firestore
    private val listaReportesCompleta = mutableListOf<Reporte>()

    // Lista que se muestra en el RecyclerView (filtrada/buscada)
    private val listaReportesFiltrada = mutableListOf<Reporte>()

    private lateinit var adapter: ReporteAdapter

    // Estado actual del filtro: null = todos
    private var filtroEstado: String? = null

    // Texto de búsqueda actual
    private var textoBusqueda: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)

        // ── RecyclerView ──────────────────────────────────────────────
        val recycler = findViewById<RecyclerView>(R.id.recyclerReportes)
        recycler.layoutManager = GridLayoutManager(this, 2)

        adapter = ReporteAdapter(listaReportesFiltrada) { reporte ->
            val intent = Intent(this, ActivityVolante::class.java).apply {
                putExtra("nombreMascota",  reporte.nombreMascota)
                putExtra("razaMascota",    reporte.razaMascota)
                putExtra("edadMascota",    reporte.edadMascota)
                putExtra("tamano",         reporte.tamano)
                putExtra("colorMascota",   reporte.colorMascota)
                putExtra("descripcion",    reporte.descripcion)
                putExtra("fechaExtravio",  reporte.fechaExtravio)
                putExtra("estadoMascota",  reporte.estadoMascota)
                putExtra("latitud",        reporte.latitud)
                putExtra("longitud",       reporte.longitud)
                putExtra("usuarioId",      reporte.usuarioId)
                putExtra("recompensa",     reporte.recompensa)
                putExtra("direccion",      reporte.direccion)
                if (reporte.imagenesUrl.isNotEmpty())
                    putExtra("imagenUrl", reporte.imagenesUrl[0])
                putExtra("reporteId", reporte.id)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        recycler.adapter = adapter

        // ── Carga Firestore ───────────────────────────────────────────
        FirebaseFirestore.getInstance()
            .collection("reportes")
            .get()
            .addOnSuccessListener { resultado ->
                listaReportesCompleta.clear()
                for (documento in resultado) {
                    val reporte = documento.toObject(Reporte::class.java)
                        .copy(id = documento.id)
                    listaReportesCompleta.add(reporte)
                }
                aplicarFiltros() // muestra todos al arrancar
            }

        // ── Búsqueda ──────────────────────────────────────────────────
        // Contenedor LYBusqueda ya existe en el XML; aquí solo tomamos el EditText
        val txtBuscar = findViewById<EditText>(R.id.txtBuscar)
        txtBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textoBusqueda = s?.toString()?.trim() ?: ""
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        val btnClasificar = findViewById<ImageButton>(R.id.btnClasificar)

        btnClasificar.setOnClickListener { vista ->
            val popup = PopupMenu(this, vista)
            popup.menuInflater.inflate(R.menu.menu_clasificar, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                filtroEstado = when (item.itemId) {
                    R.id.opPerdido    -> "perdido"
                    R.id.opVisto      -> "visto"
                    R.id.opEncontrado -> "encontrado"
                    else              -> null // "Todos"
                }
                aplicarFiltros()
                true
            }
            popup.show()
        }

        // ── Navegación inferior ───────────────────────────────────────
        val btnPerfil = findViewById<ImageButton>(R.id.BtnPerfil)
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ActivityPerfil::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        val btnMapa = findViewById<LinearLayout>(R.id.BtnMapa)
        btnMapa.setOnClickListener {
            startActivity(Intent(this, ActivityMapa::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        val btnAlertas = findViewById<LinearLayout>(R.id.BtnAlertas)
        btnAlertas.setOnClickListener {
            startActivity(Intent(this, ActivityAlertas::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        val btnHistorial = findViewById<LinearLayout>(R.id.BtnHistorial)
        btnHistorial.setOnClickListener {
            startActivity(Intent(this, ActivityHistorial::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        val btnReporte = findViewById<LinearLayout>(R.id.BtnReporte)
        btnReporte.setOnClickListener {
            startActivity(Intent(this, ActivityRegistroReporte::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    // ─────────────────────────────────────────────────────────────────
    //  Aplica AMBOS filtros (estado + texto) sobre la lista maestra
    // ─────────────────────────────────────────────────────────────────
    private fun aplicarFiltros() {
        val query = textoBusqueda.lowercase()

        val resultado = listaReportesCompleta.filter { reporte ->

            // 1) Filtro por estado
            val coincideEstado = filtroEstado == null ||
                    reporte.estadoMascota.lowercase() == filtroEstado

            // 2) Filtro por texto: busca en nombre, raza, color, descripción, fecha y dirección
            val coincideTexto = query.isEmpty() ||
                    reporte.nombreMascota.lowercase().contains(query)   ||
                    reporte.razaMascota.lowercase().contains(query)     ||
                    reporte.colorMascota.lowercase().contains(query)    ||
                    reporte.descripcion.lowercase().contains(query)     ||
                    reporte.fechaExtravio.lowercase().contains(query)   ||
                    reporte.direccion.lowercase().contains(query)

            coincideEstado && coincideTexto
        }

        listaReportesFiltrada.clear()
        listaReportesFiltrada.addAll(resultado)
        adapter.notifyDataSetChanged()
    }
}
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.hachiapp.R
import com.example.hachiapp.models.Reporte
import androidx.recyclerview.widget.RecyclerView
import com.example.hachiapp.adapters.ReporteAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityInicio : AppCompatActivity() {

    // Lista base con todos los reportes desde Firestore
    private val listaReportesCompleta = mutableListOf<Reporte>()

    // Lista filtrada que se muestra en pantalla
    private val listaReportesFiltrada = mutableListOf<Reporte>()

    private lateinit var adapter: ReporteAdapter

    // Filtros activos
    private var filtroEstado: String? = null
    private var textoBusqueda: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_inicio)

        // Marca visualmente el botón activo del menú inferior
        marcarMenuActivo("inicio")

        // ================= RECYCLER =================

        val recycler = findViewById<RecyclerView>(R.id.recyclerReportes)

        // Grid de 2 columnas para mostrar reportes tipo cards
        recycler.layoutManager = GridLayoutManager(this, 2)

        // Adaptador con click → abre detalle del reporte
        adapter = ReporteAdapter(listaReportesFiltrada) { reporte ->

            val intent = Intent(this, ActivityVolante::class.java).apply {

                // Se envían todos los datos necesarios para la pantalla de detalle
                putExtra("nombreMascota", reporte.nombreMascota)
                putExtra("razaMascota", reporte.razaMascota)
                putExtra("edadMascota", reporte.edadMascota)
                putExtra("tamano", reporte.tamano)
                putExtra("colorMascota", reporte.colorMascota)
                putExtra("descripcion", reporte.descripcion)
                putExtra("fechaExtravio", reporte.fechaExtravio)
                putExtra("estadoMascota", reporte.estadoMascota)
                putExtra("latitud", reporte.latitud)
                putExtra("longitud", reporte.longitud)
                putExtra("usuarioId", reporte.usuarioId)
                putExtra("recompensa", reporte.recompensa)
                putExtra("direccion", reporte.direccion)

                // Imagen principal del reporte (si existe)
                if (reporte.imagenesUrl.isNotEmpty())
                    putExtra("imagenUrl", reporte.imagenesUrl[0])

                putExtra("reporteId", reporte.id)
            }

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        recycler.adapter = adapter

        // ================= FIRESTORE (TIEMPO REAL) =================

        FirebaseFirestore.getInstance()
            .collection("reportes")
            .addSnapshotListener { resultado, error ->

                if (error != null) return@addSnapshotListener

                listaReportesCompleta.clear()

                // Convierte cada documento en objeto Reporte
                resultado?.forEach { documento ->

                    val reporte = documento.toObject(Reporte::class.java)
                        .copy(id = documento.id)

                    listaReportesCompleta.add(reporte)
                }

                // Aplica filtros cada vez que cambian datos en Firestore
                aplicarFiltros()
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

        // Acceso a perfil
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ActivityPerfil::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // ================= BÚSQUEDA EN TIEMPO REAL =================

        val txtBuscar = findViewById<EditText>(R.id.txtBuscar)

        txtBuscar.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textoBusqueda = s?.toString()?.trim() ?: ""
                aplicarFiltros()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        // ================= FILTRO POR ESTADO =================

        val btnClasificar = findViewById<ImageButton>(R.id.btnClasificar)

        btnClasificar.setOnClickListener { vista ->

            val popup = PopupMenu(this, vista)
            popup.menuInflater.inflate(R.menu.menu_clasificar, popup.menu)

            popup.setOnMenuItemClickListener { item ->

                // Cambia filtro según opción seleccionada
                filtroEstado = when (item.itemId) {
                    R.id.opPerdido -> "perdido"
                    R.id.opVisto -> "visto"
                    R.id.opEncontrado -> "encontrado"
                    else -> null
                }

                aplicarFiltros()
                true
            }

            popup.show()
        }

        // ================= NAVBAR =================

        findViewById<LinearLayout>(R.id.BtnMapa).setOnClickListener {
            startActivity(Intent(this, ActivityMapa::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<LinearLayout>(R.id.BtnAlertas).setOnClickListener {
            startActivity(Intent(this, ActivityAlertas::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<LinearLayout>(R.id.BtnHistorial).setOnClickListener {
            startActivity(Intent(this, ActivityHistorial::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<LinearLayout>(R.id.BtnReporte).setOnClickListener {
            startActivity(Intent(this, ActivityRegistroReporte::class.java))
        }
    }

    // ================= FILTROS COMBINADOS =================
    private fun aplicarFiltros() {

        val query = textoBusqueda.lowercase()

        val resultado = listaReportesCompleta.filter { reporte ->

            // Filtro por estado
            val coincideEstado = filtroEstado == null ||
                    reporte.estadoMascota.lowercase() == filtroEstado

            // Filtro por búsqueda global en varios campos
            val coincideTexto = query.isEmpty() ||
                    reporte.nombreMascota.lowercase().contains(query) ||
                    reporte.razaMascota.lowercase().contains(query) ||
                    reporte.colorMascota.lowercase().contains(query) ||
                    reporte.fechaExtravio.lowercase().contains(query) ||
                    reporte.direccion.lowercase().contains(query) ||
                    reporte.tamano.lowercase().contains(query) ||
                    reporte.descripcion.lowercase().contains(query) ||
                    reporte.estadoMascota.lowercase().contains(query)

            coincideEstado && coincideTexto
        }

        // Actualiza lista visible
        listaReportesFiltrada.clear()
        listaReportesFiltrada.addAll(resultado)

        adapter.notifyDataSetChanged()
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

        // Reset de todos los botones
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
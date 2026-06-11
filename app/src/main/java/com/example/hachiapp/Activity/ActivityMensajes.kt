package com.example.hachiapp.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.hachiapp.R
import com.example.hachiapp.adapters.ConversacionAdapter
import com.example.hachiapp.models.Conversacion
import com.example.hachiapp.models.Mensaje
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityMensajes : AppCompatActivity() {

    // Firestore principal para mensajes de chat
    private lateinit var firestore: FirebaseFirestore

    private lateinit var recyclerChats: RecyclerView
    private lateinit var adapter: ConversacionAdapter

    // Lista final de conversaciones agrupadas (no mensajes individuales)
    private val listaConversaciones = mutableListOf<Conversacion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_mensajes)

        // Marca visual del menú (aquí se usa "alertas" porque chat está dentro de esa sección)
        marcarMenuActivo("alertas")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        // ================= RECYCLER DE CONVERSACIONES =================

        recyclerChats = findViewById(R.id.recyclerChats)

        adapter = ConversacionAdapter(listaConversaciones) { conversacion ->

            // Click en una conversación → abre chat individual
            val intent = Intent(this, ActivityChat::class.java)

            intent.putExtra("reporteId", conversacion.reporteId)
            intent.putExtra("receptorId", conversacion.otroUsuarioId)
            intent.putExtra("nombreMascota", conversacion.nombreMascota)

            startActivity(intent)
        }

        recyclerChats.layoutManager = LinearLayoutManager(this)
        recyclerChats.adapter = adapter

        cargarConversaciones()

        // ================= NAVBAR =================

        findViewById<LinearLayout>(R.id.BtnInicio).setOnClickListener {
            startActivity(Intent(this, ActivityInicio::class.java))
        }

        findViewById<LinearLayout>(R.id.BtnMapa).setOnClickListener {
            startActivity(Intent(this, ActivityMapa::class.java))
        }

        findViewById<LinearLayout>(R.id.BtnAlertas).setOnClickListener {
            startActivity(Intent(this, ActivityAlertas::class.java))
        }

        findViewById<LinearLayout>(R.id.BtnHistorial).setOnClickListener {
            startActivity(Intent(this, ActivityHistorial::class.java))
        }

        findViewById<LinearLayout>(R.id.BtnReporte).setOnClickListener {
            startActivity(Intent(this, ActivityRegistro::class.java))
        }

        // ================= FOTO DE PERFIL =================

        val btnPerfil = findViewById<ImageButton>(R.id.BtnPerfil)
        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario != null) {

            // Obtiene imagen de perfil desde Firestore
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(usuario.uid)
                .get()
                .addOnSuccessListener { documento ->

                    val fotoPerfil = documento.getString("fotoPerfil")

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

    // ================= AGRUPACIÓN DE CONVERSACIONES =================
    private fun cargarConversaciones() {

        val uidActual =
            FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("mensajes")
            .orderBy("fecha")
            .addSnapshotListener { snapshots, error ->

                if (error != null || snapshots == null) return@addSnapshotListener

                // Mapa para agrupar mensajes por conversación (reporte + usuario)
                val conversacionesMap = mutableMapOf<String, Conversacion>()

                for (doc in snapshots.documents) {

                    val mensaje =
                        doc.toObject(Mensaje::class.java) ?: continue

                    // Filtra solo mensajes del usuario actual
                    if (
                        mensaje.remitenteId != uidActual &&
                        mensaje.destinatarioId != uidActual
                    ) continue

                    // Determina el otro usuario del chat
                    val otroUsuario =
                        if (mensaje.remitenteId == uidActual)
                            mensaje.destinatarioId
                        else
                            mensaje.remitenteId

                    // Clave única por conversación (reporte + usuario)
                    val clave = "${mensaje.reporteId}_$otroUsuario"

                    // Se guarda el último mensaje de cada conversación
                    conversacionesMap[clave] = Conversacion(
                        reporteId = mensaje.reporteId,
                        otroUsuarioId = otroUsuario,
                        nombreMascota = mensaje.nombreMascota,
                        ultimoMensaje = mensaje.mensaje,
                        fecha = mensaje.fecha?.toDate()?.time ?: 0L,
                        ultimoRemitenteId = mensaje.remitenteId
                    )
                }

                // Actualiza lista final ordenada por fecha (más reciente primero)
                listaConversaciones.clear()
                listaConversaciones.addAll(
                    conversacionesMap.values.sortedByDescending { it.fecha }
                )

                adapter.notifyDataSetChanged()
            }
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

        inicio.setBackgroundColor(normalColor)
        mapa.setBackgroundColor(normalColor)
        alertas.setBackgroundColor(normalColor)
        historial.setBackgroundColor(normalColor)
        reporte.setBackgroundColor(normalColor)

        when (seccion) {
            "inicio" -> inicio.setBackgroundColor(activoColor)
            "mapa" -> mapa.setBackgroundColor(activoColor)
            "alertas" -> alertas.setBackgroundColor(activoColor)
            "historial" -> historial.setBackgroundColor(activoColor)
            "reporte" -> reporte.setBackgroundColor(activoColor)
        }
    }
}
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

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerChats: RecyclerView
    private lateinit var adapter: ConversacionAdapter

    private val listaConversaciones = mutableListOf<Conversacion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_mensajes)
        marcarMenuActivo("alertas")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        recyclerChats = findViewById(R.id.recyclerChats)

        adapter = ConversacionAdapter(listaConversaciones) { conversacion ->

            val intent = Intent(
                this,
                ActivityChat::class.java
            )

            intent.putExtra(
                "reporteId",
                conversacion.reporteId
            )

            intent.putExtra(
                "receptorId",
                conversacion.otroUsuarioId
            )
            intent.putExtra(
                "nombreMascota",
                conversacion.nombreMascota
            )

            startActivity(intent)
        }

        recyclerChats.layoutManager =
            LinearLayoutManager(this)

        recyclerChats.adapter = adapter

        cargarConversaciones()

        // INICIO
        findViewById<LinearLayout>(R.id.BtnInicio)
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        ActivityInicio::class.java
                    )
                )
                overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }

        // MAPA
        findViewById<LinearLayout>(R.id.BtnMapa)
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        ActivityMapa::class.java
                    )
                )
                overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }

        // ALERTAS
        findViewById<LinearLayout>(R.id.BtnAlertas)
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        ActivityAlertas::class.java
                    )
                )
                overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }

        // HISTORIAL
        findViewById<LinearLayout>(R.id.BtnHistorial)
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        ActivityHistorial::class.java
                    )
                )
                overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }

        // REPORTE
        findViewById<LinearLayout>(R.id.BtnReporte)
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        ActivityRegistro::class.java
                    )
                )
                overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }

        // NOTIFICACIONES
        findViewById<TextView>(R.id.btnNotificacion)
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        ActivityAlertas::class.java
                    )
                )
                overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
        findViewById<ImageButton>(R.id.BtnPerfil)
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        ActivityPerfil::class.java
                    )
                )
            }
        // ================= FOTO DE PERFIL =================

        val btnPerfil = findViewById<ImageButton>(R.id.BtnPerfil)

        btnPerfil.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ActivityPerfil::class.java
                )
            )
        }

        val usuario = FirebaseAuth.getInstance().currentUser

        if (usuario != null) {

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
    }

    private fun cargarConversaciones() {

        val uidActual =
            FirebaseAuth.getInstance()
                .currentUser?.uid ?: return

        firestore.collection("mensajes")
            .orderBy("fecha")
            .addSnapshotListener { snapshots, error ->

                if (error != null || snapshots == null) {
                    return@addSnapshotListener
                }

                val conversacionesMap =
                    mutableMapOf<String, Conversacion>()

                for (doc in snapshots.documents) {

                    val mensaje =
                        doc.toObject(Mensaje::class.java)
                            ?: continue

                    if (
                        mensaje.remitenteId != uidActual &&
                        mensaje.destinatarioId != uidActual
                    ) {
                        continue
                    }

                    val otroUsuario =
                        if (mensaje.remitenteId == uidActual)
                            mensaje.destinatarioId
                        else
                            mensaje.remitenteId

                    val clave =
                        "${mensaje.reporteId}_$otroUsuario"

                    conversacionesMap[clave] =
                        Conversacion(
                            reporteId = mensaje.reporteId,
                            otroUsuarioId = otroUsuario,
                            nombreMascota = mensaje.nombreMascota,
                            ultimoMensaje = mensaje.mensaje,
                            fecha = mensaje.fecha?.toDate()?.time ?: 0L,
                            ultimoRemitenteId = mensaje.remitenteId
                        )
                }

                listaConversaciones.clear()

                listaConversaciones.addAll(
                    conversacionesMap.values.sortedByDescending {
                        it.fecha
                    }
                )

                adapter.notifyDataSetChanged()
            }
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
package com.example.hachiapp.Activity

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hachiapp.R
import com.example.hachiapp.adapters.MensajeAdapter
import com.example.hachiapp.models.Mensaje
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActivityChat : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerMensajes: RecyclerView
    private lateinit var adapter: MensajeAdapter

    private val listaMensajes = mutableListOf<Mensaje>()

    private var receptorId = ""
    private var reporteId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        firestore = FirebaseFirestore.getInstance()

        receptorId = intent.getStringExtra("receptorId") ?: ""
        reporteId = intent.getStringExtra("reporteId") ?: ""

        val nombreMascota = intent.getStringExtra("nombreMascota") ?: ""

        // RecyclerView
        recyclerMensajes = findViewById(R.id.recyclerMensajes)

        adapter = MensajeAdapter(listaMensajes)

        recyclerMensajes.layoutManager = LinearLayoutManager(this)
        recyclerMensajes.adapter = adapter

        // Nombre del chat
        findViewById<TextView>(R.id.tvNombreContacto).text =
            "Dueño de $nombreMascota"

        // Botón volver
        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener {
            finish()
        }

        // Crear mensaje inicial si no existe ninguno
        verificarChatVacio()

        // Escuchar mensajes
        cargarMensajes()

        val etMensaje = findViewById<EditText>(R.id.etMensaje)

        findViewById<CardView>(R.id.btnEnviar).setOnClickListener {

            val texto = etMensaje.text.toString().trim()

            if (texto.isEmpty()) {
                Toast.makeText(
                    this,
                    "Escribe un mensaje",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val remitenteId =
                FirebaseAuth.getInstance().currentUser?.uid

            if (remitenteId == null) {
                Toast.makeText(
                    this,
                    "Usuario no autenticado",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val mensaje = hashMapOf(
                "remitenteId" to remitenteId,
                "destinatarioId" to receptorId,
                "reporteId" to reporteId,
                "mensaje" to texto,
                "fecha" to Timestamp.now()
            )

            firestore.collection("mensajes")
                .add(mensaje)
                .addOnSuccessListener {

                    etMensaje.setText("")

                    Toast.makeText(
                        this,
                        "Mensaje enviado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        "Error al enviar mensaje",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun cargarMensajes() {

        Log.d("CHAT_FIREBASE", "Reporte ID: $reporteId")

        firestore.collection("mensajes")
            .whereEqualTo("reporteId", reporteId)
            .orderBy("fecha")
            .addSnapshotListener { snapshots, e ->

                if (e != null) {
                    Log.e("CHAT_FIREBASE", "ERROR FIRESTORE", e)
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    Log.e("CHAT_FIREBASE", "Snapshots NULL")
                    return@addSnapshotListener
                }

                Log.d(
                    "CHAT_FIREBASE",
                    "Documentos encontrados: ${snapshots.size()}"
                )

                listaMensajes.clear()

                for (doc in snapshots.documents) {

                    Log.d(
                        "CHAT_FIREBASE",
                        "Documento ID: ${doc.id}"
                    )

                    Log.d(
                        "CHAT_FIREBASE",
                        "Texto: ${doc.getString("mensaje")}"
                    )

                    val mensaje = doc.toObject(Mensaje::class.java)

                    if (mensaje != null) {
                        listaMensajes.add(mensaje)
                    }
                    Log.d("CHAT_FIREBASE", "reporteId recibido = $reporteId")
                }

                adapter.notifyDataSetChanged()

                if (listaMensajes.isNotEmpty()) {
                    recyclerMensajes.scrollToPosition(
                        listaMensajes.size - 1
                    )
                }

                Log.d(
                    "CHAT_FIREBASE",
                    "Mensajes cargados: ${listaMensajes.size}"
                )
            }
    }
    private fun verificarChatVacio() {

        firestore.collection("mensajes")
            .whereEqualTo("reporteId", reporteId)
            .get()
            .addOnSuccessListener { documentos ->

                // Si ya hay mensajes, no crear nada
                if (!documentos.isEmpty) {
                    return@addOnSuccessListener
                }

                val remitenteId =
                    FirebaseAuth.getInstance().currentUser?.uid

                if (remitenteId == null) {
                    Log.e(
                        "CHAT_FIREBASE",
                        "Usuario actual no autenticado"
                    )
                    return@addOnSuccessListener
                }

                val mensajeInicial = hashMapOf(
                    "remitenteId" to remitenteId,
                    "destinatarioId" to receptorId,
                    "reporteId" to reporteId,
                    "mensaje" to "Hola, tengo información sobre tu mascota.",
                    "fecha" to Timestamp.now()
                )

                firestore.collection("mensajes")
                    .add(mensajeInicial)
                    .addOnSuccessListener {
                        Log.d(
                            "CHAT_FIREBASE",
                            "Mensaje inicial creado"
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            "CHAT_FIREBASE",
                            "Error al crear mensaje inicial",
                            e
                        )
                    }
            }
            .addOnFailureListener { e ->
                Log.e(
                    "CHAT_FIREBASE",
                    "Error verificando mensajes",
                    e
                )
            }
    }
}
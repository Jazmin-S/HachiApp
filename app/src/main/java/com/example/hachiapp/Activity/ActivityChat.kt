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
    private var nombreMascota = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        firestore = FirebaseFirestore.getInstance()

        receptorId =
            intent.getStringExtra("receptorId") ?: ""

        reporteId =
            intent.getStringExtra("reporteId") ?: ""

        nombreMascota =
            intent.getStringExtra("nombreMascota") ?: "Mascota"

        recyclerMensajes =
            findViewById(R.id.recyclerMensajes)

        adapter =
            MensajeAdapter(listaMensajes)

        recyclerMensajes.layoutManager =
            LinearLayoutManager(this)

        recyclerMensajes.adapter =
            adapter

        findViewById<TextView>(
            R.id.tvNombreContacto
        ).text =
            "Dueño de $nombreMascota"

        findViewById<ImageButton>(
            R.id.btnVolver
        ).setOnClickListener {
            finish()
        }

        verificarChatVacio()

        cargarMensajes()

        val etMensaje =
            findViewById<EditText>(R.id.etMensaje)

        findViewById<CardView>(
            R.id.btnEnviar
        ).setOnClickListener {

            val texto =
                etMensaje.text.toString().trim()

            if (texto.isEmpty()) {

                Toast.makeText(
                    this,
                    "Escribe un mensaje",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val remitenteId =
                FirebaseAuth.getInstance()
                    .currentUser?.uid

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

                "nombreMascota" to nombreMascota,

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

        firestore.collection("mensajes")
            .whereEqualTo(
                "reporteId",
                reporteId
            )
            .orderBy("fecha")
            .addSnapshotListener { snapshots, e ->

                if (e != null) {
                    Log.e(
                        "CHAT_FIREBASE",
                        "ERROR",
                        e
                    )
                    return@addSnapshotListener
                }

                listaMensajes.clear()

                snapshots?.documents?.forEach { doc ->

                    val mensaje =
                        doc.toObject(
                            Mensaje::class.java
                        )

                    if (mensaje != null) {
                        listaMensajes.add(mensaje)
                    }
                }

                adapter.notifyDataSetChanged()

                if (listaMensajes.isNotEmpty()) {

                    recyclerMensajes.scrollToPosition(
                        listaMensajes.size - 1
                    )
                }
            }
    }

    private fun verificarChatVacio() {

        firestore.collection("mensajes")
            .whereEqualTo(
                "reporteId",
                reporteId
            )
            .get()
            .addOnSuccessListener { documentos ->

                if (!documentos.isEmpty) {
                    return@addOnSuccessListener
                }

                val remitenteId =
                    FirebaseAuth.getInstance()
                        .currentUser?.uid

                if (remitenteId == null) {
                    return@addOnSuccessListener
                }

                val mensajeInicial =
                    hashMapOf(

                        "remitenteId" to remitenteId,

                        "destinatarioId" to receptorId,

                        "reporteId" to reporteId,

                        "nombreMascota" to nombreMascota,

                        "mensaje" to
                                "Hola, tengo información sobre tu mascota.",

                        "fecha" to Timestamp.now()
                    )

                firestore.collection("mensajes")
                    .add(mensajeInicial)
            }
    }
}
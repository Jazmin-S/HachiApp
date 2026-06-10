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
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.MediaManager
import com.example.hachiapp.BD.CloudinaryManager

class ActivityChat : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerMensajes: RecyclerView
    private lateinit var adapter: MensajeAdapter

    private val listaMensajes = mutableListOf<Mensaje>()

    private var receptorId = ""
    private var reporteId = ""
    private var nombreMascota = ""

    private var imagenSeleccionada: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat)
        CloudinaryManager.init(this)


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

        findViewById<ImageButton>(
            R.id.btnImagen
        ).setOnClickListener {

            seleccionarImagen.launch("image/*")
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
    private val seleccionarImagen =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->

            if (uri != null) {

                imagenSeleccionada = uri

                subirImagenCloudinary(uri)
            }
        }
    private fun subirImagenCloudinary(uri: Uri) {

        Toast.makeText(
            this,
            "Subiendo imagen...",
            Toast.LENGTH_SHORT
        ).show()

        MediaManager.get()
            .upload(uri)
            .unsigned("hachiapp") // Igual que ActivityPerfil
            .option("folder", "hachiapp_chat")
            .callback(object : UploadCallback {

                override fun onStart(requestId: String) {
                }

                override fun onProgress(
                    requestId: String,
                    bytes: Long,
                    totalBytes: Long
                ) {
                }

                override fun onReschedule(
                    requestId: String,
                    error: com.cloudinary.android.callback.ErrorInfo
                ) {
                }

                override fun onSuccess(
                    requestId: String,
                    resultData: MutableMap<Any?, Any?>
                ) {

                    val imageUrl =
                        resultData["secure_url"]?.toString() ?: ""

                    runOnUiThread {

                        guardarMensajeImagen(imageUrl)
                    }
                }

                override fun onError(
                    requestId: String,
                    error: com.cloudinary.android.callback.ErrorInfo
                ) {

                    runOnUiThread {

                        Toast.makeText(
                            this@ActivityChat,
                            "Error al subir imagen: ${error.description}",
                            Toast.LENGTH_LONG
                        ).show()

                        Log.e(
                            "CLOUDINARY",
                            error.description
                        )
                    }
                }
            })
            .dispatch()
    }

    private fun guardarMensajeImagen(
        imageUrl: String
    ) {

        val remitenteId =
            FirebaseAuth.getInstance()
                .currentUser?.uid ?: return

        val mensaje = hashMapOf(

            "remitenteId" to remitenteId,

            "destinatarioId" to receptorId,

            "reporteId" to reporteId,

            "nombreMascota" to nombreMascota,

            "mensaje" to "",

            "imagenUrl" to imageUrl,

            "fecha" to Timestamp.now()
        )

        firestore.collection("mensajes")
            .add(mensaje)
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Imagen enviada",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Error al guardar imagen",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
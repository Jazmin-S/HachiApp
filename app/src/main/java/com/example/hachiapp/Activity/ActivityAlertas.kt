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
import com.example.hachiapp.Adapter.AlertaAdapter
import com.example.hachiapp.Model.Alerta
import com.example.hachiapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.example.hachiapp.models.Reporte
import com.google.firebase.firestore.Query


class ActivityAlertas : AppCompatActivity() {

    private lateinit var recyclerAlertas: RecyclerView
    private lateinit var adapter: AlertaAdapter
    private val listaAlertas = mutableListOf<Alerta>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alertas)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // ================= RECYCLER =================

        recyclerAlertas = findViewById(R.id.recyclerAlertas)

        adapter = AlertaAdapter(listaAlertas) addOnSuccessListener@{ alerta ->

            if (alerta.tipo == "reporte") {

                FirebaseFirestore.getInstance()
                    .collection("reportes")
                    .document(alerta.reporteId)
                    .get()
                    .addOnSuccessListener { doc ->

                        if (!doc.exists()) return@addOnSuccessListener

                        val reporte = doc.toObject(Reporte::class.java) ?: return@addOnSuccessListener

                        val intent = Intent(this, ActivityVolante::class.java)

                        intent.putExtra("nombreMascota", reporte.nombreMascota)
                        intent.putExtra("razaMascota", reporte.razaMascota)
                        intent.putExtra("edadMascota", reporte.edadMascota)
                        intent.putExtra("tamano", reporte.tamano)
                        intent.putExtra("colorMascota", reporte.colorMascota)
                        intent.putExtra("descripcion", reporte.descripcion)
                        intent.putExtra("fechaExtravio", reporte.fechaExtravio)
                        intent.putExtra("estadoMascota", reporte.estadoMascota)
                        intent.putExtra("latitud", reporte.latitud)
                        intent.putExtra("longitud", reporte.longitud)
                        intent.putExtra("direccion", reporte.direccion)

                        if (reporte.imagenesUrl.isNotEmpty()) {
                            intent.putExtra("imagenUrl", reporte.imagenesUrl[0])
                        }

                        startActivity(intent)
                    }

            } else if (alerta.tipo == "avistamiento") {

                val avistamientoId = alerta.avistamientoId

                if (avistamientoId.isNullOrEmpty()) return@addOnSuccessListener

                FirebaseFirestore.getInstance()
                    .collection("avistamientos")
                    .document(avistamientoId)
                    .get()
                    .addOnSuccessListener { doc ->

                        if (!doc.exists()) return@addOnSuccessListener

                        val intent = Intent(this, ActivityDetalleAvistamiento::class.java)

                        intent.putExtra("descripcion", doc.getString("descripcion"))
                        intent.putExtra("direccion", doc.getString("direccion"))
                        intent.putExtra("tipoMascota", doc.getString("tipoMascota"))
                        intent.putExtra("imagenUrl", doc.getString("imagenUrl"))
                        intent.putExtra("latitud", doc.getDouble("latitud") ?: 0.0)
                        intent.putExtra("longitud", doc.getDouble("longitud") ?: 0.0)

                        startActivity(intent)
                    }
            }
        }


        recyclerAlertas.layoutManager = LinearLayoutManager(this)
        recyclerAlertas.adapter = adapter

        cargarAlertas()

        // ================= PESTAÑAS =================

        val tabMensajes = findViewById<TextView>(R.id.Mensajes)

        tabMensajes.setOnClickListener {
            startActivity(Intent(this, ActivityMensajes::class.java))
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }

        // ================= NAVBAR =================

        findViewById<LinearLayout>(R.id.BtnInicio).setOnClickListener {
            startActivity(Intent(this, ActivityInicio::class.java))
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }

        findViewById<LinearLayout>(R.id.BtnMapa).setOnClickListener {
            startActivity(Intent(this, ActivityMapa::class.java))
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }

        findViewById<LinearLayout>(R.id.BtnHistorial).setOnClickListener {
            startActivity(Intent(this, ActivityHistorial::class.java))
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }

        findViewById<LinearLayout>(R.id.BtnReporte).setOnClickListener {
            startActivity(Intent(this, ActivityRegistroReporte::class.java))
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }

        // ================= FOTO DE PERFIL =================

        val btnPerfil = findViewById<ImageButton>(R.id.BtnPerfil)
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

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ActivityPerfil::class.java))
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }
    }

    private fun cargarAlertas() {

        FirebaseFirestore.getInstance()
            .collection("alertas")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->

                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                listaAlertas.clear()

                Log.d("ALERTAS", "Documentos encontrados: ${value?.size()}")

                value?.documents?.forEach { document ->

                    val alerta = document.toObject(Alerta::class.java)

                    if (alerta != null) {

                        Log.d(
                            "ALERTAS",
                            "${alerta.titulo} - ${alerta.descripcion}"
                        )

                        listaAlertas.add(alerta)
                    }
                }

                Log.d("ALERTAS", "Alertas cargadas: ${listaAlertas.size}")

                adapter.notifyDataSetChanged()
            }
    }
}
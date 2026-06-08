package com.example.hachiapp.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.hachiapp.Activity.ActivityRegistroReporte
import com.example.hachiapp.R
import com.example.hachiapp.BD.ReporteRepository
import com.example.hachiapp.models.Reporte
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hachiapp.adapters.ReporteAdapter
import com.google.firebase.firestore.FirebaseFirestore

class ActivityInicio : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)
        val recycler =
            findViewById<RecyclerView>(R.id.recyclerReportes)

        recycler.layoutManager =
            GridLayoutManager(this, 2)

        val listaReportes = mutableListOf<Reporte>()

        val adapter = ReporteAdapter(listaReportes) { reporte ->
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
            intent.putExtra("usuarioId", reporte.usuarioId)
            intent.putExtra("recompensa", reporte.recompensa)
            intent.putExtra("direccion", reporte.direccion)
            if (reporte.imagenesUrl.isNotEmpty()) {
                intent.putExtra("imagenUrl", reporte.imagenesUrl[0])
            }
            intent.putExtra("reporteId", reporte.id)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        recycler.adapter = adapter

        FirebaseFirestore.getInstance()
            .collection("reportes")
            .get()
            .addOnSuccessListener { resultado ->

                listaReportes.clear()

                for (documento in resultado) {
                    val reporte = documento.toObject(Reporte::class.java)
                        .copy(id = documento.id) // Copia el ID del documento
                    listaReportes.add(reporte)
                }

                adapter.notifyDataSetChanged()
            }

        // PERFIL (sin acción por ahora)
        val btnPerfil = findViewById<ImageButton>(R.id.BtnPerfil)
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ActivityPerfil::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }


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

        // HISTORIAL
        val btnHistorial = findViewById<LinearLayout>(R.id.BtnHistorial)
        btnHistorial.setOnClickListener {
            startActivity(Intent(this, ActivityHistorial::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // REPORTE
        val btnReporte = findViewById<LinearLayout>(R.id.BtnReporte)
        btnReporte.setOnClickListener {
            startActivity(Intent(this, ActivityRegistroReporte::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
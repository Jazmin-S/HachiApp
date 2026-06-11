package com.example.hachiapp.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.hachiapp.R

class ActivityDetalleAvistamiento : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_avistamiento)

        // =========================
        // DATOS RECIBIDOS DESDE ALERTAS
        // =========================
        val descripcion = intent.getStringExtra("descripcion")
        val direccion = intent.getStringExtra("direccion")
        val tipoMascota = intent.getStringExtra("tipoMascota")

        // URL de imagen subida a Cloudinary (puede venir null si no hay imagen)
        val imagenUrl = intent.getStringExtra("imagenUrl")

        // =========================
        // REFERENCIAS UI
        // =========================
        val tvDescripcion = findViewById<TextView>(R.id.tvDescripcion)
        val tvDireccion = findViewById<TextView>(R.id.tvDireccion)
        val tvTipoMascota = findViewById<TextView>(R.id.tvTipoMascota)
        val imgAvistamiento = findViewById<ImageView>(R.id.imgAvistamientoDetalle)

        // Asignación con valores por defecto si no llegan datos
        tvDescripcion.text = descripcion ?: "Sin descripción"
        tvDireccion.text = direccion ?: "Sin dirección"
        tvTipoMascota.text = tipoMascota ?: "Sin tipo"

        // =========================
        // CARGA DE IMAGEN (CLOUDINARY)
        // =========================
        if (!imagenUrl.isNullOrEmpty()) {

            // Glide maneja carga asíncrona de imágenes desde URL
            Glide.with(this)
                .load(imagenUrl)
                .placeholder(R.drawable.perfil) // imagen mientras carga
                .error(R.drawable.perfil)       // fallback si falla la carga
                .into(imgAvistamiento)
        }

        // =========================
        // CERRAR DETALLE Y VOLVER A ALERTAS
        // =========================
        findViewById<Button>(R.id.btnCerrarAvistamiento).setOnClickListener {

            // Regresa a la pantalla de alertas
            val intent = Intent(this, ActivityAlertas::class.java)
            startActivity(intent)

            // Cierra esta Activity para no acumularla en el stack
            finish()
        }
    }
}
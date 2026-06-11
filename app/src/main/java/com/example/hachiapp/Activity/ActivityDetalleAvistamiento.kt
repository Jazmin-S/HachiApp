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

        val descripcion = intent.getStringExtra("descripcion")
        val direccion = intent.getStringExtra("direccion")
        val tipoMascota = intent.getStringExtra("tipoMascota")
        val imagenUrl = intent.getStringExtra("imagenUrl") // 👈 NUEVO

        val tvDescripcion = findViewById<TextView>(R.id.tvDescripcion)
        val tvDireccion = findViewById<TextView>(R.id.tvDireccion)
        val tvTipoMascota = findViewById<TextView>(R.id.tvTipoMascota)
        val imgAvistamiento = findViewById<ImageView>(R.id.imgAvistamientoDetalle)

        tvDescripcion.text = descripcion ?: "Sin descripción"
        tvDireccion.text = direccion ?: "Sin dirección"
        tvTipoMascota.text = tipoMascota ?: "Sin tipo"

        // =========================
        // CARGAR IMAGEN CLOUDINARY
        // =========================
        if (!imagenUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imagenUrl)
                .placeholder(R.drawable.perfil) // puedes cambiarlo por "loading"
                .error(R.drawable.perfil)
                .into(imgAvistamiento)
        }

        findViewById<Button>(R.id.btnCerrarAvistamiento).setOnClickListener {
            val intent = Intent(this, ActivityAlertas::class.java)
            startActivity(intent)
            finish()
        }
    }
}
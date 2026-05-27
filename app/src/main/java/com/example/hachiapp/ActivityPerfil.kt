package com.example.hachiapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ActivityPerfil : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Genera lista ["1", "2", ... "100"]
        val edades = (1..100).map { it.toString() }

        // Adapter personalizado para controlar colores del Spinner
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, edades) {

            // Cómo se ve el item cuando el Spinner está CERRADO
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(android.graphics.Color.BLACK) // texto negro
                return view
            }

            // Cómo se ve cada item cuando el Spinner está ABIERTO
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(android.graphics.Color.BLACK) // texto negro
                view.setBackgroundColor(android.graphics.Color.WHITE) // fondo blanco
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerEdad).adapter = adapter // conecta datos con el Spinner del XML

        // Abre la galería del teléfono y espera que el usuario elija una imagen
        val seleccionarImagen = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) { // si el usuario eligió algo (no canceló)
                findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.imgPerfil)
                    .setImageURI(uri) // muestra la imagen elegida en el círculo
            }
        }

        // Al tocar la foto circular abre la galería
        findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.imgPerfil)
            .setOnClickListener {
                seleccionarImagen.launch("image/*") // filtra solo imágenes
            }
    }
}
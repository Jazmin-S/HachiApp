package com.example.hachiapp.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hachiapp.Activity.ActivityContrasena
import com.example.hachiapp.R

class ActivityCodigo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_codigo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //boton de verificar
        val btnVerificar = findViewById<Button>(R.id.btnVerificar)
        //funcion que redirige a la pantalla de contraseña
        btnVerificar.setOnClickListener {
            val intent = Intent(this, ActivityContrasena::class.java)
            startActivity(intent)
        }
    }
}
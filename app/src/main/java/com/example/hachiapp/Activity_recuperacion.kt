package com.example.hachiapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button

class Activity_recuperacion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recuperacion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //boton de principal de recuperacion
        val BtnCodigoRecuperacion = findViewById<Button>(R.id.BtnCodigoRecuperacion)
        //funcion que redirige a la pantalla de codigo
        BtnCodigoRecuperacion.setOnClickListener {
            val intent = Intent(this, ActivityCodigo::class.java)
            startActivity(intent)
        }
    }
}
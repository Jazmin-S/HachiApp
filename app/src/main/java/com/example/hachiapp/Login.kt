package com.example.hachiapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.login)

        // Ajustar padding por barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                20 + systemBars.left,
                20 + systemBars.top,
                20 + systemBars.right,
                20 + systemBars.bottom
            )

            insets
        }

        // Subrayar texto
        val olvidaContra = findViewById<TextView>(R.id.OlvidaContraseña)
        olvidaContra.paint.isUnderlineText = true
    }
}
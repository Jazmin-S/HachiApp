package com.example.hachiapp.Activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hachiapp.Activity.ActivityInicio
import com.example.hachiapp.Activity.ActivityRegistro
import com.example.hachiapp.Activity.Activity_recuperacion
import com.example.hachiapp.R
import com.google.firebase.auth.FirebaseAuth

class Activity_Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_login)

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

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

        // Referencias

        val crearCuenta = findViewById<TextView>(R.id.CrearCuenta)

        val correoLogin = findViewById<EditText>(R.id.CorreoLogin)
        val passwordLogin = findViewById<EditText>(R.id.PasswordLogin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Subrayar textos
        crearCuenta.paint.isUnderlineText = true

        // Color negro
        crearCuenta.setTextColor(Color.BLACK)


        // CLICK CREAR CUENTA
        crearCuenta.setOnClickListener {

            crearCuenta.setTextColor(Color.BLUE)

            Handler(Looper.getMainLooper()).postDelayed({

                crearCuenta.setTextColor(Color.BLACK)

                val intent = Intent(this, ActivityRegistro::class.java)
                startActivity(intent)

            }, 100)
        }

        // LOGIN
        btnLogin.setOnClickListener {

            val correo = correoLogin.text.toString().trim()
            val password = passwordLogin.text.toString().trim()

            if (correo.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                auth.signInWithEmailAndPassword(correo, password)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            // ADMIN
                            //CORREO DE ADMIN
                            if (correo == "admin@hachi.com") {
                                //CONTRASEÑA: admin123

                                Toast.makeText(
                                    this,
                                    "Bienvenido administrador",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {

                                // USUARIO NORMAL
                                Toast.makeText(
                                    this,
                                    "Inicio de sesión exitoso",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            // Ambos entran a ActivityInicio
                            startActivity(
                                Intent(this, ActivityInicio::class.java)
                            )

                            finish()

                        } else {

                            Toast.makeText(
                                this,
                                "Correo o contraseña incorrectos",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}
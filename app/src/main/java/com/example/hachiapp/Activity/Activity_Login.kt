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
import com.example.hachiapp.R
import com.google.firebase.auth.FirebaseAuth

class Activity_Login : AppCompatActivity() {

    // Instancia de Firebase Authentication para manejar el login
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Inicializa Firebase Auth (necesario para login con correo y contraseña)
        auth = FirebaseAuth.getInstance()

        // Ajuste de padding para evitar que el contenido quede debajo de las barras del sistema
        // (status bar y navigation bar)
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

        // Referencias a elementos de la UI
        val crearCuenta = findViewById<TextView>(R.id.CrearCuenta)
        val correoLogin = findViewById<EditText>(R.id.CorreoLogin)
        val passwordLogin = findViewById<EditText>(R.id.PasswordLogin)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // Estilo visual del texto "Crear cuenta"
        crearCuenta.paint.isUnderlineText = true
        crearCuenta.setTextColor(Color.BLACK)

        // CLICK EN "CREAR CUENTA"
        crearCuenta.setOnClickListener {

            // Efecto visual de clic (cambia color temporalmente)
            crearCuenta.setTextColor(Color.BLUE)

            // Delay corto para simular animación de clic antes de navegar
            Handler(Looper.getMainLooper()).postDelayed({

                crearCuenta.setTextColor(Color.BLACK)

                // Navega a pantalla de registro
                val intent = Intent(this, ActivityRegistro::class.java)
                startActivity(intent)

            }, 100)
        }

        // BOTÓN LOGIN
        btnLogin.setOnClickListener {

            val correo = correoLogin.text.toString().trim()
            val password = passwordLogin.text.toString().trim()

            // Validación básica antes de enviar a Firebase
            if (correo.isEmpty() || password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                // Inicio de sesión con Firebase Authentication
                auth.signInWithEmailAndPassword(correo, password)
                    .addOnCompleteListener { task ->

                        if (task.isSuccessful) {

                            // Identificación simple de administrador por correo fijo
                            // (en producción esto debería manejarse con roles en Firestore o claims)
                            if (correo == "admin@hachi.com") {

                                Toast.makeText(
                                    this,
                                    "Bienvenido administrador",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } else {

                                // Usuario normal autenticado correctamente
                                Toast.makeText(
                                    this,
                                    "Inicio de sesión exitoso",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            // Ambos tipos de usuario entran a la pantalla principal
                            startActivity(
                                Intent(this, com.example.hachiapp.Activity.ActivityInicio::class.java)
                            )

                            // Evita regresar al login al presionar "back"
                            finish()

                        } else {

                            // Error de autenticación (correo o contraseña incorrectos / usuario no existe)
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
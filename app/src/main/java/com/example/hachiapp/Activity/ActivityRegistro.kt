package com.example.hachiapp.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hachiapp.R
import com.google.firebase.auth.FirebaseAuth

class ActivityRegistro : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()

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

        // Referencias
        val textNombre = findViewById<EditText>(R.id.textNombre)

        val textApellido = findViewById<EditText>(R.id.textApellido)

        val textCorreo = findViewById<EditText>(R.id.textCorreo)

        val textContrasena = findViewById<EditText>(R.id.textContrasena)

        val textTelefono = findViewById<EditText>(R.id.textTelefono)

        val textNombreImagen =
            findViewById<EditText>(R.id.textNombreImagen)

        val imgPreview =
            findViewById<ImageView>(R.id.imgPreview)

        val btnSeleccionarImagen =
            findViewById<ImageButton>(R.id.btnSeleccionarImagen)

        val btnGuardar =
            findViewById<Button>(R.id.btnGuardar)

        // Seleccionar imagen
        btnSeleccionarImagen.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)

            intent.type = "image/*"

            startActivityForResult(intent, 100)
        }

        // REGISTRO
        btnGuardar.setOnClickListener {

            val nombre = textNombre.text.toString().trim()

            val apellido = textApellido.text.toString().trim()

            val correo = textCorreo.text.toString().trim()

            val contrasena = textContrasena.text.toString().trim()

            val telefono = textTelefono.text.toString().trim()

            // Validaciones
            if (
                nombre.isEmpty() ||
                apellido.isEmpty() ||
                correo.isEmpty() ||
                contrasena.isEmpty() ||
                telefono.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()

            } else if (contrasena.length < 6) {

                Toast.makeText(
                    this,
                    "La contraseña debe tener mínimo 6 caracteres",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                // Registrar usuario
                auth.createUserWithEmailAndPassword(
                    correo,
                    contrasena
                ).addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        Toast.makeText(
                            this,
                            "Usuario registrado correctamente",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Ir a inicio
                        startActivity(
                            Intent(
                                this,
                                ActivityInicio::class.java
                            )
                        )

                        finish()

                    } else {

                        Toast.makeText(
                            this,
                            "Error: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    // Resultado de selección de imagen
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == 100 &&
            resultCode == RESULT_OK &&
            data != null
        ) {

            imageUri = data.data

            val imgPreview =
                findViewById<ImageView>(R.id.imgPreview)

            val textNombreImagen =
                findViewById<EditText>(R.id.textNombreImagen)

            imgPreview.setImageURI(imageUri)

            textNombreImagen.setText(
                imageUri.toString()
            )
        }
    }
}
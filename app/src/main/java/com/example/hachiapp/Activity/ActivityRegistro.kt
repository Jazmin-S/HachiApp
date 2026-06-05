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
import com.example.hachiapp.BD.UsuarioRepository
import com.example.hachiapp.R
import com.example.hachiapp.models.Usuario
import com.google.firebase.auth.FirebaseAuth

/*
 * Activity encargada de registrar un nuevo usuario
 * en Firebase Authentication y guardar sus datos
 * en la colección "usuarios" de Firestore.
 */
class ActivityRegistro : AppCompatActivity() {

    /* Instancia de Firebase Authentication
     * para crear la cuenta del usuario.
     */
    private lateinit var auth: FirebaseAuth

    /* Guarda la URI de la imagen seleccionada
     * desde la galería del teléfono.
     */
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(R.layout.activity_registro)

        /* Obtiene la instancia de Firebase Auth
         * para usarla en el registro.
         */
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

        /* Referencias a los campos del formulario
         * para obtener los datos que escribió el usuario.
         */
        val textNombre = findViewById<EditText>(R.id.textNombre)

        val textApellido = findViewById<EditText>(R.id.textApellido)

        val textCorreo = findViewById<EditText>(R.id.textCorreo)

        val textContrasena = findViewById<EditText>(R.id.textContrasena)

        val textTelefono = findViewById<EditText>(R.id.textTelefono)

        val textNombreImagen = findViewById<EditText>(R.id.textNombreImagen)

        val imgPreview = findViewById<ImageView>(R.id.imgPreview)

        val btnSeleccionarImagen = findViewById<ImageButton>(R.id.btnSeleccionarImagen)

        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        /* Al tocar el botón de imagen abre la galería
         * del teléfono para seleccionar una foto.
         */
        btnSeleccionarImagen.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)

            intent.type = "image/*"

            startActivityForResult(intent, 100)
        }

        /* Al tocar el botón Guardar valida los campos
         * y registra al usuario en Firebase.
         */
        btnGuardar.setOnClickListener {

            /* Obtiene el texto de cada campo
             * y elimina espacios al inicio y al final.
             */
            val nombre = textNombre.text.toString().trim()

            val apellido = textApellido.text.toString().trim()

            val correo = textCorreo.text.toString().trim()

            val contrasena = textContrasena.text.toString().trim()

            val telefono = textTelefono.text.toString().trim()

            /* Verifica que ningún campo esté vacío
             * antes de intentar registrar al usuario.
             */
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

                /* Verifica que la contraseña tenga
                 * al menos 6 caracteres como requiere Firebase.
                 */
                Toast.makeText(
                    this,
                    "La contraseña debe tener mínimo 6 caracteres",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                /* Crea la cuenta del usuario en Firebase Auth
                 * usando su correo y contraseña.
                 */
                auth.createUserWithEmailAndPassword(
                    correo,
                    contrasena
                ).addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        /* Si la cuenta se creó correctamente
                         * guarda los datos del usuario en Firestore.
                         */
                        val usuario = Usuario(
                            nombre = nombre,
                            apellidos = apellido,
                            correo = correo,
                            telefono = telefono,
                            fechaRegistro = System.currentTimeMillis()
                        )

                        /* Usa el repositorio para guardar
                         * los datos en la colección "usuarios".
                         */
                        val repository = UsuarioRepository()

                        repository.guardarPerfil(
                            usuario = usuario,
                            /*
                             * Se ejecuta cuando los datos
                             * se guardan correctamente en Firestore.
                             */
                            onSuccess = {

                                Toast.makeText(
                                    this,
                                    "Usuario registrado correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()

                                /* Navega a la pantalla de inicio
                                 * y cierra el registro para que
                                 * el usuario no pueda regresar.
                                 */
                                startActivity(
                                    Intent(
                                        this,
                                        ActivityInicio::class.java
                                    )
                                )

                                finish()
                            },
                            /*
                             * Se ejecuta cuando ocurre algún error
                             * al guardar los datos en Firestore.
                             */
                            onError = { exception ->

                                Toast.makeText(
                                    this,
                                    "Error al guardar perfil: ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )

                    } else {

                        /* Si hubo un error al crear la cuenta
                         * muestra el mensaje de error de Firebase.
                         */
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

    /* Se ejecuta cuando el usuario regresa
     * de la galería con una imagen seleccionada.
     * requestCode 100 identifica que viene de la galería.
     */
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

            /* Guarda la URI de la imagen seleccionada
             * y la muestra en la vista previa.
             */
            imageUri = data.data

            val imgPreview = findViewById<ImageView>(R.id.imgPreview)

            val textNombreImagen = findViewById<EditText>(R.id.textNombreImagen)

            imgPreview.setImageURI(imageUri)

            textNombreImagen.setText(imageUri.toString())
        }
    }
}
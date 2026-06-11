package com.example.hachiapp.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.hachiapp.BD.CloudinaryManager
import com.example.hachiapp.BD.UsuarioRepository
import com.example.hachiapp.R
import com.example.hachiapp.models.Usuario
import com.google.firebase.auth.FirebaseAuth

class ActivityRegistro : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val repository = UsuarioRepository()

    private var imageUri: Uri? = null
    private var urlImagenCloudinary: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)

        auth = FirebaseAuth.getInstance()
        CloudinaryManager.init(this)

        val textNombre = findViewById<EditText>(R.id.textNombre)
        val textApellido = findViewById<EditText>(R.id.textApellido)
        val textCorreo = findViewById<EditText>(R.id.textCorreo)
        val textContrasena = findViewById<EditText>(R.id.textContrasena)
        val textTelefono = findViewById<EditText>(R.id.textTelefono)

        val imgPreview = findViewById<ImageView>(R.id.imgPreview)
        val btnSeleccionarImagen = findViewById<ImageButton>(R.id.btnSeleccionarImagen)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)

        // =========================
        // SELECCIONAR IMAGEN
        // =========================
        btnSeleccionarImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // =========================
        // GUARDAR USUARIO
        // =========================
        btnGuardar.setOnClickListener {

            val nombre = textNombre.text.toString().trim()
            val apellido = textApellido.text.toString().trim()
            val correo = textCorreo.text.toString().trim()
            val contrasena = textContrasena.text.toString().trim()
            val telefono = textTelefono.text.toString().trim()

            if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty()
                || contrasena.isEmpty() || telefono.isEmpty()
            ) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasena.length < 6) {
                Toast.makeText(this, "Mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                        // SI HAY IMAGEN → subir a Cloudinary
                        if (imageUri != null) {
                            subirImagenYGuardar(uid, nombre, apellido, correo, telefono)
                        } else {
                            guardarEnFirestore(nombre, apellido, correo, telefono, "")
                        }

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

    // =========================
    // SUBIR IMAGEN CLOUDINARY
    // =========================
    private fun subirImagenYGuardar(
        uid: String,
        nombre: String,
        apellido: String,
        correo: String,
        telefono: String
    ) {
        val publicId = "perfil_$uid"

        MediaManager.get()
            .upload(imageUri!!)
            .unsigned("hachiapp")
            .option("folder", "hachi_perfiles")
            .option("public_id", publicId)
            .callback(object : UploadCallback {

                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onReschedule(requestId: String, error: ErrorInfo) {}

                override fun onSuccess(
                    requestId: String,
                    resultData: MutableMap<Any?, Any?>
                ) {
                    val url = resultData["secure_url"]?.toString() ?: ""
                    urlImagenCloudinary = url

                    guardarEnFirestore(nombre, apellido, correo, telefono, url)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    runOnUiThread {
                        Toast.makeText(
                            this@ActivityRegistro,
                            "Error al subir imagen: ${error.description}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
            .dispatch()
    }

    // =========================
    // GUARDAR EN FIRESTORE
    // =========================
    private fun guardarEnFirestore(
        nombre: String,
        apellido: String,
        correo: String,
        telefono: String,
        fotoUrl: String
    ) {
        val usuario = Usuario(
            nombre = nombre,
            apellidos = apellido,
            correo = correo,
            telefono = telefono,
            fotoPerfil = fotoUrl,
            fechaRegistro = System.currentTimeMillis()
        )

        repository.guardarPerfil(
            usuario = usuario,
            onSuccess = {
                Toast.makeText(this, "Usuario registrado ✓", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this, ActivityInicio::class.java))
                finish()
            },
            onError = {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        )
    }

    // =========================
    // RESULTADO DE IMAGEN
    // =========================
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.data

            findViewById<ImageView>(R.id.imgPreview)
                .setImageURI(imageUri)
        }
    }
}
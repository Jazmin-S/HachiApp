package com.example.hachiapp.Activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.hachiapp.BD.CloudinaryManager
import com.example.hachiapp.BD.UsuarioRepository
import com.example.hachiapp.R
import com.example.hachiapp.models.Usuario
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class ActivityPerfil : AppCompatActivity() {

    private val repository = UsuarioRepository()
    private var imagenSeleccionada: Uri? = null
    private var urlImagenCloudinary: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        CloudinaryManager.init(this)

        configurarSpinner()
        configurarSeleccionImagen()
        cargarPerfil()
        configurarBotonGuardar()
        configurarBotonCerrarSesion()
    }

    private fun configurarBotonCerrarSesion() {
        findViewById<MaterialButton>(R.id.btnCerrarSesion).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que quieres cerrar sesión?")
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, Activity_Login::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun configurarSpinner() {
        val edades = (1..100).map { it.toString() }
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            edades
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).setTextColor(Color.BLACK)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(Color.BLACK)
                view.setBackgroundColor(Color.WHITE)
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerEdad).adapter = adapter
    }

    private fun configurarSeleccionImagen() {
        val seleccionarImagen = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                imagenSeleccionada = uri
                findViewById<CircleImageView>(R.id.imgPerfil).setImageURI(uri)
            }
        }
        findViewById<CircleImageView>(R.id.imgPerfil).setOnClickListener {
            seleccionarImagen.launch("image/*")
        }
    }

    private fun cargarPerfil() {
        repository.obtenerPerfil(
            onSuccess = { usuario ->
                findViewById<EditText>(R.id.editNombre).setText(usuario.nombre)
                findViewById<EditText>(R.id.editDescripcion).setText(usuario.descripcion)

                val spinner = findViewById<Spinner>(R.id.spinnerEdad)
                val index = (1..100).indexOfFirst { it.toString() == usuario.edad }
                if (index >= 0) spinner.setSelection(index)

                if (usuario.fotoPerfil.isNotEmpty() && usuario.fotoPerfil.startsWith("http")) {
                    com.bumptech.glide.Glide.with(this)
                        .load(usuario.fotoPerfil)
                        .placeholder(R.drawable.perfil)
                        .into(findViewById(R.id.imgPerfil))
                    urlImagenCloudinary = usuario.fotoPerfil
                }
            },
            onError = {}
        )
    }

    private fun configurarBotonGuardar() {
        findViewById<MaterialButton>(R.id.btnGuardarPerfil)
            .setOnClickListener {
                val nombre = findViewById<EditText>(R.id.editNombre).text.toString().trim()
                val descripcion = findViewById<EditText>(R.id.editDescripcion).text.toString().trim()
                val edad = findViewById<Spinner>(R.id.spinnerEdad).selectedItem.toString()
                val correo = FirebaseAuth.getInstance().currentUser?.email ?: ""

                if (nombre.isEmpty()) {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (imagenSeleccionada != null) {
                    subirImagenYGuardar(imagenSeleccionada!!, nombre, correo, edad, descripcion)
                } else {
                    guardarEnFirestore(nombre, correo, edad, descripcion, urlImagenCloudinary)
                }
            }
    }

    private fun subirImagenYGuardar(
        uri: Uri,
        nombre: String,
        correo: String,
        edad: String,
        descripcion: String
    ) {
        mostrarCarga(true)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "sin_uid"

        MediaManager.get()
            .upload(uri)
            .unsigned("hachiapp")
            .option("folder", "hachi_perfiles")
            .option("public_id", "perfil_$uid")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}

                override fun onSuccess(requestId: String, resultData: MutableMap<Any?, Any?>) {
                    val url = resultData["secure_url"]?.toString() ?: ""
                    urlImagenCloudinary = url
                    guardarEnFirestore(nombre, correo, edad, descripcion, url)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    mostrarCarga(false)
                    runOnUiThread {
                        Toast.makeText(
                            this@ActivityPerfil,
                            "Error al subir imagen: ${error.description}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
            .dispatch()
    }

    private fun guardarEnFirestore(
        nombre: String,
        correo: String,
        edad: String,
        descripcion: String,
        fotoUrl: String
    ) {
        val usuario = Usuario(
            nombre = nombre,
            correo = correo,
            edad = edad,
            descripcion = descripcion,
            fotoPerfil = fotoUrl,
            fechaRegistro = System.currentTimeMillis()
        )

        repository.guardarPerfil(
            usuario = usuario,
            onSuccess = {
                mostrarCarga(false)
                imagenSeleccionada = null
                Toast.makeText(this, "Perfil actualizado ✓", Toast.LENGTH_SHORT).show()
            },
            onError = { exception ->
                mostrarCarga(false)
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun mostrarCarga(mostrar: Boolean) {
        runOnUiThread {
            val btn = findViewById<MaterialButton>(R.id.btnGuardarPerfil)
            val progress = findViewById<ProgressBar>(R.id.progressBar)
            btn.isEnabled = !mostrar
            btn.text = if (mostrar) "Guardando..." else "Guardar cambios"
            progress.visibility = if (mostrar) View.VISIBLE else View.GONE
        }
    }
}
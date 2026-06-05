package com.example.hachiapp.Activity

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.hachiapp.BD.UsuarioRepository
import com.example.hachiapp.R
import com.example.hachiapp.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

/*
 * Activity encargada de mostrar y actualizar
 * la información personal del usuario autenticado
 * en la colección "usuarios" de Firestore.
 */
class ActivityPerfil : AppCompatActivity() {

    /*
     * Repositorio que se comunica con Firestore
     * para leer y guardar los datos del usuario.
     */
    private val repository = UsuarioRepository()

    /*
     * Guarda la URI de la imagen seleccionada
     * desde la galería del teléfono.
     */
    private var imagenSeleccionada: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        configurarSpinner()
        configurarSeleccionImagen()
        cargarPerfil()
        configurarBotonGuardar()
    }

    /*
     * Configura el Spinner de edad con una lista del 1 al 100.
     * Se personaliza el color del texto para que se vea negro
     * tanto cuando está cerrado como cuando está abierto.
     */
    private fun configurarSpinner() {

        /*Se genera una lista de números del 1 al 100
         * convertidos a texto ["1", "2", ... "100"].
         */
        val edades = (1..100).map { it.toString() }

        /*Se crea un adapter personalizado para controlar
         * cómo se ve el texto dentro del Spinner.
         * Se sobreescriben 2 métodos:
         * 1. getView: controla el item cuando el Spinner está CERRADO.
         * 2. getDropDownView: controla los items cuando está ABIERTO.
         */
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

        /*Se conecta el adapter con el Spinner del XML
         * para que muestre la lista de edades.
         */
        findViewById<Spinner>(R.id.spinnerEdad).adapter = adapter
    }

    /*
     * Configura la selección de imagen desde la galería.
     * Al tocar la foto circular se abre el selector de imágenes
     * y la imagen elegida se muestra en pantalla.
     * Se crearon 1 parámetro interno:
     * 1. uri: Dirección de la imagen seleccionada en el teléfono.
     */
    private fun configurarSeleccionImagen() {

        /*Se registra el selector de imágenes.
         * Cuando el usuario elige una imagen
         * se ejecuta el bloque { uri -> }.
         */
        val seleccionarImagen = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                /*Se guarda la URI de la imagen seleccionada
                 * y se muestra en el círculo de perfil.
                 */
                imagenSeleccionada = uri
                findViewById<CircleImageView>(R.id.imgPerfil).setImageURI(uri)
            }
        }

        /*Al tocar la foto circular se abre la galería
         * filtrando solo imágenes (jpg, png, etc).
         */
        findViewById<CircleImageView>(R.id.imgPerfil).setOnClickListener {
            seleccionarImagen.launch("image/*")
        }
    }

    /*
     * Carga los datos del usuario desde Firestore
     * y los muestra en los campos del formulario.
     * Se crearon 2 parámetros:
     * 1. onSuccess: Función que se ejecuta cuando los datos se obtienen correctamente.
     * 2. onError: Función que se ejecuta cuando ocurre algún error al obtener los datos.
     */
    private fun cargarPerfil() {

        /*Se pide al repositorio los datos del usuario
         * guardados en Firestore.
         */
        repository.obtenerPerfil(
            /*
             * Se ejecuta cuando los datos se obtienen
             * correctamente de Firestore.
             */
            onSuccess = { usuario ->

                /*Se colocan los datos del usuario
                 * en cada campo del formulario.
                 */
                findViewById<EditText>(R.id.editNombre).setText(usuario.nombre)
                findViewById<EditText>(R.id.editDescripcion).setText(usuario.descripcion)

                /*Se busca la edad guardada en la lista del Spinner
                 * y se selecciona automáticamente.
                 */
                val spinner = findViewById<Spinner>(R.id.spinnerEdad)
                val index = (1..100).indexOfFirst { it.toString() == usuario.edad }
                if (index >= 0) spinner.setSelection(index)
            },
            /*
             * Si no encuentra datos no hace nada,
             * el usuario llenará los campos manualmente.
             */
            onError = { }
        )
    }

    /*
     * Configura el botón Guardar para capturar los datos
     * del formulario y enviarlos a Firestore.
     * Se crearon 3 parámetros internos:
     * 1. usuario: Objeto Usuario con los datos capturados del formulario.
     * 2. onSuccess: Función que se ejecuta cuando el perfil se guarda correctamente.
     * 3. onError: Función que se ejecuta cuando ocurre algún error al guardar.
     */
    private fun configurarBotonGuardar() {

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnGuardarPerfil)
            .setOnClickListener {

                /*Se obtiene el texto de cada campo
                 * eliminando espacios al inicio y al final.
                 */
                val nombre = findViewById<EditText>(R.id.editNombre)
                    .text.toString().trim()

                val descripcion = findViewById<EditText>(R.id.editDescripcion)
                    .text.toString().trim()

                val edad = findViewById<Spinner>(R.id.spinnerEdad)
                    .selectedItem.toString()

                /*Se obtiene el correo del usuario
                 * actualmente autenticado en Firebase.
                 */
                val correo = FirebaseAuth.getInstance().currentUser?.email ?: ""

                /*Se verifica que el nombre no esté vacío
                 * antes de intentar guardar en Firestore.
                 */
                if (nombre.isEmpty()) {
                    Toast.makeText(
                        this,
                        "El nombre es obligatorio",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                /*Se crea el objeto Usuario con los datos
                 * capturados desde el formulario.
                 */
                val usuario = Usuario(
                    nombre = nombre,
                    correo = correo,
                    edad = edad,
                    descripcion = descripcion,
                    fotoPerfil = imagenSeleccionada?.toString() ?: "",
                    fechaRegistro = System.currentTimeMillis()
                )

                /*Se envían los datos a Firestore
                 * a través del repositorio.
                 */
                repository.guardarPerfil(
                    usuario = usuario,
                    /*
                     * Se ejecuta cuando el perfil
                     * se guarda correctamente en Firestore.
                     */
                    onSuccess = {
                        Toast.makeText(
                            this,
                            "Perfil actualizado",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    /*
                     * Se ejecuta cuando ocurre algún error
                     * al guardar el perfil en Firestore.
                     */
                    onError = { exception ->
                        Toast.makeText(
                            this,
                            "Error: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
    }
}
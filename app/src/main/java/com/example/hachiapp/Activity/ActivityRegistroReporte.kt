package com.example.hachiapp.Activity

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hachiapp.R
import java.util.Calendar
import kotlin.collections.iterator

class ActivityRegistroReporte : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_reporte)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias a las vistas
        val etColorMascota = findViewById<EditText>(R.id.etColorMascota)
        val viewColorPreview = findViewById<View>(R.id.viewColorPreview)
        val btnCalendario = findViewById<ImageButton>(R.id.btnCalendario)
        val tvFecha = findViewById<TextView>(R.id.tvFecha)

        // Paleta de colores
        val colorMap = mapOf(
            R.id.colorNegro      to Pair("#1A1A1A", "Negro"),
            R.id.colorGrisOscuro to Pair("#555555", "Gris oscuro"),
            R.id.colorGrisClaro  to Pair("#BBBBBB", "Gris claro"),
            R.id.colorBlanco     to Pair("#F5F5F5", "Blanco"),
            R.id.colorCafeOscuro to Pair("#4E2A04", "Café oscuro"),
            R.id.colorCafe       to Pair("#8B4513", "Café"),
            R.id.colorBeige      to Pair("#D2B48C", "Beige"),
            R.id.colorDorado     to Pair("#DAA520", "Dorado"),
            R.id.colorNaranja    to Pair("#E07020", "Naranja"),
            R.id.colorAmarillo   to Pair("#F5D000", "Amarillo"),
            R.id.colorRojo       to Pair("#CC2200", "Rojo"),
            R.id.colorRosa       to Pair("#F4A0B0", "Rosa"),
            R.id.colorAzul       to Pair("#1565C0", "Azul"),
            R.id.colorAzulClaro  to Pair("#64B5F6", "Azul claro"),
            R.id.colorVerde      to Pair("#2E7D32", "Verde"),
            R.id.colorVerdeClaro to Pair("#A5D6A7", "Verde claro")
        )

        for ((id, data) in colorMap) {
            findViewById<View>(id).setOnClickListener {
                val (hex, nombre) = data
                etColorMascota.setText(nombre)
                viewColorPreview.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(hex))
            }
        }

        // DatePicker al tocar el ícono de calendario
        btnCalendario.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                tvFecha.text = "$day/${month + 1}/$year"
                tvFecha.setTextColor(Color.BLACK)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
}
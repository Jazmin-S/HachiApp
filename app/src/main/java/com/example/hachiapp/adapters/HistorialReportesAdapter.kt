package com.example.hachiapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hachiapp.BD.ReporteRepository
import com.example.hachiapp.R
import com.example.hachiapp.models.Reporte

class HistorialReportesAdapter(private var listaReportes: List<Reporte> = emptyList()) :
    RecyclerView.Adapter<HistorialReportesAdapter.HistorialViewHolder>() {

    class HistorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgMascota: ImageView = view.findViewById(R.id.imgHistorialMascota)
        val txtNombre: TextView = view.findViewById(R.id.txtHistorialNombre)
        val txtFecha: TextView = view.findViewById(R.id.txtHistorialFecha)
        val txtUbicacion: TextView = view.findViewById(R.id.txtHistorialUbicacion)
        val txtEstado: TextView = view.findViewById(R.id.txtHistorialEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reporte_usuario, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val reporte = listaReportes[position]

        // Datos principales del reporte
        holder.txtNombre.text = reporte.nombreMascota
        holder.txtFecha.text = "Perdido el: ${reporte.fechaExtravio}"

        // Ubicación con fallback si no existe dirección
        holder.txtUbicacion.text = if (!reporte.direccion.isNullOrEmpty()) {
            reporte.direccion
        } else {
            "Ubicación sin dirección"
        }

        // Estado actual del reporte (valor por defecto: Activo)
        val estado = reporte.estadoMascota ?: "Activo"
        holder.txtEstado.text = estado.uppercase()

        // 🔥 Color del estado según condición (resuelto / activo / visto)
        if (estado.equals("resuelto", ignoreCase = true)) {
            holder.txtEstado.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.txtEstado.setTextColor(Color.parseColor("#C62828"))
        }

        // 🔥 Cambio de estado con actualización en Firestore/BD
        holder.txtEstado.setOnClickListener {
            val opciones = arrayOf("Perdido", "Visto", "Resuelto")

            android.app.AlertDialog.Builder(holder.itemView.context)
                .setTitle("Cambiar estado")
                .setItems(opciones) { _, index ->
                    val nuevoEstado = opciones[index]
                    val repository = ReporteRepository()

                    repository.actualizarEstado(
                        reporteId = reporte.id,
                        nuevoEstado = nuevoEstado,
                        onSuccess = {
                            // Actualización inmediata en UI sin recargar lista
                            holder.txtEstado.text = nuevoEstado.uppercase()

                            val color = when (nuevoEstado) {
                                "Resuelto" -> Color.parseColor("#4CAF50")
                                "Visto" -> Color.parseColor("#0099FF")
                                else -> Color.parseColor("#C62828")
                            }

                            holder.txtEstado.setTextColor(color)
                        },
                        onError = {
                            Toast.makeText(
                                holder.itemView.context,
                                "Error al actualizar: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
                .show()
        }

        // 🔥 Carga de imagen principal del reporte (Cloudinary)
        if (!reporte.imagenesUrl.isNullOrEmpty()) {
            val primeraImagenUrl = reporte.imagenesUrl.first()

            Glide.with(holder.itemView.context)
                .load(primeraImagenUrl)
                .centerCrop()
                .placeholder(R.drawable.images)
                .error(R.drawable.images)
                .into(holder.imgMascota)
        } else {
            // Imagen por defecto si no hay fotos
            holder.imgMascota.setImageResource(R.drawable.images)
        }
    }

    override fun getItemCount(): Int = listaReportes.size

    // Actualiza lista desde ViewModel / Firestore
    fun actualizarLista(nuevaLista: List<Reporte>) {
        this.listaReportes = nuevaLista
        notifyDataSetChanged()
    }
}
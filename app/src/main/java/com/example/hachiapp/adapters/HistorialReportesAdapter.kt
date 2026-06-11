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

        // ── Enlace de datos con los nombres exactos de tu modelo ──
        holder.txtNombre.text = reporte.nombreMascota
        holder.txtFecha.text = "Perdido el: ${reporte.fechaExtravio}"

        // Si tienes la dirección guardada la pone, si no, un texto de respaldo
        holder.txtUbicacion.text = if (!reporte.direccion.isNullOrEmpty()) {
            "${reporte.direccion}"
        } else {
            "Ubicación sin dirección"
        }

        // Asignamos el estado de la mascota (activo o resuelto)
        val estado = reporte.estadoMascota ?: "Activo"
        holder.txtEstado.text = estado.uppercase()

        // Cambiar color del texto del estado según corresponda
        if (estado.equals("resuelto", ignoreCase = true)) {
            holder.txtEstado.setTextColor(Color.parseColor("#4CAF50")) // Verde
        } else {
            holder.txtEstado.setTextColor(Color.parseColor("#C62828")) // Rojo
        }
            /*Al tocar el texto del estado se muestra
             * un menú con las opciones disponibles.
             */
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
                            /*Se actualiza visualmente el estado
                             * sin necesidad de recargar toda la lista.
                             */
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

        // ── Carga de imagen desde la lista de URLs de Cloudinary ──
        if (!reporte.imagenesUrl.isNullOrEmpty()) {
            // Tomamos la primera foto de la lista que subió el usuario
            val primeraImagenUrl = reporte.imagenesUrl.first()

            Glide.with(holder.itemView.context)
                .load(primeraImagenUrl)
                .centerCrop()
                .placeholder(R.drawable.images) // Imagen por defecto mientras carga
                .error(R.drawable.images)       // Imagen si ocurre un error
                .into(holder.imgMascota)
        } else {
            // Si el reporte no tiene fotos, dejamos la de por defecto
            holder.imgMascota.setImageResource(R.drawable.images)
        }
    }

    override fun getItemCount(): Int = listaReportes.size

    fun actualizarLista(nuevaLista: List<Reporte>) {
        this.listaReportes = nuevaLista
        notifyDataSetChanged()
    }
}
package com.example.hachiapp.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hachiapp.R
import com.example.hachiapp.models.Reporte

class ReporteAdapter(
    private val lista: List<Reporte>
) : RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder>() {

    class ReporteViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val imgMascota: ImageView =
            itemView.findViewById(R.id.imgMascota)

        val txtNombre: TextView =
            itemView.findViewById(R.id.txtNombre)

        val txtFecha: TextView =
            itemView.findViewById(R.id.txtFecha)

        val txtTipo: TextView =
            itemView.findViewById(R.id.txtTipo)

        val txtVistas: TextView =
            itemView.findViewById(R.id.txtVistas)

        val btnDetalle: Button =
            itemView.findViewById(R.id.btnDetalle)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReporteViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reporte, parent, false)

        return ReporteViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ReporteViewHolder,
        position: Int
    ) {

        val reporte = lista[position]

        // Nombre
        holder.txtNombre.text = reporte.nombreMascota

        // Fecha
        holder.txtFecha.text = reporte.fechaExtravio

        // Tipo de reporte
        holder.txtTipo.text = reporte.tipoReporte

        // Color según tipo
        when (reporte.tipoReporte.lowercase()) {

            "perdido" -> {
                holder.txtTipo.setTextColor(
                    Color.parseColor("#C62828")
                )
            }

            "visto" -> {
                holder.txtTipo.setTextColor(
                    Color.parseColor("#4527A0")
                )
            }

            "encontrado" -> {
                holder.txtTipo.setTextColor(
                    Color.parseColor("#2E7D32")
                )
            }
        }

        // Vistas falsas de ejemplo
        holder.txtVistas.text = "👁 200"

        // Imagen
        if (reporte.imagenes.isNotEmpty()) {

            Glide.with(holder.itemView.context)
                .load(reporte.imagenes[0])
                .placeholder(R.drawable.images)
                .into(holder.imgMascota)

        } else {

            holder.imgMascota.setImageResource(R.drawable.images)
        }

        // Botón detalles
        holder.btnDetalle.setOnClickListener {

            // luego aquí abrirás detalle
        }
    }

    override fun getItemCount(): Int {
        return lista.size
    }
}

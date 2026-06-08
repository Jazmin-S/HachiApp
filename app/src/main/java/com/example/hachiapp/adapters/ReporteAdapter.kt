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
    private val lista: List<Reporte>,  // Lista de reportes a mostrar
    private val onDetalleClick: (Reporte) -> Unit = {}  // Callback al hacer clic en detalle
) : RecyclerView.Adapter<ReporteAdapter.ReporteViewHolder>() {

    // ViewHolder que guarda las vistas de cada elemento
    class ReporteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMascota : ImageView = itemView.findViewById(R.id.imgMascota)
        val txtNombre  : TextView  = itemView.findViewById(R.id.txtNombre)
        val txtFecha   : TextView  = itemView.findViewById(R.id.txtFecha)
        val txtTipo    : TextView  = itemView.findViewById(R.id.txtTipo)
        val txtVistas  : TextView  = itemView.findViewById(R.id.txtVistas)
        val btnDetalle : Button    = itemView.findViewById(R.id.btnDetalle)
    }

    // Infla el layout XML y crea el ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReporteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reporte, parent, false)
        return ReporteViewHolder(view)
    }

    // Asigna los datos a las vistas en la posición indicada
    override fun onBindViewHolder(holder: ReporteViewHolder, position: Int) {
        val reporte = lista[position]

        // Asigna el nombre de la mascota
        holder.txtNombre.text = reporte.nombreMascota

        // Asigna la fecha de extravío
        holder.txtFecha.text = reporte.fechaExtravio

        // Asigna el estado de la mascota
        holder.txtTipo.text = reporte.estadoMascota

        // Cambia el color del texto según el estado
        when (reporte.estadoMascota.lowercase()) {
            "perdido" -> holder.txtTipo.setTextColor(Color.parseColor("#C62828"))// Rojo
            "visto"   -> holder.txtTipo.setTextColor(Color.parseColor("#4527A0"))// Morado
            "encontrado" -> holder.txtTipo.setTextColor(Color.parseColor("#2E7D32"))// Verde
            else      -> holder.txtTipo.setTextColor(Color.parseColor("#333333"))// Gris oscuro
        }

        // Asigna texto fijo de vistas (hardcodeado)
        holder.txtVistas.text = "👁 200"

        // Carga la primera imagen de la lista usando Glide
        if (reporte.imagenesUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(reporte.imagenesUrl[0])   // URL de la primera imagen
                .placeholder(R.drawable.images)   // Imagen mientras carga
                .into(holder.imgMascota)
        } else {
            holder.imgMascota.setImageResource(R.drawable.images) // Imagen por defecto
        }

        // Asigna acción al botón de detalle
        holder.btnDetalle.setOnClickListener {
            onDetalleClick(reporte)
        }
    }
    // Devuelve la cantidad total de elementos
    override fun getItemCount(): Int = lista.size
}
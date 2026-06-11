package com.example.hachiapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hachiapp.Model.Alerta
import com.example.hachiapp.R

class AlertaAdapter(
    private val lista: MutableList<Alerta>,
    // Callback que se ejecuta cuando el usuario toca una alerta
    private val onClick: (Alerta) -> Unit
) : RecyclerView.Adapter<AlertaAdapter.ViewHolder>() {

    // ViewHolder: contiene referencias a las vistas del item para evitar findViewById repetidos
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val titulo = itemView.findViewById<TextView>(R.id.txtTitulo)
        val descripcion = itemView.findViewById<TextView>(R.id.txtDescripcion)
        val icono = itemView.findViewById<ImageView>(R.id.imgTipo)

        // Indicador visual de “no leída”
        val puntoRojo = itemView.findViewById<View>(R.id.puntoRojo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        // Infla el layout de cada alerta (item de la lista)
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alerta, parent, false)

        return ViewHolder(vista)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val alerta = lista[position]

        // Asigna datos básicos al item
        holder.titulo.text = alerta.titulo
        holder.descripcion.text = alerta.descripcion

        // Lógica importante: cambia el ícono según el tipo de alerta
        when (alerta.tipo) {

            "mensaje" ->
                holder.icono.setImageResource(R.drawable.mensaje)

            "reporte" ->
                holder.icono.setImageResource(R.drawable.reportar)

            "avistamiento" ->
                holder.icono.setImageResource(R.drawable.mapa)

            "actualizacion" ->
                holder.icono.setImageResource(R.drawable.actualizaciones)

            "sistema" ->
                holder.icono.setImageResource(R.drawable.alerta)
        }

        // Punto rojo: indica si la alerta NO ha sido leída
        holder.puntoRojo.visibility =
            if (alerta.leida) View.INVISIBLE else View.VISIBLE

        // Click en la alerta -> se ejecuta acción definida desde la Activity
        holder.itemView.setOnClickListener {
            onClick(alerta)
        }
    }
}
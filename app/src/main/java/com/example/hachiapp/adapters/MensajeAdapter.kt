package com.example.hachiapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hachiapp.R
import com.example.hachiapp.models.Mensaje
import com.google.firebase.auth.FirebaseAuth

class MensajeAdapter(
    private val listaMensajes: List<Mensaje>
) : RecyclerView.Adapter<MensajeAdapter.ViewHolder>() {

    companion object {
        private const val MENSAJE_RECIBIDO = 0
        private const val MENSAJE_ENVIADO = 1
    }

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val txtMensaje: TextView =
            itemView.findViewById(R.id.txtMensaje)
    }

    override fun getItemViewType(position: Int): Int {

        val uidActual =
            FirebaseAuth.getInstance().currentUser?.uid

        return if (
            listaMensajes[position].remitenteId == uidActual
        ) {
            MENSAJE_ENVIADO
        } else {
            MENSAJE_RECIBIDO
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val layout = if (
            viewType == MENSAJE_ENVIADO
        ) {
            R.layout.item_mensaje_enviado
        } else {
            R.layout.item_mensaje_recibido
        }

        val view = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.txtMensaje.text =
            listaMensajes[position].mensaje
    }

    override fun getItemCount(): Int {
        return listaMensajes.size
    }
}
package com.example.hachiapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

        val imgMensaje: ImageView =
            itemView.findViewById(R.id.imgMensaje)
    }

    // 🔥 Determina si el mensaje es enviado o recibido según el UID actual
    override fun getItemViewType(position: Int): Int {

        val uidActual =
            FirebaseAuth.getInstance()
                .currentUser?.uid

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

        // 🔥 Se elige el layout según si el mensaje es mío o recibido
        val layout =
            if (viewType == MENSAJE_ENVIADO) {
                R.layout.item_mensaje_enviado
            } else {
                R.layout.item_mensaje_recibido
            }

        val view =
            LayoutInflater.from(parent.context)
                .inflate(
                    layout,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val mensaje =
            listaMensajes[position]

        // 🔥 Si el mensaje contiene imagen, se muestra imagen en lugar de texto
        if (mensaje.imagenUrl.isNotEmpty()) {

            holder.txtMensaje.visibility =
                View.GONE

            holder.imgMensaje.visibility =
                View.VISIBLE

            Glide.with(holder.itemView.context)
                .load(mensaje.imagenUrl)
                .into(holder.imgMensaje)

        } else {

            // Mensaje de texto normal
            holder.imgMensaje.visibility =
                View.GONE

            holder.txtMensaje.visibility =
                View.VISIBLE

            holder.txtMensaje.text =
                mensaje.mensaje
        }
    }

    override fun getItemCount(): Int {
        return listaMensajes.size
    }
}
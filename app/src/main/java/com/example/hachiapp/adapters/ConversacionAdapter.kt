package com.example.hachiapp.adapters

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hachiapp.R
import com.example.hachiapp.models.Conversacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConversacionAdapter(
    private val lista: MutableList<Conversacion>,
    private val onClick: (Conversacion) -> Unit
) : RecyclerView.Adapter<ConversacionAdapter.ViewHolder>() {

    // UID del usuario actual para identificar si el mensaje es mío o recibido
    private val uidActual = FirebaseAuth.getInstance().currentUser?.uid

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.txtNombre)
        val tvUltimoMensaje: TextView = view.findViewById(R.id.txtUltimoMensaje)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conv = lista[position]

        // Nombre mostrado en la conversación
        holder.tvNombre.text = "Dueño de ${conv.nombreMascota}"

        // 🔥 Diferenciar mensaje enviado vs recibido
        if (conv.ultimoRemitenteId == uidActual) {
            holder.tvUltimoMensaje.text = "Tú: ${conv.ultimoMensaje}"
            holder.tvUltimoMensaje.setTextColor(Color.parseColor("#888888"))
            holder.tvUltimoMensaje.setTypeface(null, Typeface.NORMAL)
        } else {
            holder.tvUltimoMensaje.text = conv.ultimoMensaje
            holder.tvUltimoMensaje.setTextColor(Color.parseColor("#000000"))
            holder.tvUltimoMensaje.setTypeface(null, Typeface.BOLD)
        }

        // Abrir conversación al tocar item
        holder.itemView.setOnClickListener { onClick(conv) }

        // 🔥 Eliminar conversación con confirmación
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Eliminar conversación")
                .setMessage("¿Deseas eliminar esta conversación con el dueño de ${conv.nombreMascota}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    eliminarConversacion(conv)
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }
    }

    override fun getItemCount() = lista.size

    // 🔥 Elimina mensajes de Firestore relacionados a esta conversación
    private fun eliminarConversacion(conv: Conversacion) {
        val db = FirebaseFirestore.getInstance()

        db.collection("mensajes")
            .whereEqualTo("reporteId", conv.reporteId)
            .get()
            .addOnSuccessListener { docs ->

                val batch = db.batch()

                for (doc in docs) {
                    val rem = doc.getString("remitenteId") ?: ""
                    val des = doc.getString("destinatarioId") ?: ""

                    // Solo borra mensajes de esta conversación específica
                    if (rem == conv.otroUsuarioId || des == conv.otroUsuarioId) {
                        batch.delete(doc.reference)
                    }
                }

                batch.commit()
            }
    }
}
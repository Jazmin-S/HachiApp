package com.example.hachiapp.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.hachiapp.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

/*
 * Clase encargada de mostrar una ventana personalizada
 * al tocar un marcador en el mapa.
 * Muestra la imagen, nombre y estado de la mascota.
 */
class ReporteInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    /*Se guarda un caché de imágenes ya descargadas
     * para mostrarlas sin recargar cada vez.
     */
    private val imageCache = HashMap<String, Bitmap>()

    override fun getInfoWindow(marker: Marker): View? = null

    /*
     * Infla el layout personalizado y llena los datos
     * del marcador en la ventana emergente.
     * Parámetros:
     * 1. marker: Marcador que fue tocado por el usuario.
     */
    override fun getInfoContents(marker: Marker): View {

        /*Se infla el layout XML del InfoWindow
         * para usarlo como vista personalizada.
         */
        val view = LayoutInflater.from(context)
            .inflate(R.layout.info_window_reporte, null)

        val imgInfoWindow = view.findViewById<ImageView>(R.id.imgInfoWindow)
        val txtNombre = view.findViewById<TextView>(R.id.txtNombreInfo)
        val txtEstado = view.findViewById<TextView>(R.id.txtEstadoInfo)

        /*Se obtienen los datos guardados en el tag del marcador.*/
        val datos = marker.tag as? Map<*, *>

        // Asignación de Nombre seguro
        txtNombre.text = datos?.get("nombre") as? String ?: marker.title

        /* * Asignación de Estado seguro
         * Recuperamos el string de estado, quitamos los espacios en blanco (.trim())
         * y si resulta estar vacío o nulo, le ponemos un valor por defecto "Perdido".
         */
        val estadoRaw = datos?.get("estado") as? String ?: ""
        val estadoValidado = if (estadoRaw.trim().isEmpty()) "Visto" else estadoRaw

        txtEstado.text = "Estado: $estadoValidado"

        val imagenUrl = datos?.get("imagenesUrl") as? String

        if (!imagenUrl.isNullOrEmpty()) {
            /*Si la imagen ya fue descargada antes
             * se usa la copia en caché directamente.
             */
            val cached = imageCache[imagenUrl]
            if (cached != null) {
                imgInfoWindow.setImageBitmap(cached)
            } else {
                /*Si no está en caché se descarga con Glide
                 * y cuando termina refresca el InfoWindow.
                 */
                Glide.with(context)
                    .asBitmap()
                    .load(imagenUrl)
                    .centerCrop()
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            /*Se guarda la imagen en caché
                             * y se muestra el marcador actualizado.
                             */
                            imageCache[imagenUrl] = resource
                            if (marker.isInfoWindowShown) {
                                marker.showInfoWindow()
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })

                /*Mientras carga muestra el ícono por defecto.*/
                imgInfoWindow.setImageResource(R.drawable.iconoprincipal)
            }
        }

        return view
    }
}
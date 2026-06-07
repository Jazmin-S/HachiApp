package com.example.hachiapp.BD

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryManager {

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return

        val config = HashMap<String, String>()
        config["cloud_name"] = "ddsvqtm52"

        MediaManager.init(context, config)

        initialized = true
    }
}
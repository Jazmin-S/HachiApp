package com.example.hachiapp.models

data class Usuario (
        val nombre: String = "",
        val apellidos: String = "",
        val correo: String = "",
        val telefono: String = "",
        val edad: String = "",
        val descripcion: String = "",
        val fotoPerfil: String = "",
        val fechaRegistro: Any? = null
)
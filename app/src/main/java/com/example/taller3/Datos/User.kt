package com.example.taller3.Datos

data class User(
    val userId: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val identificacion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)
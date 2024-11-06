package com.example.taller3.Datos

data class User(
    var userId: String = "",
    var nombre: String = "",
    var apellido: String = "",
    var identificacion: String = "",
    var latitud: Double = 0.0,
    var longitud: Double = 0.0
)
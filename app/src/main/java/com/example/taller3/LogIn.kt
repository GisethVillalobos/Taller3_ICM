package com.example.taller3

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class LogIn : AppCompatActivity() {

    private lateinit var usuario: EditText
    private lateinit var contrasena: EditText
    private lateinit var iniciarSesion: Button
    private lateinit var registrarse: TextView
    private lateinit var intentRegister: Intent
    private lateinit var intentPrincipal: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_in)

        usuario = findViewById(R.id.usernameField)
        contrasena = findViewById(R.id.passwordField)
        iniciarSesion = findViewById(R.id.logInButton)
        registrarse = findViewById(R.id.registerText)

        registrarse.paintFlags = registrarse.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        intentRegister = Intent(this, Register::class.java)
        intentPrincipal = Intent(this, Principal::class.java)

        registrarse.setOnClickListener {
            startActivity(intentRegister)
        }

        iniciarSesion.setOnClickListener {
            validateFields()
        }
    }

    private fun validateFields() {
        val username = usuario.text.toString().trim()
        val password = contrasena.text.toString().trim()

        // Check if fields are empty
        if (username.isEmpty()) {
            usuario.error = "Usuario es obligatorio"
            usuario.requestFocus()
            return
        }

        if (password.isEmpty()) {
            contrasena.error = "Contraseña es obligatoria"
            contrasena.requestFocus()
            return
        }

        intentPrincipal.putExtra("username", username)
        intentPrincipal.putExtra("password", password)
        startActivity(intentPrincipal)
    }

}
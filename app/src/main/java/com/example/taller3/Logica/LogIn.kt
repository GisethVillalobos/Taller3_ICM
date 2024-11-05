package com.example.taller3.Logica

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

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

        // Firebase
        auth = Firebase.auth

        usuario = findViewById(R.id.usernameField)
        contrasena = findViewById(R.id.passwordField)
        iniciarSesion = findViewById(R.id.logInButton)
        registrarse = findViewById(R.id.registerText)

        registrarse.paintFlags = registrarse.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Intents
        intentRegister = Intent(this, Register::class.java)
        intentPrincipal = Intent(this, Principal::class.java)

        registrarse.setOnClickListener {
            startActivity(intentRegister)
        }

        iniciarSesion.setOnClickListener {
            val email = usuario.text.toString()
            val password = contrasena.text.toString()

            if (validateForm() && isEmailValid(email)) {
                signInUser(email, password)
            } else {
                Toast.makeText(this, "Por favor, ingrese un correo y contraseña válidos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            intentPrincipal.putExtra("user", currentUser.email)
            startActivity(intentPrincipal)
        } else {
            usuario.setText("")
            contrasena.setText("")
        }
    }

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // Sign-in failed
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Falló la autenticación", Toast.LENGTH_LONG).show()
                    updateUI(null)
                }
            }
    }

    // Form validation
    private fun validateForm(): Boolean {
        var valid = true

        val email = usuario.text.toString()
        if (TextUtils.isEmpty(email)) {
            usuario.error = "Required."
            valid = false
        } else {
            usuario.error = null
        }

        val password = contrasena.text.toString()
        if (TextUtils.isEmpty(password)) {
            contrasena.error = "Required."
            valid = false
        } else {
            contrasena.error = null
        }

        return valid
    }

    // Email validation
    private fun isEmailValid(email: String): Boolean {
        if (!email.contains("@") ||
            !email.contains(".") ||
            email.length < 5)
            return false
        return true
    }
}

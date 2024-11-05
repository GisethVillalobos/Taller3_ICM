package com.example.taller3.Logica

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.taller3.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Delay for 5 seconds
        Handler().postDelayed({
            // Start the new activity
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            finish()
        }, 5000)
    }
}
package com.example.taller3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Register : AppCompatActivity() {

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }

    private lateinit var editTextName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextIdentification: EditText
    private lateinit var imageViewContact: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var buttonSubmit: Button

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextName = findViewById(R.id.editTextName)
        editTextLastName = findViewById(R.id.editTextLastName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextIdentification = findViewById(R.id.editTextIdentification)
        imageViewContact = findViewById(R.id.imageViewContact)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonSubmit = findViewById(R.id.buttonSubmit)

        buttonSelectImage.setOnClickListener {
            selectImage()
        }

        buttonSubmit.setOnClickListener {
            submitForm()
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    @Deprecated("deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            imageViewContact.setImageURI(selectedImageUri)
        }
    }

    private fun submitForm() {
        val name = editTextName.text.toString()
        val lastName = editTextLastName.text.toString()
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()
        val identification = editTextIdentification.text.toString()

        if (name.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || identification.isEmpty()) {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Handle form submission logic here
        Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LogIn::class.java)
        startActivity(intent)
    }
}

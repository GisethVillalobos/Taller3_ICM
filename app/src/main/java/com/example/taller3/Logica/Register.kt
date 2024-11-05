package com.example.taller3.Logica

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import java.io.File

class Register : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
        private const val IMAGE_PICK_CODE = 1000
    }

    private lateinit var auth: FirebaseAuth

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

        auth = Firebase.auth

        editTextName = findViewById(R.id.editTextName)
        editTextLastName = findViewById(R.id.editTextLastName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextIdentification = findViewById(R.id.editTextIdentification)
        imageViewContact = findViewById(R.id.imageViewContact)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)
        buttonSubmit = findViewById(R.id.buttonSubmit)

        buttonSelectImage.setOnClickListener {
            checkPermissionsAndSelectImage()
        }

        buttonSubmit.setOnClickListener {
            submitForm()
        }
    }

    private fun checkPermissionsAndSelectImage() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (cameraPermission != PackageManager.PERMISSION_GRANTED || storagePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            selectImage()
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    @Deprecated("Deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            imageViewContact.setImageURI(selectedImageUri)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                selectImage()
            } else {
                Toast.makeText(this, "Permisos de cámara y almacenamiento denegados", Toast.LENGTH_SHORT).show()
            }
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

        // Lógica para manejar el registro de usuario aquí
        registerUser(name, lastName, email, password, identification)

        Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LogIn::class.java)
        startActivity(intent)
    }

    private fun registerUser(name: String,
                             lastName: String,
                             email: String,
                             password: String,
                             identification: String) {

        val latitude: Double = 0.0
        val longitude: Double = 0.0

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                    val user = auth.currentUser

                    if (user != null) {
                        val userId = user.uid
                        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

                        val userData = hashMapOf(
                            "name" to name,
                            "lastName" to lastName,
                            "email" to email,
                            "identification" to identification,
                            "latitude" to latitude,
                            "longitude" to longitude
                        )

                        userRef.set(userData)
                            .addOnSuccessListener {
                                Log.d(TAG, "User data saved successfully.")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error saving user data", e)
                            }
                    }

                    val intent = Intent(this, LogIn::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "createUserWithEmail:Failure: " + task.exception.toString(),
                        Toast.LENGTH_SHORT).show()
                    task.exception?.message?.let { Log.e(TAG, it) }
                }
            }
    }
}

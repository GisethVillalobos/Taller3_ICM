package com.example.taller3.Logica

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taller3.Datos.Location
import com.example.taller3.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class Principal : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var osmMap: MapView
    private var myLocation = GeoPoint(0.0, 0.0)
    private lateinit var jsonString: String
    private lateinit var locationsList: List<Location>

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                // Permissions granted, proceed with location access
                getLocation()
            } else {
                // Permissions denied
                Toast.makeText(this, "Se requieren los permisos de ubicación", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)

        // Firebase
        auth = Firebase.auth

        // Initialize location client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Setting the map
        osmMap = findViewById(R.id.osmMap)
        setMap() // Initialize map settings

        // Location permissions
        requestLocationPermissions()
    }

    override fun onResume() {
        super.onResume()
        osmMap.onResume()

        val mapController: IMapController = osmMap.controller
        mapController.setZoom(18.0)
        mapController.setCenter(myLocation)
    }
    override fun onPause() {
        super.onPause()
        osmMap.onPause()
    }

    override fun onBackPressed() {
        // Sign out the user
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(this, "Ha cerrado su sesión", Toast.LENGTH_SHORT).show()

        // Call the default back button behavior (finish the activity)
        super.onBackPressed()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuLogOut -> {
                auth.signOut()
                val intent = Intent(this, LogIn::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                true
            }
            R.id.menuPeople -> {

                val intent = Intent(this, ListaUsuarios::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                true
            }
            R.id.menuAvailable -> {
                setAvailable()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setAvailable() {
        Toast.makeText(this, "Funcion para validar si está en línea o no", Toast.LENGTH_SHORT).show()
    }

    private fun requestLocationPermissions() {
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED &&
            coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            // Request permissions
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            // Permissions are already granted
            getLocation()
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        } else {
            mFusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    myLocation = GeoPoint(location.latitude, location.longitude)
                    markPoint(myLocation, "Mi Ubicación") // Now mark the location
                    osmMap.controller.setCenter(myLocation) // Center the map on the location
                    markOtherPoints() // Now you can safely mark other points
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setMap() {
        Configuration.getInstance().userAgentValue = "com.example.taller3"

        osmMap.setTileSource(TileSourceFactory.MAPNIK)
        osmMap.setMultiTouchControls(true)
        Log.d("MapSetup", "Setting map center to: $myLocation")
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun markPoint(point: GeoPoint, name: String) {
        val marker = Marker(osmMap)
        marker.title = name
        val myIcon = ContextCompat.getDrawable(this, R.drawable.baseline_location_pin_24)
        marker.icon = myIcon
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        osmMap.overlays.add(marker)
    }


    private fun markOtherPoints() {
        jsonString = readJsonFromAssets(this, "locations.json") ?: return // Exit if null
        locationsList = parseLocations(jsonString)

        for (location in locationsList) {
            val geoPoint = GeoPoint(location.latitude, location.longitude)
            markPoint(geoPoint, location.name)
        }
    }

    private fun parseLocations(json: String): List<Location> {
        val gson = Gson()
        val type = object : TypeToken<List<Location>>() {}.type
        return gson.fromJson(json, type)
    }



    private fun readJsonFromAssets(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
package com.dianca.synced

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class GeoLocationActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var locationSwitch: Switch
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var locationOverlay: MyLocationNewOverlay
    private var myMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getSharedPreferences("osm_prefs", MODE_PRIVATE))
        setContentView(R.layout.activity_geolocation)

        // Init views
        map = findViewById(R.id.osmMap)
        locationSwitch = findViewById(R.id.switchShareLocation)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupMap()

        // Location sharing switch
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) startSharingLocation() else stopSharingLocation()
        }

        listenForOtherUsers()

        // Bottom navigation setup
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, TopMatchesActivity::class.java))
                    true
                }

                R.id.nav_geo -> {
                    // Already here
                    true
                }

                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }

                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                R.id.nav_help -> {
                    startActivity(Intent(this, HelpActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    private fun setupMap() {
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)

        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        map.overlays.add(locationOverlay)

        val compassOverlay = CompassOverlay(this, InternalCompassOrientationProvider(this), map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)
    }

    private fun startSharingLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        Toast.makeText(this, "Location sharing enabled", Toast.LENGTH_SHORT).show()

        val locationProvider = GpsMyLocationProvider(this)
        locationProvider.startLocationProvider { location, _ ->
            location?.let {
                val uid = auth.currentUser?.uid ?: return@let
                val userName = auth.currentUser?.displayName ?: auth.currentUser?.email ?: uid

                val data = hashMapOf(
                    "latitude" to it.latitude,
                    "longitude" to it.longitude,
                    "name" to userName
                )
                firestore.collection("locations").document(uid).set(data)

                // Center map on user's location
                val geoPoint = GeoPoint(it.latitude, it.longitude)
                map.controller.setCenter(geoPoint)

                // Add or update my marker
                if (myMarker == null) {
                    myMarker = Marker(map)
                    myMarker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    map.overlays.add(myMarker)
                }
                myMarker!!.position = geoPoint
                myMarker!!.title = userName
                map.invalidate()
            }
        }
    }

    private fun stopSharingLocation() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("locations").document(uid).delete()
        Toast.makeText(this, "Location sharing disabled", Toast.LENGTH_SHORT).show()

        // Remove your marker from map
        myMarker?.let {
            map.overlays.remove(it)
            myMarker = null
            map.invalidate()
        }
    }

    private fun listenForOtherUsers() {
        firestore.collection("locations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading map data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                snapshot?.let { updateMapMarkers(it) }
            }
    }

    private fun updateMapMarkers(snapshot: QuerySnapshot) {
        // Keep only the current user's location overlay (MyLocationNewOverlay)
        val overlaysToKeep = map.overlays.filterIsInstance<MyLocationNewOverlay>().toMutableList()

        // Clear all overlays and add only the location overlays
        map.overlays.clear()
        map.overlays.addAll(overlaysToKeep)

        // Add the current user's marker if it exists
        myMarker?.let { map.overlays.add(it) }

        // Add markers for other users
        for (doc in snapshot.documents) {
            val uid = doc.id
            if (uid == auth.currentUser?.uid) continue // skip self

            val lat = doc.getDouble("latitude") ?: continue
            val lon = doc.getDouble("longitude") ?: continue
            val name = doc.getString("name") ?: continue

            // Only add marker if the user is actively sharing
            if (name.isBlank()) continue

            val marker = Marker(map)
            marker.position = GeoPoint(lat, lon)
            marker.title = name
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            map.overlays.add(marker)
        }

        map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
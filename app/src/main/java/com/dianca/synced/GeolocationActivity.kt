package com.dianca.synced

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class GeoLocationActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var switchShareLocation: Switch
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbRef: DatabaseReference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geolocation)

        // Initialize map and Firebase
        Configuration.getInstance().userAgentValue = packageName
        map = findViewById(R.id.osmMap)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        switchShareLocation = findViewById(R.id.switchShareLocation)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        dbRef = FirebaseDatabase.getInstance().getReference("UserLocations")

        requestLocationPermission()

        // Load other users' locations in realtime
        listenForOtherUsers()

        switchShareLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) shareLocation() else stopSharing()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    private fun shareLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                val geoPoint = GeoPoint(lat, lon)

                // Center map on user
                map.controller.setZoom(15.0)
                map.controller.setCenter(geoPoint)

                // Add marker for user
                val marker = Marker(map)
                marker.position = geoPoint
                marker.title = "You"
                map.overlays.add(marker)

                // Upload to Firebase
                val userLoc = mapOf("latitude" to lat, "longitude" to lon)
                dbRef.child(userId).setValue(userLoc)
            }
        }
    }

    private fun stopSharing() {
        dbRef.child(userId).removeValue()
    }

    private fun listenForOtherUsers() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                map.overlays.clear()

                for (child in snapshot.children) {
                    val uid = child.key ?: continue
                    val lat = child.child("latitude").getValue(Double::class.java) ?: continue
                    val lon = child.child("longitude").getValue(Double::class.java) ?: continue

                    val marker = Marker(map)
                    marker.position = GeoPoint(lat, lon)
                    marker.title = if (uid == userId) "You" else "User $uid"
                    map.overlays.add(marker)
                }

                map.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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

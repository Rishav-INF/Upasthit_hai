package com.example.upasthithai

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.upasthithai.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Date

private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
private const val CHANNEL_ID = "location_updates"
private const val FIXED_LOCATION_RADIUS_METERS = 70
private const val STAY_TIME_THRESHOLD_MS = 5 * 60 * 1000 // 5 minutes
private const val STAY_TIME_THRESHOLD_enters = 10 * 1000 // 10 seconds
private const val STAY_TIME_THRESHOLD_exits = 20 * 1000 // 20 seconds

private val FIXED_LOCATION = LatLng(20.222601, 85.733929) // Example fixed location

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private  var database: DatabaseReference = FirebaseDatabase.getInstance().getReference("organizations")
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var username : String
    private lateinit var password : String
    private lateinit var employee_id : String
    private lateinit var officeType : String
    private var intime : Long = 0
    private var outime : Long = 0

    private var isInsideRadius = false
    private var lastNotificationTimefivemins: Long = 0
    private var lastNotificationTimeenters: Long = 0
    private var lastNotificationTimeexits: Long = 0

    private var enteredRadiusTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // Get stored values
        username = sharedPreferences.getString("username", null).toString() // Default is null if not found
        password = sharedPreferences.getString("password", null).toString()
        employee_id = sharedPreferences.getString("employee_id", null).toString()
        officeType = sharedPreferences.getString("office_type", null).toString()

        // Set up the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLocationRequest()
        setupLocationCallback()
        checkLocationPermissions()

        // Create notification channel
        createNotificationChannel()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable blue dot for current location
        if (checkLocationPermission()) {
            mMap.isMyLocationEnabled = true
        }

        // Move camera to a default location until we get the real location
        val defaultLocation = LatLng(-34.0, 151.0) // Example location (Sydney)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))

        // Set up long-click listener to add a marker on the map
        mMap.setOnMapLongClickListener { latLng ->
            mMap.addMarker(MarkerOptions().position(latLng).title("Marker"))
        }

        // Draw circle around the fixed location
        drawCircleAroundFixedLocation()
    }

    private fun drawCircleAroundFixedLocation() {
        val circleOptions = CircleOptions()
            .center(FIXED_LOCATION)
            .radius(FIXED_LOCATION_RADIUS_METERS.toDouble())
            .strokeColor(0xFFFF0000.toInt()) // Red border
            .fillColor(0x40FF0000) // Semi-transparent red fill
            .strokeWidth(5f) // Border width
        mMap.addCircle(circleOptions)
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    updateMapWithLocation(location)
                    checkProximityToFixedLocation(location)
                }
            }
        }
    }

    private fun updateMapWithLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun checkProximityToFixedLocation(location: Location) {
        val userLocation = LatLng(location.latitude, location.longitude)
        val distance = FloatArray(1)
        Location.distanceBetween(
            userLocation.latitude, userLocation.longitude,
            FIXED_LOCATION.latitude, FIXED_LOCATION.longitude,
            distance
        )


        val currentTime = System.currentTimeMillis()

        if (distance[0] < FIXED_LOCATION_RADIUS_METERS) {
            if (!isInsideRadius) {
                // Entered radius
                isInsideRadius = true
                enteredRadiusTime = currentTime
               // Log.d("LODA", "Entered radius at: $currentTime")
                intime = currentTime // Start tracking time inside the radius

                sendNotification("You are within 200 meters of the fixed location!")
                lastNotificationTimeenters = currentTime
            }
        } else {
            if (isInsideRadius) {
                // Exiting the radius
                isInsideRadius  = false
                outime = currentTime // Mark the time when exiting the radius
                sendNotification("You have exited the 200-meter radius!")
                // Calculate worked time (in milliseconds)

                database.child("organization_id_1")
                    .child("offices").child(officeType)
                    .child("employees").child(employee_id)
                    .child("hours_worked")
                    .get()
                    .addOnSuccessListener { dataSnapshot ->
                        var prevworkedtime = (dataSnapshot.getValue(Long::class.java) ?: 0L)
                        // Use prevworkedtime here
                        var workedTimeSec = prevworkedtime + ((outime - intime) / 1000)

                        if (officeType != null && username != null && password != null) {
                            val employeeRef = database.child("organization_id_1")
                                .child("offices").child(officeType)
                                .child("employees").child(employee_id)

                            employeeRef.get().addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    // Update the hours_worked field with the formatted time
                                    employeeRef.child("hours_worked")
                                        .setValue(workedTimeSec)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Work hours updated", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Failed to update work hours", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }

                    }


                // Convert worked time to hours, minutes, and seconds
//                val hours = (workedTimeMillis / (1000 * 60 * 60)) % 24
//                val minutes = (workedTimeMillis / (1000 * 60)) % 60
//                val seconds = (workedTimeMillis / 1000) % 60

                // Format the time as hh:mm:ss
//                val workedTimeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                // Update Firebase only if valid user details are available

                lastNotificationTimeexits = currentTime
            }
        }
    }




    private fun sendNotification(message: String) {
        // Check for notification permission if Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
                return
            }
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_upasthit_hai) // Replace with your notification icon
            .setContentTitle("Location Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(1001, notificationBuilder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Location Updates"
            val descriptionText = "Channel for location update notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startLocationUpdates() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun checkLocationPermissions() {
        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS // For Android 13+
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startLocationUpdates()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkLocationPermission()) {
                        mMap.isMyLocationEnabled = true
                        startLocationUpdates()
                    }
                } else {
                    Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
                }
            }
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notification permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

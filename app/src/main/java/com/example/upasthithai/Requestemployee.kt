package com.example.upasthithai

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class Requestemployee : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences
    val database = FirebaseDatabase.getInstance("organizations")

    val officeType = sharedPreferences.getString("office_type", null)
    val employee_id = sharedPreferences.getString("employee_id", null)

    val employeeRef = database.reference.child("organization_id_1")
        .child("offices").child(officeType.toString())
        .child("employees").child(employee_id.toString())

    val adminRef = database.reference.child("organization_id_1")
        .child("offices").child(officeType.toString())
        .child("employees").child(employee_id.toString())

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.requestemployee)

        // Initialize the fused location client for location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize SharedPreferences (assuming employee_id is saved at login)
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Set click listeners for the TextView elements
        findViewById<TextView>(R.id.leave).setOnClickListener {
            onRequestLeave("Personal leave reason")
        }

        findViewById<TextView>(R.id.manualcheckin).setOnClickListener {
            onManualCheckIn()
        }

        findViewById<TextView>(R.id.manualcheckout).setOnClickListener {
            onManualCheckOut()
        }

        findViewById<TextView>(R.id.requestsstatus).setOnClickListener {
            onViewPreviousRequests()
        }
    }

    // Function for requesting a manual check-in
    private fun onManualCheckIn() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        // Get the last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val token = UUID.randomUUID().toString()  // Generate a random token
                sendRequestToAdminAndEmployee("requests for manual checkin", location.latitude, location.longitude, token)
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function for requesting a manual check-out
    private fun onManualCheckOut() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        // Get the last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val token = UUID.randomUUID().toString()  // Generate a random token
                sendRequestToAdminAndEmployee("requests for manual checkout", location.latitude, location.longitude, token)
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function for requesting a leave
    private fun onRequestLeave(leaveReason: String) {
        val token = UUID.randomUUID().toString()  // Generate a random token
        sendLeaveRequestToAdminAndEmployee("requests for leave", leaveReason, token)
    }

    // Function to view previous requests
    private fun onViewPreviousRequests() {
        val database = FirebaseDatabase.getInstance()
        val requestsRef = database.getReference("employees/admin_id_1")

        requestsRef.get().addOnSuccessListener { dataSnapshot ->
            val leaveRequests = dataSnapshot.child("requests for leave").value
            val checkinRequests = dataSnapshot.child("requests for manual checkin").value
            val checkoutRequests = dataSnapshot.child("requests for manual checkout").value

            // Display the previous requests status (You can use a dialog, toast, or new activity for UI)
            Toast.makeText(this, "Leave Requests: $leaveRequests\nCheck-in Requests: $checkinRequests\nCheck-out Requests: $checkoutRequests", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to retrieve previous requests", Toast.LENGTH_SHORT).show()
        }
    }

    // General function to send check-in and check-out requests to both admin and employee
    private fun sendRequestToAdminAndEmployee(requestType: String, latitude: Double, longitude: Double, token: String) {
        val request = mapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "token" to token,
            "timestamp" to System.currentTimeMillis()
        )

        val employeeId = sharedPreferences.getString("employee_id", null)  // Retrieve the employee ID from SharedPreferences

        if (employeeId != null) {
            val database = FirebaseDatabase.getInstance()

            // Save request under admin
            val adminRequestsRef = database.getReference("employees/admin_id_1/$requestType/$token")
            adminRequestsRef.setValue(request)

            // Save request under employee
            val employeeRequestsRef = database.getReference("employees/$employeeId/requests/$requestType/$token")
            employeeRequestsRef.setValue(request).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Request sent successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Employee ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to send leave requests to both admin and employee
    private fun sendLeaveRequestToAdminAndEmployee(requestType: String, leaveReason: String, token: String) {
        val request = mapOf(
            "leaveReason" to leaveReason,
            "token" to token,
            "timestamp" to System.currentTimeMillis()
        )

        val employeeId = sharedPreferences.getString("employee_id", null)  // Retrieve the employee ID from SharedPreferences

        if (employeeId != null) {
            val database = FirebaseDatabase.getInstance()

            // Save request under admin
            val adminRequestsRef = database.getReference("employees/admin_id_1/$requestType/$token")
            adminRequestsRef.setValue(request)

            // Save request under employee
            val employeeRequestsRef = database.getReference("employees/$employeeId/requests/$requestType/$token")
            employeeRequestsRef.setValue(request).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Leave request sent successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send leave request", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Employee ID not found", Toast.LENGTH_SHORT).show()
        }
    }
}

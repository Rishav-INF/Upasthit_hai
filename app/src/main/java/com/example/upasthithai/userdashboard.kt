package com.example.upasthithai

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class userdashboard : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.userstats)

        val requests = findViewById<TextView>(R.id.Requests)
        val map = findViewById<TextView>(R.id.LiveMap)
        val hoursWorkedTextView = findViewById<TextView>(R.id.timefetched)
//         Convert worked time to hours, minutes, and seconds

        // Navigate to the MapsActivity when button is clicked
        map.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        requests.setOnClickListener {
            val intent = Intent(this, Requestemployee::class.java)
            startActivity(intent)
        }

        // Get shared preferences
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val hours_worked = sharedPreferences.getString("hours_worked",null).toString()
        val hours = hours_worked.toLong() / 3600
        val minutes = (hours_worked.toLong() % 3600) / 60
        val seconds = hours_worked.toLong() % 60

        val workedTimeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        // Fetch the "hours_worked" value from Firebase

                // Set the fetched value in the TextView
                hoursWorkedTextView.text = workedTimeFormatted
            }

    }


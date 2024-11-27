package com.example.upasthithai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.upasthithai.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var userTypeSpinner: Spinner
    private lateinit var officeTypeSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val loginName = findViewById<EditText>(R.id.loginnametext)
        val loginPassword = findViewById<EditText>(R.id.loginpasswordtext)
        val loginBtn = findViewById<Button>(R.id.loginbtn)
        userTypeSpinner = findViewById(R.id.user_type_spinner)
        officeTypeSpinner = findViewById(R.id.office_type_spinner)

        val direct = findViewById<Button>(R.id.Tester)
        direct.setOnClickListener()
        {
            val intent = Intent(this,userdashboard::class.java)
            startActivity(intent)
            Toast.makeText(this,"tester login",Toast.LENGTH_SHORT)
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.user_types_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            userTypeSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.office_types_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            officeTypeSpinner.adapter = adapter
        }

        database = FirebaseDatabase.getInstance().getReference("organizations")

        loginBtn.setOnClickListener {
            val name = loginName.text.toString().trim()
            var hours_worked : Long = 0
            val password = loginPassword.text.toString().trim()
            val userType = userTypeSpinner.selectedItem.toString()
            val officeType = officeTypeSpinner.selectedItem.toString()
            val user_id : String


            if (name.isEmpty() || password.isEmpty() || userType.isEmpty() || officeType.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("username", name)
            editor.putString("password", password)
            editor.putString("user_type", userType)
            editor.putString("office_type", officeType)
            editor.apply()


            // Construct the path dynamically if needed
            val organizationId = "organization_id_1"  // Replace with your logic for determining the org ID
              // You might want to determine this dynamically based on user input or other logic

            database.child(organizationId).child("offices").child(officeType).child("employees").get().addOnSuccessListener { snapshot ->
                var userFound = false

                // Check if the snapshot exists
                if (snapshot.exists()) {
                    for (employeeSnapshot in snapshot.children) {
                        val fetchedName = employeeSnapshot.child("name").getValue(String::class.java)
                        val fetchedPassword = employeeSnapshot.child("password").getValue(String::class.java)

                        if (fetchedName == name && fetchedPassword == password) {
                            hours_worked = employeeSnapshot.child("hours_worked").getValue(Long::class.java) ?: 0L
                            userFound = true
                            val employeeId = employeeSnapshot.key
                            Toast.makeText(this, "USER LOGIN SUCCESS", Toast.LENGTH_SHORT).show()

                            editor.putString("username", name)
                            editor.putString("password", password)
                            editor.putString("employee_id", employeeId)
                            editor.putString("hours_worked", hours_worked.toString())
                            editor.apply()
                            val intent = Intent(this, userdashboard::class.java)
                            startActivity(intent)
                            // Navigate to next activity
                            // val intent = Intent(this, EventNavigationActivity::class.java)
                            // startActivity(intent)
                            finish()
                            break
                        }
                    }
                }

                if (!userFound) {
                    Toast.makeText(this, "Invalid login credentials", Toast.LENGTH_SHORT).show()

                }
            }.addOnFailureListener {
                Log.e("LoginActivity", "Failed to retrieve user data", it)
                Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


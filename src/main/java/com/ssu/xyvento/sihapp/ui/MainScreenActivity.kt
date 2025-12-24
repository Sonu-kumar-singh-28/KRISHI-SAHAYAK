package com.ssu.xyvento.sihapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ssu.xyvento.sihapp.R
import com.ssu.xyvento.sihapp.databinding.ActivityMainScreenBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainScreenActivity : AppCompatActivity() {

    private val binding: ActivityMainScreenBinding by lazy {
        ActivityMainScreenBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupBottomNavigation()
        setupCropAdvisoryButton()
        loadUserName()
        setCurrentDate()
    }

    // 🔥 USERNAME FROM FIRESTORE
    private fun loadUserName() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val username = doc.getString("username") ?: "User"
                binding.tvGreeting.text = "Hello $username!"
            }
    }

    // 🔥 CALENDAR DATE
    private fun setCurrentDate() {
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        binding.tvLocation.text = "India\n$currentDate"
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_profile -> {
                    startActivity(Intent(this, User_Profile_Activity::class.java))
                    true
                }

                R.id.nav_home -> true

                else -> false
            }
        }
    }

    private fun setupCropAdvisoryButton() {
        binding.cropAdvisory.setOnClickListener {
            startActivity(Intent(this, chat_screen_Activity::class.java))
        }
    }


}

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

        binding.bottomNavigationView.selectedItemId = R.id.nav_home

        setupBottomNavigation()
        setupButtons()
        loadUserName()
        setCurrentDate()
    }

    private fun loadUserName() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                binding.tvGreeting.text =
                    "Hello ${it.getString("username") ?: "User"}!"
            }
    }

    private fun setCurrentDate() {
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        binding.tvLocation.text = "India\n${sdf.format(Date())}"
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> true

                R.id.nav_profile -> {
                    startActivity(Intent(this, User_Profile_Activity::class.java))
                    true
                }

                R.id.nav_advisory -> {
                    startActivity(Intent(this, chat_screen_Activity::class.java))
                    true
                }

                R.id.nav_market -> {
                    startActivity(Intent(this, MarketPriceActivity::class.java))
                    true
                }

                R.id.nav_SoilFertilizer -> {
                    startActivity(Intent(this, SoilFertilizerActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    private fun setupButtons() {
        binding.cropAdvisory.setOnClickListener {
            startActivity(Intent(this, chat_screen_Activity::class.java))
        }

        binding.soilFertillzer.setOnClickListener {
            startActivity(Intent(this, SoilFertilizerActivity::class.java))
        }

        binding.pestDisease.setOnClickListener {
            startActivity(Intent(this, PestDiseaseActivity::class.java))
        }

        binding.marketPrice.setOnClickListener {
            startActivity(Intent(this, MarketPriceActivity::class.java))
        }
    }
}

package com.ssu.xyvento.sihapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ssu.xyvento.sihapp.R
import com.ssu.xyvento.sihapp.databinding.ActivityMainScreenBinding

class MainScreenActivity : AppCompatActivity() {

    private val binding: ActivityMainScreenBinding by lazy {
        ActivityMainScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupBottomNavigation()
        setupCropAdvisoryButton()
    }

    private fun setupBottomNavigation() {
        val bottomNavigationView = binding.bottomNavigationView

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_profile -> {
                    val intent = Intent(this, User_Profile_Activity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.nav_home -> {
                    true
                }

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

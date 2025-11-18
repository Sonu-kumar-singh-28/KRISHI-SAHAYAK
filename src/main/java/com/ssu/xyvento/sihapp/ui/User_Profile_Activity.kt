package com.ssu.xyvento.sihapp.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ssu.xyvento.sihapp.databinding.ActivityUserProfileBinding

class User_Profile_Activity : AppCompatActivity() {

    private val binding: ActivityUserProfileBinding by lazy {
        ActivityUserProfileBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupBackButton()
    }

    private fun setupBackButton() {
        binding.backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}

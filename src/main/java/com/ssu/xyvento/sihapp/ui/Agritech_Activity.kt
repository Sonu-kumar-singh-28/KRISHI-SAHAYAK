package com.ssu.xyvento.sihapp.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ssu.xyvento.sihapp.R
import com.ssu.xyvento.sihapp.databinding.ActivityAgritechBinding

class Agritech_Activity : AppCompatActivity() {

    private val binding: ActivityAgritechBinding by lazy{
        ActivityAgritechBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
    }


}
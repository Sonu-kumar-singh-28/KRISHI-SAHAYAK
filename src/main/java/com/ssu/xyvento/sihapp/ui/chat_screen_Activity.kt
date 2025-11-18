package com.ssu.xyvento.sihapp.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ssu.xyvento.sihapp.R
import com.ssu.xyvento.sihapp.databinding.ActivityAgritechBinding
import com.ssu.xyvento.sihapp.databinding.ActivityChatScreenBinding
import com.ssu.xyvento.sihapp.databinding.ActivitySplashScreenBinding

class chat_screen_Activity : AppCompatActivity() {

    private  val binding: ActivityChatScreenBinding by lazy {
        ActivityChatScreenBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
    }
    
}
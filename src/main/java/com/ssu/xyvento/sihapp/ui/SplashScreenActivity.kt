package com.ssu.xyvento.sihapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.ssu.xyvento.sihapp.ui.LoginScreenActivity
import com.ssu.xyvento.sihapp.R
import com.ssu.xyvento.sihapp.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {

    private val binding: ActivitySplashScreenBinding by lazy {
        ActivitySplashScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // SplashScreenActivity
        SplashScreenFunction()

        // AnimationScreenFunction
        Animatonfunction()
    }

    private  fun SplashScreenFunction(){
        Handler(Looper.getMainLooper()).postDelayed({
            var intent = Intent(this
            , LoginScreenActivity::class.java)
            startActivity(intent)
            finish()
        },
        1800)
    }


    private  fun  Animatonfunction(){
        val glowupAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val AppImage =  findViewById<ImageView>(R.id.imageView)
        val Tagline = findViewById<TextView>(R.id.txt_tagline)
        val Sponsored = findViewById<TextView>(R.id.txt_sponsored)


        AppImage.startAnimation(glowupAnimation)
        Tagline.startAnimation(glowupAnimation)
        Sponsored.startAnimation(glowupAnimation)
    }
}
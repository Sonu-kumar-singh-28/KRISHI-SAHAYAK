package com.ssu.xyvento.sihapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PackageManagerCompat.LOG_TAG
import com.google.firebase.auth.FirebaseAuth
import com.ssu.xyvento.sihapp.ui.MainScreenActivity
import com.ssu.xyvento.sihapp.R
import com.ssu.xyvento.sihapp.ui.RegisterScreenActivity
import com.ssu.xyvento.sihapp.databinding.ActivityLoginScreenBinding

class LoginScreenActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val binding: ActivityLoginScreenBinding by lazy {
        ActivityLoginScreenBinding.inflate(layoutInflater)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        // GoToRedirectButton
        GoToRegisterScreen()
        // Login Button
        setupLoginButton()
    }


    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            LoginButton()
        }
    }

    // Register button code
    private fun GoToRegisterScreen() {
        val RedirectButtonRegister = binding.signupOption
        RedirectButtonRegister.setOnClickListener {
            val intent = Intent(this, RegisterScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun LoginButton() {
        val emailId = binding.emailtextfield.editText?.text.toString().trim()
        val password = binding.passwordInputField.editText?.text.toString().trim()

        //  input validation
        if (emailId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
            return
        }

        Login(emailId, password)
    }

    private fun Login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(LOG_TAG, "Login Successful")
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainScreenActivity::class.java))
                    finish()
                } else {
                    Log.e(LOG_TAG, "Failure", task.exception)
                    val errorMessage = task.exception?.localizedMessage ?: "Authentication Failed"
                    Toast.makeText(
                        this,
                        "Login Failed: $errorMessage",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
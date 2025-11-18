package com.ssu.xyvento.sihapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ssu.xyvento.sihapp.ui.MainScreenActivity
import com.ssu.xyvento.sihapp.databinding.ActivityRegisterScreenBinding

class RegisterScreenActivity : AppCompatActivity() {
    private val binding: ActivityRegisterScreenBinding by lazy {
        ActivityRegisterScreenBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth

    companion object {
        private const val LOG_TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        GotoLoginScreen()
        RegisterUserForApplication()
        UserAlreadyLogin()
    }

    private fun GotoLoginScreen() {
        val RedirectButton = binding.tvBackToLogin
        RedirectButton.setOnClickListener {
            val intent = Intent(this, LoginScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun RegisterUserForApplication() {
        binding.btnRegister.setOnClickListener {
            val username = binding.nametextfield.editText?.text.toString().trim()
            val emailId = binding.emailtextfield.editText?.text.toString().trim()
            val password = binding.passwordtextfield.editText?.text.toString().trim()
            val repeatPassword = binding.repeattextfield.editText?.text.toString().trim()

            if (username.isEmpty()|| emailId.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Fill input Field", Toast.LENGTH_SHORT).show()
            }

            if (password != repeatPassword) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(emailId, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(LOG_TAG, "Register Successful for: $emailId")
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainScreenActivity::class.java))
                        finish()
                    } else {
                        Log.e(LOG_TAG, "Registration failed", task.exception)
                        val errorMessage = task.exception?.localizedMessage ?: "Authentication Failed"
                        Toast.makeText(
                            this,
                            "Registration Failed: $errorMessage",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    fun UserAlreadyLogin() {
        val currentuser = auth.currentUser
        if (currentuser != null) {
            Toast.makeText(this, "Welcome Back ${currentuser.email}", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainScreenActivity::class.java))
            finish()
        }
    }
}
package com.ssu.xyvento.sihapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ssu.xyvento.sihapp.databinding.ActivityLoginScreenBinding

class LoginScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val binding: ActivityLoginScreenBinding by lazy {
        ActivityLoginScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainScreenActivity::class.java))
            finish()
            return
        }

        binding.signupOption.setOnClickListener {
            startActivity(Intent(this, RegisterScreenActivity::class.java))
            finish()
        }

        binding.loginButton.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = binding.emailtextfield.editText?.text.toString().trim()
        val password = binding.passwordInputField.editText?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                val uid = auth.currentUser!!.uid

                db.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        val username = document.getString("username") ?: "User"
                        Toast.makeText(this, "Welcome $username", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainScreenActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }
}

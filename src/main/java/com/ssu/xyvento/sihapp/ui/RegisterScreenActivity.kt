package com.ssu.xyvento.sihapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ssu.xyvento.sihapp.databinding.ActivityRegisterScreenBinding
import com.ssu.xyvento.sihapp.ui.MainScreenActivity

data class User(
    val username: String = "",
    val email: String = ""
)

class RegisterScreenActivity : AppCompatActivity() {
    private val binding: ActivityRegisterScreenBinding by lazy {
        ActivityRegisterScreenBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    companion object {
        private const val LOG_TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        gotoLoginScreen()
        registerUser()
    }

    private fun gotoLoginScreen() {
        binding.tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginScreenActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        binding.btnRegister.setOnClickListener {

            val username = binding.nametextfield.editText?.text.toString().trim()
            val email = binding.emailtextfield.editText?.text.toString().trim()
            val password = binding.passwordtextfield.editText?.text.toString().trim()
            val repeatPassword = binding.repeattextfield.editText?.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != repeatPassword) {
                Toast.makeText(this, "Password mismatch", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->

                    val uid = result.user!!.uid
                    val user = User(username, email)

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainScreenActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }

}

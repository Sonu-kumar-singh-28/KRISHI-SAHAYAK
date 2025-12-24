package com.ssu.xyvento.sihapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ssu.xyvento.sihapp.databinding.ActivityUserProfileBinding
import kotlin.random.Random

class User_Profile_Activity : AppCompatActivity() {

    private val binding: ActivityUserProfileBinding by lazy {
        ActivityUserProfileBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        uid = auth.currentUser?.uid ?: run {
            startActivity(Intent(this, LoginScreenActivity::class.java))
            finish()
            return
        }

        setupBackButton()
        setupLogout()
        fetchUserProfile()
        setupEditProfile()
    }

    private fun setupBackButton() {
        binding.backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupLogout() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun fetchUserProfile() {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val username = doc.getString("username") ?: "User"
                val email = doc.getString("email") ?: ""
                val birthday = doc.getString("birthday") ?: generateBirthday()
                val phone = doc.getString("phone") ?: generatePhone()
                val kisanId = doc.getString("kisanId") ?: generateKisanId()

                binding.userName.text = username
                binding.fullNameField.setText(username)
                binding.emailField.setText(email)
                binding.birthdayField.setText(birthday)
                binding.phoneField.setText(phone)
                binding.instaField.setText(kisanId)

                saveGeneratedData(birthday, phone, kisanId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Profile load failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveGeneratedData(birthday: String, phone: String, kisanId: String) {
        val data = hashMapOf(
            "birthday" to birthday,
            "phone" to phone,
            "kisanId" to kisanId
        )

        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
    }

    private fun setupEditProfile() {
        binding.editButton.setOnClickListener {

            val updatedData = hashMapOf(
                "username" to binding.fullNameField.text.toString(),
                "email" to binding.emailField.text.toString(),
                "birthday" to binding.birthdayField.text.toString(),
                "phone" to binding.phoneField.text.toString(),
                "kisanId" to binding.instaField.text.toString()
            )

            db.collection("users").document(uid)
                .set(updatedData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun generateBirthday(): String {
        return "12 Aug 1998"
    }

    private fun generatePhone(): String {
        return "9${Random.nextInt(100000000, 999999999)}"
    }

    private fun generateKisanId(): String {
        return "KISAN_${Random.nextInt(100000, 999999)}"
    }
}

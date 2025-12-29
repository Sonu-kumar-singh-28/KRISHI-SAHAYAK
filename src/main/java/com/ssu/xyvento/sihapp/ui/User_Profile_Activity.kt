package com.ssu.xyvento.sihapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.ssu.xyvento.sihapp.R
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

            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)

            val etName = dialogView.findViewById<EditText>(R.id.etName)
            val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
            val etBirthday = dialogView.findViewById<EditText>(R.id.etBirthday)
            val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)
            val etKisanId = dialogView.findViewById<EditText>(R.id.etKisanId)

            // Set old values
            etName.setText(binding.fullNameField.text.toString())
            etEmail.setText(binding.emailField.text.toString())
            etBirthday.setText(binding.birthdayField.text.toString())
            etPhone.setText(binding.phoneField.text.toString())
            etKisanId.setText(binding.instaField.text.toString())

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save") { _, _ ->

                    val updatedData = hashMapOf(
                        "username" to etName.text.toString(),
                        "email" to etEmail.text.toString(),
                        "birthday" to etBirthday.text.toString(),
                        "phone" to etPhone.text.toString(),
                        "kisanId" to etKisanId.text.toString()
                    )

                    db.collection("users").document(uid)
                        .set(updatedData, SetOptions.merge())
                        .addOnSuccessListener {
                            // Update UI
                            binding.userName.text = etName.text.toString()
                            binding.fullNameField.setText(etName.text.toString())
                            binding.emailField.setText(etEmail.text.toString())
                            binding.birthdayField.setText(etBirthday.text.toString())
                            binding.phoneField.setText(etPhone.text.toString())
                            binding.instaField.setText(etKisanId.text.toString())

                            Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
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

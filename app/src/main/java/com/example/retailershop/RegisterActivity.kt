package com.example.retailershop

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.retailershop.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.registerButton.setOnClickListener {
            val fullName = binding.fullNameRegister.text.toString().trim()
            val email = binding.emailRegister.text.toString().trim()
            val phone = binding.phoneRegister.text.toString().trim()
            val address = binding.addressRegister.text.toString().trim()
            val password = binding.passwordRegister.text.toString()
            val keyCode = binding.keyCodeRegister.text.toString().trim()

            if (validateInput(fullName, email, phone, address, password, keyCode)) {
                registerUser(fullName, email, phone, address, password, keyCode)
            }
        }
    }

    private fun validateInput(fullName: String, email: String, phone: String, address: String, password: String, keyCode: String): Boolean {
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registerUser(fullName: String, email: String, phone: String, address: String, password: String, keyCode: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Mengganti titik dengan koma di email untuk digunakan sebagai ID di Firebase
                val emailForId = email.replace(".", ",")

                // Membuat data pengguna
                val userData = mutableMapOf<String, Any>(
                    "fullName" to fullName,
                    "email" to email,
                    "phone" to phone,
                    "address" to address
                )

                // Jika keyCode diisi, tambahkan keyCode ke data pengguna
                if (keyCode.isNotEmpty()) {
                    userData["keyCode"] = keyCode
                }

                // Simpan data pengguna di Firebase
                database.reference.child("akun").child(emailForId).setValue(userData).addOnCompleteListener { dbTask ->
                    if (dbTask.isSuccessful) {
                        Toast.makeText(this, "Registration successful, please login", Toast.LENGTH_SHORT).show()
                        auth.signOut()  // Logout setelah registrasi
                        // Mengarahkan pengguna ke LoginActivity
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                val errorMessage = task.exception?.localizedMessage ?: "Registration failed"
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

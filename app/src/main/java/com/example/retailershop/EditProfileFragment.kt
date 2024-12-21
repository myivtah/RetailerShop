package com.example.retailershop

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.retailershop.databinding.FragmentEditProfileBinding
import com.example.retailershop.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("akun")

        // Load user profile data into EditText fields
        loadUserProfile()

        // Set up save button click listener
        binding.btnSave.setOnClickListener {
            saveUserProfile()
        }

        return binding.root
    }

    private fun loadUserProfile() {
        val emailForId = auth.currentUser?.email?.replace(".", ",")
        if (emailForId != null) {
            databaseReference.child(emailForId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(User::class.java)
                        if (user != null) {
                            binding.fullNameEdit.setText(user.fullName)
                            binding.emailEdit.setText(user.email)
                            binding.addressEdit.setText(user.address)
                            binding.phoneEdit.setText(user.phone)
                            binding.keyCodeEdit.setText(user.keyCode)
                            binding.storeNameEdit.setText(user.storeName)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(activity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun saveUserProfile() {
        val emailForId = auth.currentUser?.email?.replace(".", ",")
        if (emailForId != null) {
            val fullName = binding.fullNameEdit.text.toString().trim()
            val email = binding.emailEdit.text.toString().trim()
            val phone = binding.phoneEdit.text.toString().trim()
            val storeName = binding.storeNameEdit.text.toString().trim()
            val address = binding.addressEdit.text.toString().trim()
            val keyCode = binding.keyCodeEdit.text.toString().trim()

            if (validateInput(fullName, email, phone, storeName, address, keyCode)) {
                if (keyCode.isEmpty()) {
                    proceedWithProfileUpdate(fullName, email, phone, storeName, address, keyCode)
                } else {
                    validateKeyCodeAndProceed(fullName, email, phone, storeName, address, keyCode)
                }
            }
        }
    }

    private fun validateInput(
        fullName: String,
        email: String,
        phone: String,
        storeName: String,
        address: String,
        keyCode: String
    ): Boolean {
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || storeName.isEmpty() || address.isEmpty()) {
            Toast.makeText(activity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(activity, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun validateKeyCodeAndProceed(
        fullName: String,
        email: String,
        phone: String,
        storeName: String,
        address: String,
        keyCode: String
    ) {
        database.reference.child("Keycode").get().addOnSuccessListener { dataSnapshot ->
            val keyCodes = dataSnapshot.children.mapNotNull { it.getValue(String::class.java) }
            if (keyCodes.contains(keyCode)) {
                // KeyCode valid, lanjutkan dengan pembaruan profil
                proceedWithProfileUpdate(fullName, email, phone, storeName, address, keyCode)
            } else {
                // KeyCode tidak valid
                Toast.makeText(activity, "Invalid keycode", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(activity, "Failed to validate keycode", Toast.LENGTH_SHORT).show()
        }
    }

    private fun proceedWithProfileUpdate(
        fullName: String,
        email: String,
        phone: String,
        storeName: String,
        address: String,
        keyCode: String
    ) {
        val user = User(
            email,
            address,
            fullName,
            keyCode,
            phone,
            storeName
        )

        databaseReference.child(email.replace(".", ",")).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(activity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(activity, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
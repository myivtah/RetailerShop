package com.example.retailershop

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.retailershop.databinding.FragmentDisplayProfileBinding
import com.example.retailershop.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DisplayProfileFragment : Fragment() {

    private lateinit var binding: FragmentDisplayProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDisplayProfileBinding.inflate(inflater, container, false)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("akun")

        // Load user profile
        loadUserProfile()

        // Set up edit button click listener
        binding.btnEdit.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        // Set up logout button click listener
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            activity?.finish()
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
                            binding.profileName.text = user.fullName
                            binding.fullName.text = user.fullName
                            binding.email.text = user.email
                            binding.address.text = user.address
                            binding.phone.text = user.phone
                            binding.keyCode.text = user.keyCode
                            binding.storeName.text = user.storeName
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(activity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}

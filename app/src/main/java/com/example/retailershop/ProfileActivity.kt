package com.example.retailershop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.retailershop.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the DisplayProfileFragment on start
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DisplayProfileFragment())
            .commit()
    }
}

package com.example.retailershop

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Check if user is not logged in, navigate to LoginActivity
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("akun")

        // Find views
        val storeNameTextView: TextView = findViewById(R.id.storeName)
        val profilePicture: ShapeableImageView = findViewById(R.id.profilePicture)

        // Load store name
        loadStoreName(storeNameTextView)

        // Navigate to ProfileActivity
        profilePicture.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // CardView Transaksi
        findViewById<CardView>(R.id.cardSales).setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        // CardView Item
        findViewById<CardView>(R.id.cardAddItem).setOnClickListener {
            startActivity(Intent(this, ItemActivity::class.java))
        }

        // CardView Supplier
        findViewById<CardView>(R.id.cardSuplier).setOnClickListener {
            startActivity(Intent(this, SupplierActivity::class.java))
        }

        // CardView Membership
        findViewById<CardView>(R.id.cardMember).setOnClickListener {
            startActivity(Intent(this, MemberActivity::class.java))
        }

        // CardView History
        findViewById<CardView>(R.id.cardHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun loadStoreName(storeNameTextView: TextView) {
        val emailForId = auth.currentUser?.email?.replace(".", ",")
        if (emailForId != null) {
            databaseReference.child(emailForId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val storeName = dataSnapshot.child("storeName").getValue(String::class.java)
                        storeNameTextView.text = storeName ?: "Store Name"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Failed to load store name", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
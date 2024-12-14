package com.example.retailershop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.adapter.ItemAdapter
import com.example.retailershop.model.Item
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ItemActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private lateinit var database: DatabaseReference
    private lateinit var btnAddItem: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item)

        // Initialize Firebase Auth, RecyclerView, Button, and Firebase Database
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.rv_items)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAddItem = findViewById(R.id.btn_add_item)

        database = FirebaseDatabase.getInstance().reference.child("items")

        // Set up adapter for RecyclerView
        adapter = object : ItemAdapter(mutableListOf()) {
            override fun updateItems(newItems: List<Item>) {
                super.updateItems(newItems)
            }
        }

        recyclerView.adapter = adapter

        // Set up real-time listener for Firebase
        setupRealtimeListener()

        // Set click listener for Add Item button
        btnAddItem.setOnClickListener {
            // Navigate to InputActivity for adding new item
            val intent = Intent(this, InputItemActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRealtimeListener() {
        val userEmail = auth.currentUser?.email ?: return

        // Convert email to a valid Firebase key (replace "@" with "_" and "." with "_")
        val emailKey = userEmail.replace("@", "_").replace(".", "_")

        // Reference to the user's items node
        val userItemsRef = Firebase.database.reference.child("items").child(emailKey)

        // Set up real-time listener
        userItemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val itemsList = mutableListOf<Item>()
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(Item::class.java)
                        item?.let { itemsList.add(it) }
                    }
                    adapter.updateItems(itemsList)
                } else {
                    Toast.makeText(this@ItemActivity, "No items found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ItemActivity, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

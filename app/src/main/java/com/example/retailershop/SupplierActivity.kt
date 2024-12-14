package com.example.retailershop

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.adapter.SupplierAdapter
import com.example.retailershop.model.Supplier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SupplierActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SupplierAdapter
    private lateinit var database: DatabaseReference
    private lateinit var suppliersList: MutableList<Supplier>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supplier)

        // Initialize Firebase Auth, RecyclerView, and Database reference
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.rv_suppliers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        suppliersList = mutableListOf()
        adapter = SupplierAdapter(suppliersList)
        recyclerView.adapter = adapter

        // Get the current user's email and use it as the key for the database
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert email to a valid Firebase key
        val emailKey = userEmail.replace("@", "_").replace(".", "_")

        // Initialize Firebase reference to the user's suppliers
        database = FirebaseDatabase.getInstance().reference.child("suppliers").child(emailKey)

        // Fetch suppliers from Firebase
        setupRealtimeListener()

        // Set up the add supplier button
        val btnAddSupplier = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_add_supplier)
        btnAddSupplier.setOnClickListener {
            val intent = Intent(this, InputSupplierActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRealtimeListener() {
        // Listen for changes to the suppliers data in Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                suppliersList.clear()
                for (supplierSnapshot in snapshot.children) {
                    val supplier = supplierSnapshot.getValue(Supplier::class.java)
                    supplier?.let {
                        suppliersList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SupplierActivity, "Gagal memuat data supplier", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to delete supplier by id
    fun deleteSupplier(supplierId: String) {
        val userEmail = auth.currentUser?.email
        if (userEmail == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val emailKey = userEmail.replace("@", "_").replace(".", "_")
        val supplierRef = database.child(supplierId)

        // Remove supplier from Firebase
        supplierRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Supplier berhasil dihapus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal menghapus supplier", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.example.retailershop

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.adapter.SupplierAdapter
import com.example.retailershop.model.Supplier
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SupplierActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SupplierAdapter
    private lateinit var database: DatabaseReference
    private lateinit var suppliersList: MutableList<Supplier>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supplier)

        // Initialize RecyclerView and Database reference
        recyclerView = findViewById(R.id.rv_suppliers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        suppliersList = mutableListOf()
        adapter = SupplierAdapter(suppliersList)
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance().reference.child("suppliers")

        // Fetch suppliers from Firebase
        fetchSuppliers()

        // Set up the add supplier button
        val btnAddSupplier = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_add_supplier)
        btnAddSupplier.setOnClickListener {
            val intent = Intent(this, InputSupplierActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchSuppliers() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                suppliersList.clear()
                for (supplierSnapshot in snapshot.children) {
                    val supplier = supplierSnapshot.getValue(Supplier::class.java)
                    supplier?.let {
                        suppliersList.add(it)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(this@SupplierActivity, "Gagal memuat data supplier", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

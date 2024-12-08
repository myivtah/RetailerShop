package com.example.retailershop

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.retailershop.model.Supplier

class InputSupplierActivity : AppCompatActivity() {
    private lateinit var etSupplierName: EditText
    private lateinit var etSupplierPhone: EditText
    private lateinit var btnSaveSupplier: Button
    private lateinit var imageBack: ImageButton
    private lateinit var database: DatabaseReference
    private lateinit var lastIdReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userEmail: String? = null

    private var supplierId: String? = null // For editing an existing supplier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_supplier)

        // Initialize views
        etSupplierName = findViewById(R.id.et_supplier_name)
        etSupplierPhone = findViewById(R.id.et_supplier_phone)
        btnSaveSupplier = findViewById(R.id.btn_save_supplier)
        imageBack = findViewById(R.id.imageBack_input_supplier)

        // Initialize Firebase Auth and Database reference
        auth = FirebaseAuth.getInstance()
        userEmail = auth.currentUser?.email
        database = FirebaseDatabase.getInstance().reference.child("suppliers")
        lastIdReference = FirebaseDatabase.getInstance().reference.child("last_supplier_id")

        // Check if we are editing an existing supplier
        supplierId = intent.getStringExtra("supplier_id") // If editing, supplier ID will be passed

        // If editing, fetch existing supplier data
        if (supplierId != null) {
            loadSupplierData(supplierId!!)
        }

        // Set up save button click listener
        btnSaveSupplier.setOnClickListener {
            val name = etSupplierName.text.toString().trim()
            val phone = etSupplierPhone.text.toString().trim()

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "Nama dan nomor telepon harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (supplierId != null) {
                // Update existing supplier
                updateSupplier(supplierId!!, name, phone)
            } else {
                // Add new supplier
                addSupplier(name, phone)
            }
        }

        // Back button
        imageBack.setOnClickListener {
            onBackPressed()
        }
    }

    // Function to add a new supplier
    private fun addSupplier(name: String, phone: String) {
        val currentEmail = userEmail
        if (currentEmail == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the last used ID
        lastIdReference.get().addOnSuccessListener { snapshot ->
            var newId = 1
            if (snapshot.exists()) {
                // Retrieve the last ID used and increment by 1
                newId = snapshot.getValue(Int::class.java) ?: 1
                newId++ // Increment ID
            }

            // Update the last used ID in Firebase
            lastIdReference.setValue(newId)

            // Create a new Supplier with the incremented ID
            val newSupplier = Supplier(newId.toString(), name, phone, currentEmail)

            // Save the new supplier to Firebase directly under "suppliers"
            database.child(newId.toString()).setValue(newSupplier).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Supplier berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    finish() // Go back to the supplier list
                } else {
                    Toast.makeText(this, "Gagal menambahkan supplier", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to update an existing supplier
    private fun updateSupplier(id: String, name: String, phone: String) {
        val currentEmail = userEmail
        if (currentEmail == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedSupplier = Supplier(id, name, phone, currentEmail)

        // Update the supplier under "suppliers" with the supplier's ID
        database.child(id).setValue(updatedSupplier).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Supplier berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish() // Go back to the supplier list
            } else {
                Toast.makeText(this, "Gagal memperbarui supplier", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to load existing supplier data for editing
    private fun loadSupplierData(id: String) {
        database.child(id).get().addOnSuccessListener {
            if (it.exists()) {
                val supplier = it.getValue(Supplier::class.java)
                supplier?.let {
                    etSupplierName.setText(it.name)
                    etSupplierPhone.setText(it.phone)
                }
            }
        }
    }

    // Function to initialize the last supplier ID if it doesn't exist in Firebase
    private fun initializeLastId() {
        lastIdReference.get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                // Initialize the last supplier ID to 1 if it doesn't exist
                lastIdReference.setValue(1)
            }
        }
    }

    // Call initializeLastId when the activity is created
    override fun onStart() {
        super.onStart()
        initializeLastId() // Ensure the last ID is initialized if not already set
    }
}

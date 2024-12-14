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
    private lateinit var auth: FirebaseAuth

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
        database = FirebaseDatabase.getInstance().reference.child("suppliers")

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
        // Get the reference to the user's suppliers node
        val userEmail = auth.currentUser?.email ?: return

        // Convert email to a valid Firebase key
        val emailKey = userEmail.replace("@", "_").replace(".", "_")

        // Get the reference to the user's suppliers node
        val userSuppliersRef = database.child(emailKey)

        // Fetch the current number of suppliers to determine the next ID
        userSuppliersRef.get().addOnSuccessListener { snapshot ->
            val nextSupplierId = (snapshot.childrenCount + 1).toString()  // Increment last ID

            // Create a new Supplier object
            val newSupplier = Supplier(nextSupplierId, name, phone)

            // Save the new supplier in Firebase
            userSuppliersRef.child(nextSupplierId).setValue(newSupplier).addOnCompleteListener {
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
        // Get the reference to the supplier node under the user's email
        val userEmail = auth.currentUser?.email ?: return
        val emailKey = userEmail.replace("@", "_").replace(".", "_")

        val supplierRef = database.child(emailKey).child(id)

        // Create the updated supplier object
        val updatedSupplier = Supplier(id, name, phone)

        // Update the supplier data in Firebase
        supplierRef.setValue(updatedSupplier).addOnCompleteListener {
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
        // Get the reference to the supplier node
        val userEmail = auth.currentUser?.email ?: return
        val emailKey = userEmail.replace("@", "_").replace(".", "_")

        val supplierRef = database.child(emailKey).child(id)

        // Load the supplier data from Firebase
        supplierRef.get().addOnSuccessListener {
            if (it.exists()) {
                val supplier = it.getValue(Supplier::class.java)
                supplier?.let {
                    etSupplierName.setText(it.name)
                    etSupplierPhone.setText(it.phone)
                }
            }
        }
    }
}

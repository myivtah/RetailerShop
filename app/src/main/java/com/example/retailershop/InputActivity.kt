package com.example.retailershop

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.retailershop.model.Item
import com.example.retailershop.model.Supplier
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import java.util.*

class InputActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private var itemId: String? = null
    private var selectedSupplier: Supplier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        database = Firebase.database.reference.child("items")
        itemId = intent.getStringExtra("item_id")

        // If there's an itemId, load the item data from Firebase
        itemId?.let { loadItemData(it) }

        // Load supplier data for the dropdown
        loadSuppliers()

        // Set up the date picker
        val etTanggal = findViewById<EditText>(R.id.et_tanggal_input)
        etTanggal.setOnClickListener { showDateTimePicker(etTanggal) }

        // Set up save button
        findViewById<Button>(R.id.btn_simpan_input).setOnClickListener { saveItem() }

        // Set up barcode scan button
        findViewById<ImageButton>(R.id.btn_scan_barcode_input).setOnClickListener {
            launchBarcodeScanner()
        }
    }

    private fun loadSuppliers() {
        val supplierList = mutableListOf<Supplier>()
        val supplierNames = mutableListOf<String>()

        // Reference to "suppliers" node in Firebase
        val supplierRef = Firebase.database.reference.child("suppliers")
        supplierRef.get().addOnSuccessListener { snapshot ->
            for (child in snapshot.children) {
                val supplier = child.getValue(Supplier::class.java)
                supplier?.let {
                    supplierList.add(it)
                    supplierNames.add(it.name)
                }
            }

            val spinner = findViewById<Spinner>(R.id.spinner_suplier_input)
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, supplierNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            // Handle supplier selection
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    selectedSupplier = supplierList[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedSupplier = null
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load suppliers", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDateTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val selectedTime = "$selectedHour:$selectedMinute"
                editText.setText("$selectedDate $selectedTime")
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

        }, year, month, day).show()
    }

    // Load item data from Firebase if itemId exists
    private fun loadItemData(id: String) {
        database.child(id).get().addOnSuccessListener {
            val item = it.getValue(Item::class.java)
            item?.let {
                findViewById<EditText>(R.id.et_id_item_input).setText(it.codebar)
                findViewById<EditText>(R.id.et_nama_item_input).setText(it.name)
                findViewById<EditText>(R.id.et_harga_satuan_input).setText(it.price.toString())
                findViewById<EditText>(R.id.et_harga_satuan_beli_input).setText(it.purchasePrice.toString())
                findViewById<EditText>(R.id.et_quantity_input).setText(it.quantity.toString())
                findViewById<EditText>(R.id.et_tanggal_input).setText(it.date)
            }
        }
    }

    // Save or update item
    private fun saveItem() {
        val codebar = findViewById<EditText>(R.id.et_id_item_input).text.toString()
        val name = findViewById<EditText>(R.id.et_nama_item_input).text.toString()
        val price = findViewById<EditText>(R.id.et_harga_satuan_input).text.toString().toDoubleOrNull() ?: 0.0
        val purchasePrice = findViewById<EditText>(R.id.et_harga_satuan_beli_input).text.toString().toDoubleOrNull() ?: 0.0
        val quantity = findViewById<EditText>(R.id.et_quantity_input).text.toString().toIntOrNull() ?: 0
        val date = findViewById<EditText>(R.id.et_tanggal_input).text.toString()
        val supplier = selectedSupplier?.name ?: "Unknown"

        // Create an Item object with the updated data
        val item = Item(codebar, name,
            price.toInt(), purchasePrice.toInt(), quantity, date, supplier)

        // If itemId exists, update the existing item, otherwise create a new one
        if (itemId != null) {
            database.child(itemId!!).setValue(item).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // If no itemId exists, it's a new item, add it
            database.child(codebar).setValue(item).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Item saved successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save item", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Launch the barcode scanner
    private fun launchBarcodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Scan a barcode")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    // Handle barcode scan result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                findViewById<EditText>(R.id.et_id_item_input).setText(result.contents)
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
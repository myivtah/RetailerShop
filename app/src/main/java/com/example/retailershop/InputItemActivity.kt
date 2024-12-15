package com.example.retailershop

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.retailershop.model.Item
import com.example.retailershop.model.Supplier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.integration.android.IntentIntegrator
import java.util.*

class InputItemActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var itemId: String? = null
    private var selectedSupplier: Supplier? = null
    private lateinit var auth: FirebaseAuth
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_item)

        database = FirebaseDatabase.getInstance().reference.child("items")
        auth = FirebaseAuth.getInstance()
        userEmail = auth.currentUser?.email

        itemId = intent.getStringExtra("item_id")
        itemId?.let { loadItemData(it) }

        loadSuppliers()
        findViewById<EditText>(R.id.et_tanggal_input).setOnClickListener { showDateTimePicker() }
        findViewById<Button>(R.id.btn_simpan_input).setOnClickListener { saveItem() }
        findViewById<ImageButton>(R.id.btn_scan_barcode_input).setOnClickListener { launchBarcodeScanner() }
    }

    private fun loadItemData(barcode: String) {
        val emailKey = userEmail!!.replace("@", "_").replace(".", "_")
        val itemRef = database.child(emailKey).child(barcode)

        itemRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val item = snapshot.getValue(Item::class.java)
                item?.let {
                    val etBarcode = findViewById<EditText>(R.id.et_id_item_input)
                    etBarcode.setText(it.barcode)
                    etBarcode.isEnabled = false

                    findViewById<EditText>(R.id.et_nama_item_input).setText(it.name)
                    findViewById<EditText>(R.id.et_harga_satuan_input).setText(it.price.toString())
                    findViewById<EditText>(R.id.et_harga_satuan_beli_input).setText(it.purchasePrice.toString())
                    findViewById<EditText>(R.id.et_quantity_input).setText(it.quantity.toString())
                    findViewById<EditText>(R.id.et_tanggal_input).setText(it.date)
                }
            }
        }
    }

    private fun loadSuppliers() {
        val supplierList = mutableListOf<Supplier>()
        val emailKey = userEmail!!.replace("@", "_").replace(".", "_")
        val supplierRef = FirebaseDatabase.getInstance().reference.child("suppliers").child(emailKey)

        supplierRef.get().addOnSuccessListener { snapshot ->
            val supplierNames = snapshot.children.mapNotNull { it.getValue(Supplier::class.java)?.name }
            val supplierMap = snapshot.children.associate { it.getValue(Supplier::class.java)?.name to it.getValue(Supplier::class.java) }
            selectedSupplier = supplierMap[supplierNames.firstOrNull()]

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, supplierNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val spinner = findViewById<Spinner>(R.id.spinner_suplier_input)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedSupplier = supplierMap[supplierNames[position]]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedSupplier = null
                }
            }
        }
    }

    private fun saveItem() {
        val barcode = findViewById<EditText>(R.id.et_id_item_input).text.toString().trim()
        val name = findViewById<EditText>(R.id.et_nama_item_input).text.toString().trim()
        val price = findViewById<EditText>(R.id.et_harga_satuan_input).text.toString().toIntOrNull() ?: 0
        val purchasePrice = findViewById<EditText>(R.id.et_harga_satuan_beli_input).text.toString().toIntOrNull() ?: 0
        val quantity = findViewById<EditText>(R.id.et_quantity_input).text.toString().toIntOrNull() ?: 0
        val date = findViewById<EditText>(R.id.et_tanggal_input).text.toString().trim()

        if (barcode.isEmpty()) {
            Toast.makeText(this, "Barcode tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val emailKey = userEmail!!.replace("@", "_").replace(".", "_")
        val userItemsRef = database.child(emailKey).child(barcode)
        val item = Item(barcode, name, price, purchasePrice, quantity, date, selectedSupplier?.name ?: "")

        userItemsRef.setValue(item).addOnCompleteListener {
            Toast.makeText(this, "Item berhasil disimpan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val date = "$year-${month + 1}-$day"
            findViewById<EditText>(R.id.et_tanggal_input).setText(date)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun launchBarcodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setPrompt("Scan Barcode")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }
}

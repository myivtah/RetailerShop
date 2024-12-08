package com.example.retailershop

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.adapter.ProductAdapter
import com.example.retailershop.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.zxing.integration.android.IntentIntegrator
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionActivity : AppCompatActivity(), ProductAdapter.OnProductActionListener {

    private lateinit var productList: MutableList<Product>
    private lateinit var productAdapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalPriceText: TextView
    private lateinit var cashPaidEdit: EditText
    private lateinit var cashChangeText: TextView
    private lateinit var btnConfirmTransaction: Button
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        auth = FirebaseAuth.getInstance()
        userEmail = auth.currentUser?.email

        productList = mutableListOf()
        recyclerView = findViewById(R.id.recyclerViewTransaction)
        totalPriceText = findViewById(R.id.totalPrice)
        cashPaidEdit = findViewById(R.id.Cash)
        cashChangeText = findViewById(R.id.totalKembalian)
        btnConfirmTransaction = findViewById(R.id.submitTransaction)

        productAdapter = ProductAdapter(productList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = productAdapter

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference

        val inputBarcode = findViewById<EditText>(R.id.inputBarcode)
        val btnScanBarcode = findViewById<ImageButton>(R.id.btnScanBarcode)
        val btnAddItem = findViewById<ImageButton>(R.id.tambahItemTransaksi)

        btnScanBarcode.setOnClickListener {
            launchBarcodeScanner()
        }

        btnAddItem.setOnClickListener {
            val barcodeInput = inputBarcode.text.toString().trim()
            if (barcodeInput.isNotEmpty()) {
                findProductByBarcode(barcodeInput)
            } else {
                Toast.makeText(this, "Please enter a barcode", Toast.LENGTH_SHORT).show()
            }
        }

        cashPaidEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateCashChange()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnConfirmTransaction.setOnClickListener {
            checkStockAndSubmitTransaction()
        }
    }

    private fun findProductByBarcode(barcode: String) {
        val productRef = database.child("items").child(barcode)
        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val price = snapshot.child("price").getValue(Int::class.java) ?: 0
                    val foundProduct = Product(barcode, name ?: "Unknown", price, 1)
                    productList.add(foundProduct)
                    productAdapter.notifyDataSetChanged()
                    updateTotal()
                } else {
                    Toast.makeText(this@TransactionActivity, "Product not found", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransactionActivity, "Failed to fetch product data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateTotal() {
        totalPriceText.text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(calculateTotalPrice())
        updateCashChange()
    }

    private fun calculateTotalPrice(): Int {
        return productList.sumBy { it.price * it.quantity }
    }

    private fun calculateCashChange(): Int {
        val totalPrice = calculateTotalPrice()
        val cashPaid = cashPaidEdit.text.toString().toIntOrNull() ?: 0
        return cashPaid - totalPrice
    }

    private fun updateCashChange() {
        cashChangeText.text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(calculateCashChange())
    }

    private fun checkStockAndSubmitTransaction() {
        val totalPrice = calculateTotalPrice()
        val cashPaid = cashPaidEdit.text.toString().toIntOrNull() ?: 0

        if (cashPaid < totalPrice) {
            Toast.makeText(this, "Cash tidak cukup", Toast.LENGTH_SHORT).show()
            return
        }

        val allStockAvailable = productList.all { product ->
            val productRef = database.child("items").child(product.barcode)
            var stockAvailable = true
            productRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val stockQuantity = snapshot.child("quantity").getValue(Int::class.java) ?: 0
                        when {
                            product.quantity < stockQuantity -> {
                                productRef.child("quantity").setValue(stockQuantity - product.quantity)
                            }
                            product.quantity == stockQuantity -> {
                                productRef.removeValue()
                            }
                            else -> {
                                stockAvailable = false
                                Toast.makeText(this@TransactionActivity, "Stok ${product.name} lebih sedikit", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        stockAvailable = false
                        Toast.makeText(this@TransactionActivity, "Product not found: ${product.name}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    stockAvailable = false
                    Toast.makeText(this@TransactionActivity, "Failed to fetch product data", Toast.LENGTH_SHORT).show()
                }
            })
            stockAvailable
        }

        if (allStockAvailable) {
            saveTransactionToDatabase()
        }
    }

    private fun saveTransactionToDatabase() {
        val currentEmail = userEmail
        if (currentEmail == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        // Menentukan id transaksi sebagai nomor urut
        database.child("transactions").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactionId = (snapshot.childrenCount + 1).toString()
                val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                val transactionData = HashMap<String, Any>().apply {
                    put("id", transactionId)
                    put("productList", productList.map { it.toMap() })
                    put("totalPrice", calculateTotalPrice())
                    put("cashPaid", NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(cashPaidEdit.text.toString().toIntOrNull() ?: 0))
                    put("cashChange", NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(calculateCashChange()))
                    put("transactionDate", currentTime)
                    put("userEmail", currentEmail) // Menyimpan email pengguna yang sedang login
                }

                database.child("transactions").child(transactionId).setValue(transactionData)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            // Berhasil disimpan, pindah ke SubmittedActivity
                            val intent = Intent(this@TransactionActivity, SubmittedActivity::class.java)
                            intent.putExtra("transactionId", transactionId)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@TransactionActivity, "Failed to submit transaction", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransactionActivity, "Failed to fetch transaction data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onQuantityChanged(product: Product) {
        updateTotal()
    }

    override fun onProductDeleted(product: Product) {
        productList.remove(product)
        productAdapter.notifyDataSetChanged()
        updateTotal()
    }

    private fun launchBarcodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Scan a barcode")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                findViewById<EditText>(R.id.inputBarcode).setText(result.contents)
                findProductByBarcode(result.contents)
            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

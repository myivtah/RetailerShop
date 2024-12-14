package com.example.retailershop

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
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

        if (userEmail == null) {
            // Jika tidak ada pengguna yang login, arahkan ke halaman login
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

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
            checkTransactionLimitAndKeyCode(userEmail ?: "")
        }
    }

    private fun findProductByBarcode(barcode: String) {
        val productRef = database.child("items").child(barcode).child(userEmail?.replace(".", "_") ?: "")
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

    private fun checkTransactionLimitAndKeyCode(currentEmail: String) {
        if (currentEmail.isEmpty()) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val transactionsRef = database.child("transactions").orderByChild("userEmail").equalTo(currentEmail)
        transactionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactionCount = snapshot.childrenCount
                if (transactionCount >= 20) {
                    checkForKeyCodeAndProceed(currentEmail)
                } else {
                    validateAndSaveTransaction()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransactionActivity, "Failed to fetch transaction count", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun validateAndSaveTransaction() {
        val cashPaid = cashPaidEdit.text.toString().toIntOrNull() ?: 0
        val totalPrice = calculateTotalPrice()

        if (cashPaid < totalPrice) {
            Toast.makeText(this, "Cash paid must be equal to or greater than total price", Toast.LENGTH_SHORT).show()
            return
        }

        saveTransactionToDatabase()
    }

    private fun checkForKeyCodeAndProceed(currentEmail: String) {
        val userRef = database.child("akun").child(currentEmail.replace(".", "_"))
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val keyCode = snapshot.child("keyCode").getValue(String::class.java)

                if (keyCode == null) {
                    showSubscriptionNotificationDialog()
                } else {
                    saveTransactionToDatabase()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransactionActivity, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveTransactionToDatabase() {
        val currentEmail = userEmail
        if (currentEmail == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

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
                            reduceStockAndProceed(transactionId)
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

    private fun reduceStockAndProceed(transactionId: String) {
        for (product in productList) {
            val productRef = database.child("items").child(product.barcode).child(userEmail?.replace(".", "_") ?: "")
            productRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val currentStock = snapshot.child("quantity").getValue(Int::class.java) ?: 0
                    val updatedStock = currentStock - product.quantity

                    productRef.child("quantity").setValue(updatedStock)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                if (productList.indexOf(product) == productList.size - 1) {
                                    val intent = Intent(this@TransactionActivity, SubmittedActivity::class.java)
                                    intent.putExtra("transactionId", transactionId)
                                    startActivity(intent)
                                }
                            } else {
                                Toast.makeText(this@TransactionActivity, "Failed to update stock for product ${product.name}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@TransactionActivity, "Failed to fetch product stock", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun showSubscriptionNotificationDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Transaksi Anda lebih dari 20. Silakan hubungi admin untuk melanjutkan langganan.")
            .setCancelable(false)
            .setPositiveButton("Hubungi Admin") { _, _ ->
                val phoneNumber = "628970990504"
                val message = "Halo Admin, saya ingin melanjutkan transaksi lebih dari 20 item. Mohon bantuannya."
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(message)}"))
                startActivity(intent)
            }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
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

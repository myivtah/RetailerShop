package com.example.retailershop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.adapter.ProductAdapter
import com.example.retailershop.model.Product
import com.google.firebase.database.*
import com.google.zxing.integration.android.IntentIntegrator
import java.text.NumberFormat
import java.util.*

class TransactionActivity : AppCompatActivity(), ProductAdapter.OnProductActionListener {

    private lateinit var productList: MutableList<Product>
    private lateinit var productAdapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalPriceText: TextView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        productList = mutableListOf()
        recyclerView = findViewById(R.id.recyclerViewTransaction)
        totalPriceText = findViewById(R.id.totalPrice)

        productAdapter = ProductAdapter(productList, this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = productAdapter

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference

        val inputBarcode = findViewById<EditText>(R.id.inputBarcode)
        val btnScanBarcode = findViewById<ImageButton>(R.id.btnScanBarcode)
        val btnAddItem = findViewById<ImageButton>(R.id.tambahItemTransaksi)

        // Ketika tombol Scan Barcode ditekan
        btnScanBarcode.setOnClickListener {
            launchBarcodeScanner()
        }

        // Ketika tombol tambah item ditekan
        btnAddItem.setOnClickListener {
            val barcodeInput = inputBarcode.text.toString().trim()

            // Memastikan barcode yang dimasukkan tidak kosong
            if (barcodeInput.isNotEmpty()) {
                // Mencocokkan barcode dengan daftar produk di Firebase
                findProductByBarcode(barcodeInput)
            } else {
                Toast.makeText(this, "Please enter a barcode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk mencari produk berdasarkan barcode dari Firebase Realtime Database
    private fun findProductByBarcode(barcode: String) {
        // Mengambil referensi ke node items di Firebase Realtime Database
        val productRef = database.child("items").child(barcode)

        // Membaca data produk berdasarkan barcode
        productRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Jika data ditemukan, ambil informasi produk
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val price = snapshot.child("price").getValue(Double::class.java) ?: 0.0
                    val quantity = snapshot.child("quantity").getValue(Int::class.java) ?: 1

                    // Membuat objek Product
                    val foundProduct = Product(
                        barcode,
                        name ?: "Unknown",
                        price,
                        quantity
                    )

                    // Menambahkan produk ke daftar produk yang ditampilkan di RecyclerView
                    productList.add(foundProduct)
                    productAdapter.notifyDataSetChanged()

                    // Mengupdate total harga
                    updateTotalPrice()

                } else {
                    // Jika produk tidak ditemukan
                    Toast.makeText(this@TransactionActivity, "Product not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TransactionActivity, "Failed to fetch product data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi untuk menghitung total harga berdasarkan produk di RecyclerView
    private fun updateTotalPrice() {
        var total = 0.0
        for (product in productList) {
            total += product.price * product.quantity
        }

        // Format total dengan simbol Rupiah dan pemisah ribuan
        val formattedTotal = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(total)
        totalPriceText.text = "Total: $formattedTotal"
    }

    // Menangani perubahan kuantitas produk
    override fun onQuantityChanged(product: Product) {
        updateTotalPrice()
    }

    // Menangani penghapusan produk dari transaksi
    override fun onProductDeleted(product: Product) {
        productList.remove(product)
        productAdapter.notifyDataSetChanged()
        updateTotalPrice()
    }

    // Memulai barcode scanner
    private fun launchBarcodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Scan a barcode")
        integrator.setBeepEnabled(true)
        integrator.initiateScan()
    }

    // Menangani hasil dari barcode scanner
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

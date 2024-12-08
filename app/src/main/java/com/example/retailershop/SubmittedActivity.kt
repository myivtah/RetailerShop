package com.example.retailershop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.adapter.SubmittedAdapter
import com.example.retailershop.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.NumberFormat
import java.util.*

class SubmittedActivity : AppCompatActivity() {

    private lateinit var transactionId: String
    private lateinit var productList: MutableList<Product>
    private lateinit var submittedAdapter: SubmittedAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalPriceText: TextView
    private lateinit var cashPaidText: TextView
    private lateinit var cashChangeText: TextView
    private lateinit var btnConfirmSubmission: Button
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submitted)

        auth = FirebaseAuth.getInstance()

        // Initialize views
        transactionId = intent.getStringExtra("transactionId") ?: return
        database = FirebaseDatabase.getInstance().reference.child("transactions").child(transactionId)

        productList = mutableListOf()
        recyclerView = findViewById(R.id.recyclerViewSubmitted)
        totalPriceText = findViewById(R.id.totalPriceSubmitted)
        cashPaidText = findViewById(R.id.cashPaidSubmitted)
        cashChangeText = findViewById(R.id.cashChangeSubmitted)
        btnConfirmSubmission = findViewById(R.id.btnConfirmSubmission)

        // Setup RecyclerView
        submittedAdapter = SubmittedAdapter(productList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = submittedAdapter

        // Load transaction data
        loadTransactionData()

        // Set onClick listener for confirm button
        btnConfirmSubmission.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadTransactionData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userEmail = auth.currentUser?.email
                    snapshot.getValue(object : GenericTypeIndicator<HashMap<String, Any>>() {})?.let {
                        if (it["userEmail"] == userEmail) {
                            val productListData = it["productList"] as List<HashMap<String, Any>>
                            productList.addAll(productListData.map { productMap ->
                                Product(
                                    productMap["barcode"] as String,
                                    productMap["name"] as String,
                                    (productMap["price"] as Long).toInt(),
                                    (productMap["quantity"] as Long).toInt()
                                )
                            })
                            submittedAdapter.notifyDataSetChanged()

                            val totalPrice = it["totalPrice"].toString().toLongOrNull() ?: 0L
                            val cashPaid = convertCurrencyStringToLong(it["cashPaid"].toString())
                            val cashChange = convertCurrencyStringToLong(it["cashChange"].toString())

                            totalPriceText.text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(totalPrice)
                            cashPaidText.text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(cashPaid)
                            cashChangeText.text = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(cashChange)
                        } else {
                            Toast.makeText(this@SubmittedActivity, "Access denied", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this@SubmittedActivity, "Transaction data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SubmittedActivity, "Failed to fetch transaction data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun convertCurrencyStringToLong(currencyString: String): Long {
        return currencyString.replace("[^\\d,]".toRegex(), "").replace(",", ".").toDouble().toLong()
    }
}

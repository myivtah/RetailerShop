package com.example.retailershop

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.adapter.TransactionAdapter
import com.example.retailershop.model.Transaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var transactionList: MutableList<Transaction>
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        // Pastikan ID "main" adalah benar dan tidak null
        val mainView = findViewById<View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        } ?: run {
            // Handle null case for mainView
            println("View with ID 'main' not found")
        }

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Mendapatkan email pengguna dan mengubahnya menjadi format yang sesuai untuk Firebase
        val userEmail = auth.currentUser?.email?.replace(".", "_")?.replace("@", "_") ?: return

        // Inisialisasi list dan adapter
        transactionList = mutableListOf()
        adapter = TransactionAdapter(transactionList)
        recyclerView.adapter = adapter

        // Inisialisasi Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("transactions").child(userEmail)

        // Mendapatkan data dari Firebase
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                transactionList.clear()
                for (snapshot in dataSnapshot.children) {
                    val transaction = snapshot.getValue(Transaction::class.java)
                    transaction?.let { transactionList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Tangani kesalahan
            }
        })
    }
}

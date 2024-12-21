package com.example.retailershop

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Check if user is not logged in, navigate to LoginActivity
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // CardView Transaksi
        findViewById<CardView>(R.id.cardSales).setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        // CardView Item
        findViewById<CardView>(R.id.cardAddItem).setOnClickListener {
            startActivity(Intent(this, ItemActivity::class.java))
        }

        // CardView Supplier
        findViewById<CardView>(R.id.cardSuplier).setOnClickListener {
            startActivity(Intent(this, SupplierActivity::class.java))
        }

        // CardView Membership
        findViewById<CardView>(R.id.cardMember).setOnClickListener {
            startActivity(Intent(this, MemberActivity::class.java))
        }

        // CardView History
        findViewById<CardView>(R.id.cardHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Button Logout
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}

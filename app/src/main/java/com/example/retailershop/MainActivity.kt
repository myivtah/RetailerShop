package com.example.retailershop

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }
}

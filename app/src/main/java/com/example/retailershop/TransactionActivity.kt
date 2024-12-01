package com.example.retailershop

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class TransactionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var totalPrice: TextView
    private lateinit var adapter: ProductAdapter
    private val products = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        recyclerView = findViewById(R.id.recyclerView)
        totalPrice = findViewById(R.id.totalPrice)

        products.addAll(generateMockData())

        adapter = ProductAdapter(products, object : ProductAdapter.OnProductChangeListener {
            override fun onQuantityChanged(product: Product, newQuantity: Int) {
                product.quantity = newQuantity
                updateTotalPrice()
            }

            override fun onItemDeleted(product: Product) {
                products.remove(product)
                adapter.notifyDataSetChanged()
                updateTotalPrice()
                Toast.makeText(this@TransactionActivity, "Item Deleted", Toast.LENGTH_SHORT).show()
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val total = products.sumOf { it.price * it.quantity }
        totalPrice.text = String.format("Total: $%.2f", total)
    }

    private fun generateMockData(): List<Product> {
        return listOf(
            Product("Product 1", 10.0, 1),
            Product("Product 2", 15.5, 2),
            Product("Product 3", 8.0, 3)
        )
    }
}

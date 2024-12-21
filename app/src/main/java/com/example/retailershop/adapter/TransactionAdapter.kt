package com.example.retailershop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.R
import com.example.retailershop.model.Transaction
import com.example.retailershop.model.ProductFirebase

class TransactionAdapter(private val transactionList: MutableList<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.transactionDate.text = transaction.transactionDate
        holder.cashChange.text = transaction.cashChange
        holder.cashPaid.text = transaction.cashPaid
        holder.totalPrice.text = transaction.totalPrice.toString()

        // Tampilkan daftar produk
        val productsText = transaction.productList?.joinToString("\n") { product ->
            "${product.name} x${product.quantity} @ Rp${product.price}"
        } ?: "No products"
        holder.productList.text = productsText
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionDate: TextView = itemView.findViewById(R.id.transactionDate)
        val cashChange: TextView = itemView.findViewById(R.id.cashChange)
        val cashPaid: TextView = itemView.findViewById(R.id.cashPaid)
        val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
        val productList: TextView = itemView.findViewById(R.id.productList)
    }
}

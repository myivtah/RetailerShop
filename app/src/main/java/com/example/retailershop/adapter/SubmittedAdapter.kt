package com.example.retailershop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.R
import com.example.retailershop.model.Product
import java.text.NumberFormat
import java.util.*

class SubmittedAdapter(
    private val productList: MutableList<Product>
) : RecyclerView.Adapter<SubmittedAdapter.SubmittedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmittedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_submitted, parent, false)
        return SubmittedViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubmittedViewHolder, position: Int) {
        val product = productList[position]

        // Menampilkan nama produk, harga, kuantitas, dan subtotal
        holder.nameTextView.text = product.name

        // Format harga menjadi integer tanpa desimal
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }.format(product.price)

        holder.priceTextView.text = formattedPrice
        holder.quantityTextView.text = product.quantity.toString()

        // Menghitung subtotal
        val subtotal = product.price * product.quantity
        val formattedSubtotal = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }.format(subtotal)
        holder.subtotalTextView.text = formattedSubtotal
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class SubmittedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.productName)
        val priceTextView: TextView = itemView.findViewById(R.id.productPrice)
        val quantityTextView: TextView = itemView.findViewById(R.id.productQuantity)
        val subtotalTextView: TextView = itemView.findViewById(R.id.productSubtotal)
    }
}

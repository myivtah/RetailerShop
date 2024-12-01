package com.example.retailershop

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private val products: List<Product>,
    private val listener: OnProductChangeListener
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)

        // Update quantity
        holder.quantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newQuantity = s?.toString()?.toIntOrNull() ?: 0
                listener.onQuantityChanged(product, newQuantity)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Delete item
        holder.deleteItem.setOnClickListener {
            listener.onItemDeleted(product)
        }
    }

    override fun getItemCount(): Int = products.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val quantity: EditText = itemView.findViewById(R.id.quantity)
        val deleteItem: ImageButton = itemView.findViewById(R.id.deleteItem)

        fun bind(product: Product) {
            productName.text = product.name
            productPrice.text = String.format("$%.2f", product.price)
            quantity.setText(product.quantity.toString())
        }
    }

    interface OnProductChangeListener {
        fun onQuantityChanged(product: Product, newQuantity: Int)
        fun onItemDeleted(product: Product)
    }
}

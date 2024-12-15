package com.example.retailershop.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.R
import com.example.retailershop.model.Product
import java.text.NumberFormat
import java.util.*

class ProductAdapter(
    private val productList: MutableList<Product>,
    private val listener: OnProductActionListener
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    interface OnProductActionListener {
        fun onQuantityChanged(product: Product)
        fun onProductDeleted(product: Product)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // Menampilkan nama produk, harga, dan kuantitas
        holder.nameTextView.text = product.name

        // Format harga menjadi integer tanpa desimal
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }.format(product.price)

        holder.priceTextView.text = formattedPrice
        holder.quantityEditText.setText(product.quantity.toString())

        // Menghapus TextWatcher sebelumnya untuk mencegah rekursi
        holder.quantityEditText.tag?.let {
            holder.quantityEditText.removeTextChangedListener(it as TextWatcher)
        }

        // Menambahkan TextWatcher baru
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    try {
                        val newQuantity = s.toString().toInt()
                        if (newQuantity > 0) {
                            product.quantity = newQuantity
                            listener.onQuantityChanged(product)
                        }
                    } catch (e: NumberFormatException) {
                        holder.quantityEditText.setText(product.quantity.toString())
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        holder.quantityEditText.addTextChangedListener(textWatcher)
        holder.quantityEditText.tag = textWatcher

        // Menambahkan event listener untuk tombol tambah dan kurang kuantitas
        holder.increaseQuantityButton.setOnClickListener {
            product.quantity++
            holder.quantityEditText.setText(product.quantity.toString())  // Update EditText
            listener.onQuantityChanged(product)
        }

        holder.decreaseQuantityButton.setOnClickListener {
            if (product.quantity > 1) {
                product.quantity--
                holder.quantityEditText.setText(product.quantity.toString())  // Update EditText
                listener.onQuantityChanged(product)
            }
        }

        // Tombol hapus produk
        holder.deleteButton.setOnClickListener {
            listener.onProductDeleted(product)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.productName)
        val priceTextView: TextView = itemView.findViewById(R.id.productPrice)
        val quantityEditText: EditText = itemView.findViewById(R.id.ev_quantity)
        val increaseQuantityButton: ImageButton = itemView.findViewById(R.id.btnIncrease)
        val decreaseQuantityButton: ImageButton = itemView.findViewById(R.id.btnDecrease)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDelete)
    }
}

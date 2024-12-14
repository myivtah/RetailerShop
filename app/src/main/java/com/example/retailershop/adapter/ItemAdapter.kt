package com.example.retailershop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.InputItemActivity
import com.example.retailershop.R
import com.example.retailershop.model.Item
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

abstract class ItemAdapter(private var items: MutableList<Item>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tv_id_item)
        val tvName: TextView = view.findViewById(R.id.tv_nama_item)
        val tvPrice: TextView = view.findViewById(R.id.tv_harga_satuan)
        val tvQuantity: TextView = view.findViewById(R.id.tv_quantity)
        val tvDate: TextView = view.findViewById(R.id.tv_tanggal_input)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_edit_item)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.tvId.text = item.barcode
        holder.tvName.text = item.name
        holder.tvPrice.text = item.price.toString()
        holder.tvQuantity.text = item.quantity.toString()
        holder.tvDate.text = item.date

        // Edit button click listener
        holder.btnEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, InputItemActivity::class.java)
            intent.putExtra("item_id", item.barcode) // Mengirimkan ID untuk diedit
            holder.itemView.context.startActivity(intent)
        }

        // Delete button click listener
        holder.btnDelete.setOnClickListener {
            val database = FirebaseDatabase.getInstance().reference.child("items")
            val userEmail = FirebaseAuth.getInstance().currentUser?.email
            val emailKey = userEmail?.replace("@", "_")?.replace(".", "_") ?: return@setOnClickListener

            // Menghapus item dari Firebase berdasarkan ID
            database.child(emailKey).child(item.barcode).removeValue().addOnSuccessListener {
                // Menghapus item dari daftar dan memperbarui UI
                items.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(holder.itemView.context, "Item deleted successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(holder.itemView.context, "Failed to delete item", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = items.size

    // Tambahkan metode updateItems di sini
    open fun updateItems(newItems: List<Item>) {
        items.clear()  // Kosongkan daftar yang lama
        items.addAll(newItems)  // Tambahkan item yang baru
        notifyDataSetChanged()  // Memberitahu adapter untuk merefresh tampilan
    }
}

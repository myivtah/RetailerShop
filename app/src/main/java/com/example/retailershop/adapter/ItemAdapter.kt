package com.example.retailershop.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.InputItemActivity
import com.example.retailershop.model.Item
import com.google.firebase.database.FirebaseDatabase
import com.example.retailershop.R

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
        holder.tvId.text = item.codebar
        holder.tvName.text = item.name
        holder.tvPrice.text = item.price.toString()
        holder.tvQuantity.text = item.quantity.toString()
        holder.tvDate.text = item.date

        holder.btnEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, InputItemActivity::class.java)
            intent.putExtra("item_id", item.codebar) // Pass the codebar (ID) to InputActivity
            holder.itemView.context.startActivity(intent)
        }

        holder.btnDelete.setOnClickListener {
            val database = FirebaseDatabase.getInstance().reference.child("items")
            database.child(item.codebar).removeValue().addOnSuccessListener {
                items.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    override fun getItemCount() = items.size

    open fun updateItems(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

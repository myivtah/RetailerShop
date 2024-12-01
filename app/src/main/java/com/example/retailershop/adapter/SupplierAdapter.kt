package com.example.retailershop.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.R
import com.example.retailershop.model.Supplier
import com.example.retailershop.InputSupplierActivity

class SupplierAdapter(private val suppliers: MutableList<Supplier>) : RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder>() {

    inner class SupplierViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_supplier_name)
        val tvPhone: TextView = view.findViewById(R.id.tv_supplier_phone)
        val btnEdit: ImageButton = view.findViewById(R.id.btn_edit_supplier)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_supplier)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_supplier, parent, false)
        return SupplierViewHolder(view)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        val supplier = suppliers[position]
        holder.tvName.text = supplier.name
        holder.tvPhone.text = supplier.phone

        // Edit supplier onClick
        holder.btnEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, InputSupplierActivity::class.java)
            intent.putExtra("supplier_id", supplier.id)
            holder.itemView.context.startActivity(intent)
        }

        // Delete supplier onClick
        holder.btnDelete.setOnClickListener {
            // Implement delete logic
        }
    }

    override fun getItemCount(): Int = suppliers.size

    fun updateSuppliers(newSuppliers: List<Supplier>) {
        suppliers.clear()
        suppliers.addAll(newSuppliers)
        notifyDataSetChanged()
    }
}

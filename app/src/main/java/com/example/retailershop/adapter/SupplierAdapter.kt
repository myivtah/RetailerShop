package com.example.retailershop.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.InputSupplierActivity
import com.example.retailershop.R
import com.example.retailershop.SupplierActivity
import com.example.retailershop.model.Supplier

class SupplierAdapter(private val suppliers: MutableList<Supplier>) : RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_supplier, parent, false)
        return SupplierViewHolder(view)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        val supplier = suppliers[position]
        holder.bind(supplier)
    }

    override fun getItemCount(): Int = suppliers.size

    inner class SupplierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_supplier_name)
        private val tvPhone: TextView = itemView.findViewById(R.id.tv_supplier_phone)
        private val btnEdit: AppCompatImageButton = itemView.findViewById(R.id.btn_edit_supplier) // Ganti menjadi AppCompatImageButton
        private val btnDelete: AppCompatImageButton = itemView.findViewById(R.id.btn_delete_supplier) // Ganti menjadi AppCompatImageButton

        fun bind(supplier: Supplier) {
            tvName.text = supplier.name
            tvPhone.text = supplier.phone

            // Edit Supplier
            btnEdit.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, InputSupplierActivity::class.java)
                intent.putExtra("supplier_id", supplier.id)
                context.startActivity(intent)
            }

            // Delete Supplier
            btnDelete.setOnClickListener {
                (itemView.context as SupplierActivity).deleteSupplier(supplier.id)
            }
        }
    }

}

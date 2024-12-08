package com.example.retailershop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.retailershop.R
import com.example.retailershop.model.Member

class MemberAdapter(
    private var memberList: MutableList<Member>,
    private val onEditClick: (Member) -> Unit,
    private val onDeleteClick: (Member) -> Unit
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = memberList[position]
        holder.bind(member, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    fun updateMembers(newMembers: List<Member>) {
        memberList.clear()
        memberList.addAll(newMembers)
        notifyDataSetChanged()
    }

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val memberId: TextView = itemView.findViewById(R.id.tv_member_id)
        private val memberName: TextView = itemView.findViewById(R.id.tv_member_name)
        private val memberEmail: TextView = itemView.findViewById(R.id.tv_member_email)
        private val memberPhone: TextView = itemView.findViewById(R.id.tv_member_phone)
        private val memberAddress: TextView = itemView.findViewById(R.id.tv_member_address)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_member)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_member)

        fun bind(member: Member, onEditClick: (Member) -> Unit, onDeleteClick: (Member) -> Unit) {
            memberId.text = member.id
            memberName.text = member.name
            memberEmail.text = member.email
            memberPhone.text = member.phone
            memberAddress.text = member.address

            btnEdit.setOnClickListener { onEditClick(member) }
            btnDelete.setOnClickListener { onDeleteClick(member) }
        }
    }
}
